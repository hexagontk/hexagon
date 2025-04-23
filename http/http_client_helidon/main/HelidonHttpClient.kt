package com.hexagontk.http.client.helidon

import com.hexagontk.core.media.TEXT_EVENT_STREAM
import com.hexagontk.core.security.loadKeyStore
import com.hexagontk.http.handlers.bodyToBytes
import com.hexagontk.http.HttpFeature
import com.hexagontk.http.HttpFeature.*
import com.hexagontk.http.client.HttpClient
import com.hexagontk.http.client.HttpClientPort
import com.hexagontk.http.client.HttpClientSettings
import com.hexagontk.http.model.HttpResponse
import com.hexagontk.http.model.*
import com.hexagontk.http.model.CookieSameSite.*
import com.hexagontk.http.model.HttpProtocol.HTTP
import com.hexagontk.http.model.HttpProtocol.HTTPS
import com.hexagontk.http.model.ws.WsSession
import com.hexagontk.http.parseContentType
import org.eclipse.jetty.client.HttpResponseException
import org.eclipse.jetty.client.ContentResponse
import org.eclipse.jetty.client.Request
import org.eclipse.jetty.client.Response
import org.eclipse.jetty.client.transport.HttpClientTransportDynamic
import org.eclipse.jetty.client.transport.HttpClientConnectionFactory.HTTP11
import org.eclipse.jetty.client.BytesRequestContent
import org.eclipse.jetty.client.MultiPartRequestContent
import org.eclipse.jetty.client.StringRequestContent
import org.eclipse.jetty.http.HttpCookie
import org.eclipse.jetty.http.HttpCookie.SameSite
import org.eclipse.jetty.http.HttpCookieStore
import org.eclipse.jetty.http.HttpFields as JettyHttpFields
import org.eclipse.jetty.http.HttpFields.EMPTY
import org.eclipse.jetty.http.HttpMethod
import org.eclipse.jetty.http.MultiPart.ContentSourcePart
import org.eclipse.jetty.io.ClientConnector
import java.net.URI
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.Flow.Publisher
import java.util.concurrent.SubmissionPublisher
import org.eclipse.jetty.http2.client.HTTP2Client as JettyHttp2Client
import org.eclipse.jetty.http2.client.transport.ClientConnectionFactoryOverHTTP2.HTTP2
import org.eclipse.jetty.websocket.client.WebSocketClient
import org.eclipse.jetty.client.HttpClient as JettyClient
import org.eclipse.jetty.util.ssl.SslContextFactory.Client as ClientSslContextFactory

/**
 * Client to use other REST services.
 */
open class HelidonHttpClient : HttpClientPort {

    protected lateinit var jettyClient: JettyClient
    protected lateinit var httpClient: HttpClient
    private lateinit var httpSettings: HttpClientSettings
    private lateinit var wsClient: WebSocketClient
    private var started: Boolean = false
    private val publisherExecutor = Executors.newSingleThreadExecutor()

    override fun startUp(client: HttpClient) {
        val clientConnector = ClientConnector()
        val settings = client.settings
        clientConnector.sslContextFactory = sslContext(settings)

        val http2 = HTTP2(JettyHttp2Client(clientConnector))
        val transport = HttpClientTransportDynamic(clientConnector, HTTP11, http2)

        jettyClient = JettyClient(transport)
        httpClient = client
        httpSettings = settings

        jettyClient.userAgentField = null // Disable default user agent header
        jettyClient.isFollowRedirects = settings.followRedirects
        jettyClient.start()
        wsClient = WebSocketClient(jettyClient)
        wsClient.start()
        started = true
    }

    override fun shutDown() {
        jettyClient.stop()
        wsClient.stop()
        started = false
    }

    override fun started() =
        started

