package com.hexagonkt.http.client.jetty

import com.hexagonkt.core.media.TEXT_EVENT_STREAM
import com.hexagonkt.core.security.loadKeyStore
import com.hexagonkt.http.handlers.bodyToBytes
import com.hexagonkt.http.CHECKED_HEADERS
import com.hexagonkt.http.client.HttpClient
import com.hexagonkt.http.client.HttpClientPort
import com.hexagonkt.http.client.HttpClientSettings
import com.hexagonkt.http.model.HttpResponse
import com.hexagonkt.http.model.*
import com.hexagonkt.http.model.CookieSameSite.*
import com.hexagonkt.http.model.ws.WsSession
import com.hexagonkt.http.parseContentType
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
import org.eclipse.jetty.http.HttpFields
import org.eclipse.jetty.http.HttpFields.EMPTY
import org.eclipse.jetty.http.HttpMethod
import org.eclipse.jetty.http.MultiPart.ContentSourcePart
import org.eclipse.jetty.io.ClientConnector
import java.lang.StringBuilder
import java.lang.UnsupportedOperationException
import java.net.URI
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.Flow.Publisher
import java.util.concurrent.SubmissionPublisher
import org.eclipse.jetty.http2.client.HTTP2Client as JettyHttp2Client
import org.eclipse.jetty.http2.client.transport.ClientConnectionFactoryOverHTTP2.HTTP2
import org.eclipse.jetty.client.HttpClient as JettyHttpClient
import org.eclipse.jetty.util.ssl.SslContextFactory.Client as ClientSslContextFactory

/**
 * Client to use other REST services.
 */
open class JettyClientAdapter : HttpClientPort {

    protected lateinit var jettyClient: JettyHttpClient
    protected lateinit var httpClient: HttpClient
    private lateinit var httpSettings: HttpClientSettings
    private var started: Boolean = false
    private val publisherExecutor = Executors.newSingleThreadExecutor()

    override fun startUp(client: HttpClient) {
        val clientConnector = ClientConnector()
        val settings = client.settings
        clientConnector.sslContextFactory = sslContext(settings)

        val http2 = HTTP2(JettyHttp2Client(clientConnector))
        val transport = HttpClientTransportDynamic(clientConnector, HTTP11, http2)

        jettyClient = JettyHttpClient(transport)
        httpClient = client
        httpSettings = settings

        jettyClient.userAgentField = null // Disable default user agent header
        jettyClient.isFollowRedirects = settings.followRedirects
        jettyClient.start()
        started = true
    }

    override fun shutDown() {
        jettyClient.stop()
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
        throw UnsupportedOperationException("WebSockets not supported. Use 'http_client_jetty_ws")
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

    private fun convertJettyResponse(
        adapterHttpClient: HttpClient, adapterJettyClient: JettyHttpClient, response: Response
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
            status = HttpStatus(response.status),
            contentLength = bodyString.length.toLong(),
        )
    }

    private fun convertHeaders(headers: HttpFields): Headers =
        Headers(
            headers
                .fieldNamesCollection
                .map { it.lowercase() }
                .filter { it !in CHECKED_HEADERS }
                .map { Header(it, headers.getValuesList(it)) }
        )

    private fun createJettyRequest(
        adapterJettyClient: JettyHttpClient, request: HttpRequestPort
    ): Request {

        val contentType = request.contentType
        val authorization = request.authorization
        val baseUrl = httpSettings.baseUrl

        if (httpSettings.useCookies) {
            val uri = (baseUrl ?: request.url()).toURI()
            addCookies(uri, adapterJettyClient.httpCookieStore, request.cookies)
        }

        val jettyRequest = adapterJettyClient
            .newRequest(URI((baseUrl?.toString() ?: "") + request.path))
            .method(HttpMethod.valueOf(request.method.toString()))
            .headers {
                it.remove("accept-encoding") // Don't send encoding by default
                if (contentType != null)
                    it.put("content-type", contentType.text)
                if (authorization != null)
                    it.put("authorization", authorization.text)
                request.headers.values.forEach { (k, v) ->
                    v.map(Any::toString).forEach { s -> it.add(k, s)}
                }
            }
            .body(createBody(request))
            .accept(*request.accept.map { it.text }.toTypedArray())

        request.queryParameters
            .forEach { (k, v) -> v.strings().forEach { jettyRequest.param(k, it) } }

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

        request.formParameters
            .forEach { (k, v) ->
                v.strings().forEach {
                    multiPart.addPart(ContentSourcePart(k, null, EMPTY, StringRequestContent(it)))
                }
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
