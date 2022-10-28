package com.hexagonkt.http.client.jetty

import com.hexagonkt.core.fail
import com.hexagonkt.core.security.loadKeyStore
import com.hexagonkt.http.bodyToBytes
import com.hexagonkt.http.checkedHeaders
import com.hexagonkt.http.client.HttpClient
import com.hexagonkt.http.client.HttpClientPort
import com.hexagonkt.http.client.HttpClientSettings
import com.hexagonkt.http.client.model.HttpClientRequest
import com.hexagonkt.http.client.model.HttpClientResponse
import com.hexagonkt.http.model.Cookie
import com.hexagonkt.http.model.Header
import com.hexagonkt.http.model.Headers
import com.hexagonkt.http.model.HttpStatus
import com.hexagonkt.http.model.ws.WsCloseStatus
import com.hexagonkt.http.model.ws.WsSession
import com.hexagonkt.http.parseContentType
import org.eclipse.jetty.client.HttpResponseException
import org.eclipse.jetty.client.api.ContentResponse
import org.eclipse.jetty.client.api.Request
import org.eclipse.jetty.client.api.Response
import org.eclipse.jetty.client.dynamic.HttpClientTransportDynamic
import org.eclipse.jetty.client.util.BytesRequestContent
import org.eclipse.jetty.client.util.MultiPartRequestContent
import org.eclipse.jetty.client.util.StringRequestContent
import org.eclipse.jetty.http.HttpFields
import org.eclipse.jetty.http.HttpFields.EMPTY
import org.eclipse.jetty.http.HttpMethod
import org.eclipse.jetty.io.ClientConnector
import org.eclipse.jetty.websocket.client.WebSocketClient
import java.net.CookieStore
import java.net.URI
import java.util.concurrent.ExecutionException
import org.eclipse.jetty.client.HttpClient as JettyHttpClient
import org.eclipse.jetty.util.ssl.SslContextFactory.Client as ClientSslContextFactory

/**
 * Client to use other REST services.
 */
class JettyClientAdapter : HttpClientPort {

    private lateinit var jettyClient: JettyHttpClient
    private lateinit var httpClient: HttpClient
    private val wsClient = WebSocketClient()
    private var started: Boolean = false

    override fun startUp(client: HttpClient) {
        val clientConnector = ClientConnector()
        clientConnector.sslContextFactory = sslContext(client.settings)
        val clientInstance = JettyHttpClient(HttpClientTransportDynamic(clientConnector))

        jettyClient = clientInstance
        httpClient = client

        clientInstance.userAgentField = null // Disable default user agent header
        clientInstance.start()
        wsClient.start()
        started = true
    }

    override fun shutDown() {
        check(started) { "HTTP client *MUST BE STARTED* before shut-down" }
        jettyClient.stop()
        started = false
    }

    override fun send(request: HttpClientRequest): HttpClientResponse {
        check(started) { "HTTP client *MUST BE STARTED* before sending requests" }

        val response =
            try {
                createJettyRequest(httpClient, jettyClient, request).send()
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
        onClose: WsSession.(status: WsCloseStatus, reason: String) -> Unit,
    ): WsSession {

        check(started) { "HTTP client *MUST BE STARTED* before connecting to WS" }

        val baseUrl = httpClient.settings.baseUrl
        val scheme = if (baseUrl.protocol.lowercase() == "https") "wss" else "ws"
        val uri = URI("$scheme://${baseUrl.host}:${baseUrl.port}${baseUrl.path}$path")
        val adapter = JettyWebSocketAdapter(onConnect, onBinary, onText, onClose)
        val session = wsClient.connect(adapter, uri).get()

        return JettyClientWsSession(session)
    }

    private fun convertJettyResponse(
        adapterHttpClient: HttpClient, adapterJettyClient: JettyHttpClient, response: Response
    ): HttpClientResponse {

        val bodyString = if (response is ContentResponse) response.contentAsString else ""
        val settings = adapterHttpClient.settings

        if (settings.useCookies)
            adapterHttpClient.cookies = adapterJettyClient.cookieStore.cookies.map {
                Cookie(it.name, it.value, it.maxAge, it.secure)
            }

        return HttpClientResponse(
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
                .filter { it !in checkedHeaders }
                .map { Header(it, headers.getValuesList(it)) }
        )

    private fun createJettyRequest(
        adapterHttpClient: HttpClient,
        adapterJettyClient: JettyHttpClient,
        request: HttpClientRequest
    ): Request {

        val settings = adapterHttpClient.settings
        val contentType = request.contentType ?: settings.contentType
        val authorization = request.authorization ?: settings.authorization

        if (settings.useCookies)
            addCookies(adapterHttpClient, adapterJettyClient.cookieStore, request.cookies)

        val jettyRequest = adapterJettyClient
            .newRequest(URI(settings.baseUrl.toString() + request.path))
            .method(HttpMethod.valueOf(request.method.toString()))
            .headers {
                it.remove("accept-encoding") // Don't send encoding by default
                if (contentType != null)
                    it.put("content-type", contentType.text)
                if (authorization != null)
                    it.put("authorization", authorization.text)
                (settings.headers + request.headers).values.forEach { (k, v) -> it.put(k, v) }
            }
            .body(createBody(request))
            .accept(*request.accept.map { it.text }.toTypedArray())

        request.queryParameters
            .forEach { (k, v) ->
                v.values.forEach { jettyRequest.param(k, it) }
            }

        return jettyRequest
    }

    private fun createBody(request: HttpClientRequest): Request.Content {

        if (request.parts.isEmpty() && request.formParameters.isEmpty())
            return BytesRequestContent(bodyToBytes(request.body))

        val multiPart = MultiPartRequestContent()

        request.parts.forEach { p ->
            if (p.submittedFileName == null)
                // TODO Add content type if present
                multiPart.addFieldPart(p.name, StringRequestContent(p.bodyString()), EMPTY)
            else
                multiPart.addFilePart(
                    p.name,
                    p.submittedFileName,
                    BytesRequestContent(bodyToBytes(p.body)),
                    EMPTY
                )
        }

        request.formParameters
            .forEach { (k, v) ->
                v.values.forEach { multiPart.addFieldPart(k, StringRequestContent(it), EMPTY) }
            }

        multiPart.close()

        return multiPart
    }

    private fun addCookies(client: HttpClient, store: CookieStore, cookies: List<Cookie>) {
        val uri = client.settings.baseUrl.toURI()

        cookies.forEach {
            val httpCookie = java.net.HttpCookie(it.name, it.value)
            httpCookie.secure = it.secure
            httpCookie.maxAge = it.maxAge
            store.add(uri, httpCookie)
        }
    }

    private fun sslContext(settings: HttpClientSettings): ClientSslContextFactory =
        when {
            settings.insecure ->
                ClientSslContextFactory().apply { isTrustAll = true }

            settings.sslSettings != null -> {
                val sslSettings = settings.sslSettings ?: fail
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