    override fun send(request: HttpRequestPort): HttpResponsePort {
        val response =
            try {
                createJettyRequest(jettyClient, request).send()
            }
            catch (e: ExecutionException) {
                val cause = e.cause
                if (cause is HttpResponseException) cause.response
                else throw e
            }

        return convertJettyResponse(httpClient, jettyClient, response)
    }

    override fun ws(
        path: String,
        onConnect: WsSession.() -> Unit,
        onBinary: WsSession.(data: ByteArray) -> Unit,
        onText: WsSession.(text: String) -> Unit,
        onPing: WsSession.(data: ByteArray) -> Unit,
        onPong: WsSession.(data: ByteArray) -> Unit,
        onClose: WsSession.(status: Int, reason: String) -> Unit,
    ): WsSession {

        val baseUri = httpClient.settings.baseUri ?: URI(path)
        val scheme = if (baseUri.scheme.lowercase() == "https") "wss" else "ws"
        val uri = URI("$scheme://${baseUri.host}:${baseUri.port}${baseUri.path}$path")
        val adapter =
            HelidonWebSocketAdapter(uri, onConnect, onBinary, onText, onPing, onPong, onClose)
        val session = wsClient.connect(adapter, uri).get()

        return HelidonClientWsSession(uri, session)
    }

    override fun sse(request: HttpRequestPort): Publisher<ServerEvent> {
        val clientPublisher = SubmissionPublisher<ServerEvent>(publisherExecutor, Int.MAX_VALUE)

        val sseRequest = request.with(
            accept = listOf(ContentType(TEXT_EVENT_STREAM)),
            headers = request.headers + Header("connection", "keep-alive")
        )

        createJettyRequest(jettyClient, sseRequest)
            .onResponseBegin {
                if (it.status !in 200 until 300)
                    error("Invalid response: ${it.status}")
            }
            .onResponseContent { _, content ->
                val sb = StringBuilder()
                while (content.hasRemaining())
                    sb.append(Char(content.get().toInt()))

                val evt = sb
                    .trim()
                    .lines()
                    .map { it.split(":") }
                    .associate { it.first().trim().lowercase() to it.last().trim() }
                    .let { ServerEvent(it["event"], it["data"], it["id"], it["retry"]?.toLong()) }

                clientPublisher.submit(evt)
            }
            .send {
                clientPublisher.close()
            }

        return clientPublisher
    }

    override fun supportedFeatures(): Set<HttpFeature> =
        setOf(ZIP, COOKIES, MULTIPART, SSE, WEBSOCKETS)

    override fun supportedProtocols(): Set<HttpProtocol> =
        setOf(HTTP, HTTPS, HttpProtocol.HTTP2)

    private fun convertJettyResponse(
        adapterHttpClient: HttpClient, adapterJettyClient: JettyClient, response: Response
    ): HttpResponse {

        val bodyString = if (response is ContentResponse) response.contentAsString else ""

        if (httpSettings.useCookies)
            adapterHttpClient.cookies = adapterJettyClient.httpCookieStore.all().map {
                Cookie(
                    it.name,
                    it.value,
                    it.maxAge,
                    it.isSecure,
                    it.path,
                    it.isHttpOnly,
                    it.domain,
                    it.attributes["SameSite"]?.uppercase()?.let(CookieSameSite::valueOf),
                    expires = it.expires,
                )
            }

        return HttpResponse(
            body = bodyString,
            headers = convertHeaders(response.headers),
            contentType = response.headers["content-type"]?.let { parseContentType(it) },
            cookies = adapterHttpClient.cookies,
            status = response.status,
            contentLength = bodyString.length.toLong(),
        )
    }

    private fun convertHeaders(headers: JettyHttpFields): Headers =
        Headers(
            headers
                .fieldNamesCollection
                .map { it.lowercase() }
                .flatMap { h -> headers.getValuesList(h).map { Header(h, it) } }
        )

    private fun createJettyRequest(
        adapterJettyClient: JettyClient, request: HttpRequestPort
    ): Request {

        // TODO Remove these fields and handle them as headers
        val contentType = request.contentType
        val authorization = request.authorization
        val baseUri = httpSettings.baseUri

        if (httpSettings.useCookies) {
            val uri = baseUri ?: request.uri()
            addCookies(uri, adapterJettyClient.httpCookieStore, request.cookies)
        }

        val jettyRequest = adapterJettyClient
            .newRequest(URI((baseUri?.toString() ?: "") + request.path))
            .method(HttpMethod.valueOf(request.method.toString()))
            .headers {
                it.remove("accept-encoding") // Don't send encoding by default
                if (contentType != null)
                    it.put("content-type", contentType.text)
                if (authorization != null)
                    it.put("authorization", authorization.text)
                request.headers.all.forEach { (k, v) ->
                    v.map(HttpField::text).forEach { s -> it.add(k, s)}
                }
            }
            .body(createBody(request))
            .accept(*request.accept.map { it.text }.toTypedArray())

        request.queryParameters.all
            .forEach { (k, v) -> v.forEach { jettyRequest.param(k, it.text) } }

        return jettyRequest
    }

    private fun createBody(request: HttpRequestPort): Request.Content {

        if (request.parts.isEmpty() && request.formParameters.isEmpty())
            return BytesRequestContent(bodyToBytes(request.body))

        val multiPart = MultiPartRequestContent()

        request.parts.forEach { p ->
            if (p.submittedFileName == null)
                // TODO Add content type if present
                multiPart.addPart(
                    ContentSourcePart(p.name, null, EMPTY, StringRequestContent(p.bodyString()))
                )
            else
                multiPart.addPart(
                    ContentSourcePart(
                        p.name,
                        p.submittedFileName,
                        EMPTY,
                        BytesRequestContent(bodyToBytes(p.body)),
                    )
                )
        }

        request.formParameters.fields.forEach {
            val part = ContentSourcePart(it.name, null, EMPTY, StringRequestContent(it.text))
            multiPart.addPart(part)
        }

        multiPart.close()

        return multiPart
    }

    private fun addCookies(uri: URI, store: HttpCookieStore, cookies: List<Cookie>) {
        cookies.forEach {
            val httpCookie = java.net.HttpCookie(it.name, it.value)
            httpCookie.secure = it.secure
            httpCookie.maxAge = it.maxAge
            httpCookie.path = it.path
            httpCookie.isHttpOnly = it.httpOnly
            it.domain?.let(httpCookie::setDomain)

            val from = HttpCookie.build(httpCookie).expires(it.expires)

            it.sameSite?.let { ss ->
                when(ss){
                    STRICT -> SameSite.STRICT
                    LAX -> SameSite.LAX
                    NONE -> SameSite.NONE
                }
            }?.let { ss -> from.sameSite(ss) }

            store.add(uri, from.build())
        }
    }

    private fun sslContext(settings: HttpClientSettings): ClientSslContextFactory =
        when {
            settings.insecure ->
                ClientSslContextFactory().apply { isTrustAll = true }

            settings.sslSettings != null -> {
                val sslSettings = settings.sslSettings ?: error("SSL settings cannot be 'null'")
                val keyStore = sslSettings.keyStore
                val trustStore = sslSettings.trustStore
                val sslContextBuilder = ClientSslContextFactory()

                if (keyStore != null) {
                    val store = loadKeyStore(keyStore, sslSettings.keyStorePassword)
                    sslContextBuilder.keyStore = store
                    sslContextBuilder.keyStorePassword = sslSettings.keyStorePassword
                }

                if (trustStore != null) {
                    val store = loadKeyStore(trustStore, sslSettings.trustStorePassword)
                    sslContextBuilder.trustStore = store
                    sslContextBuilder.setTrustStorePassword(sslSettings.trustStorePassword)
                }

                sslContextBuilder
            }

            else ->
                ClientSslContextFactory()
        }
}
