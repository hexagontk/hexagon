package com.hexagonkt.http.client.java

import com.hexagonkt.http.client.HttpClient
import com.hexagonkt.http.client.HttpClientPort
import com.hexagonkt.http.client.HttpClientSettings
import com.hexagonkt.http.model.HttpResponse
import com.hexagonkt.http.model.*
import com.hexagonkt.http.model.ws.WsSession
import java.lang.UnsupportedOperationException
import java.net.http.HttpClient.Redirect.ALWAYS
import java.net.http.HttpClient.Redirect.NEVER
import java.util.concurrent.Flow.Publisher
import java.net.http.HttpClient as JavaHttpClient

/**
 * Client to use other REST services.
 */
open class JavaClientAdapter : HttpClientPort {

    protected lateinit var jettyClient: JavaHttpClient
    protected lateinit var httpClient: HttpClient
    private lateinit var httpSettings: HttpClientSettings
    private var started: Boolean = false

    override fun startUp(client: HttpClient) {
        val settings = client.settings

        httpClient = client
        httpSettings = settings
        jettyClient = JavaHttpClient
            .newBuilder()
            .followRedirects(if (settings.followRedirects) ALWAYS else NEVER)
//            .sslContext(sslContext(settings))
            .build()

        started = true
    }

    override fun shutDown() {
        started = false
    }

    override fun started() =
        started

    override fun send(request: HttpRequestPort): HttpResponsePort {
//        val response =
//            try {
//                createJettyRequest(jettyClient, request).send()
//            }
//            catch (e: ExecutionException) {
//                val cause = e.cause
//                if (cause is HttpResponseException) cause.response
//                else throw e
//            }
//
//        return convertJettyResponse(httpClient, jettyClient, response)
        return HttpResponse()
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
        throw UnsupportedOperationException("WebSockets not supported")
    }

    override fun sse(request: HttpRequestPort): Publisher<ServerEvent> {
        throw UnsupportedOperationException("SSE not supported")
    }

//    private fun convertJettyResponse(
//        adapterHttpClient: HttpClient, adapterJettyClient: JettyHttpClient, response: Response
//    ): HttpResponse {
//
//        val bodyString = if (response is ContentResponse) response.contentAsString else ""
//
//        if (httpSettings.useCookies)
//            adapterHttpClient.cookies = adapterJettyClient.httpCookieStore.all().map {
//                Cookie(
//                    it.name,
//                    it.value,
//                    it.maxAge,
//                    it.isSecure,
//                    it.path,
//                    it.isHttpOnly,
//                    it.domain,
//                    it.attributes["SameSite"]?.uppercase()?.let(CookieSameSite::valueOf),
//                    expires = it.expires,
//                )
//            }
//
//        return HttpResponse(
//            body = bodyString,
//            headers = convertHeaders(response.headers),
//            contentType = response.headers["content-type"]?.let { parseContentType(it) },
//            cookies = adapterHttpClient.cookies,
//            status = HttpStatus(response.status),
//            contentLength = bodyString.length.toLong(),
//        )
//    }
//
//    private fun convertHeaders(headers: HttpFields): Headers =
//        Headers(
//            headers
//                .fieldNamesCollection
//                .map { it.lowercase() }
//                .filter { it !in CHECKED_HEADERS }
//                .map { Header(it, headers.getValuesList(it)) }
//        )
//
//    private fun createJettyRequest(
//        adapterJettyClient: JettyHttpClient, request: HttpRequestPort
//    ): Request {
//
//        val contentType = request.contentType
//        val authorization = request.authorization
//        val baseUrl = httpSettings.baseUrl
//
//        if (httpSettings.useCookies) {
//            val uri = (baseUrl ?: request.url()).toURI()
//            addCookies(uri, adapterJettyClient.httpCookieStore, request.cookies)
//        }
//
//        val jettyRequest = adapterJettyClient
//            .newRequest(URI((baseUrl?.toString() ?: "") + request.path))
//            .method(HttpMethod.valueOf(request.method.toString()))
//            .headers {
//                it.remove("accept-encoding") // Don't send encoding by default
//                if (contentType != null)
//                    it.put("content-type", contentType.text)
//                if (authorization != null)
//                    it.put("authorization", authorization.text)
//                request.headers.values.forEach { (k, v) ->
//                    v.map(Any::toString).forEach { s -> it.add(k, s)}
//                }
//            }
//            .body(createBody(request))
//            .accept(*request.accept.map { it.text }.toTypedArray())
//
//        request.queryParameters
//            .forEach { (k, v) -> v.strings().forEach { jettyRequest.param(k, it) } }
//
//        return jettyRequest
//    }
//
//    private fun createBody(request: HttpRequestPort): Request.Content {
//
//        if (request.parts.isEmpty() && request.formParameters.isEmpty())
//            return BytesRequestContent(bodyToBytes(request.body))
//
//        val multiPart = MultiPartRequestContent()
//
//        request.parts.forEach { p ->
//            if (p.submittedFileName == null)
//                // TODO Add content type if present
//                multiPart.addPart(
//                    ContentSourcePart(p.name, null, EMPTY, StringRequestContent(p.bodyString()))
//                )
//            else
//                multiPart.addPart(
//                    ContentSourcePart(
//                        p.name,
//                        p.submittedFileName,
//                        EMPTY,
//                        BytesRequestContent(bodyToBytes(p.body)),
//                    )
//                )
//        }
//
//        request.formParameters
//            .forEach { (k, v) ->
//                v.strings().forEach {
//                    multiPart.addPart(ContentSourcePart(k, null, EMPTY, StringRequestContent(it)))
//                }
//            }
//
//        multiPart.close()
//
//        return multiPart
//    }
//
//    private fun addCookies(uri: URI, store: HttpCookieStore, cookies: List<Cookie>) {
//        cookies.forEach {
//            val httpCookie = java.net.HttpCookie(it.name, it.value)
//            httpCookie.secure = it.secure
//            httpCookie.maxAge = it.maxAge
//            httpCookie.path = it.path
//            httpCookie.isHttpOnly = it.httpOnly
//            it.domain?.let(httpCookie::setDomain)
//
//            val from = HttpCookie.build(httpCookie).expires(it.expires)
//
//            it.sameSite?.let { ss ->
//                when(ss){
//                    STRICT -> SameSite.STRICT
//                    LAX -> SameSite.LAX
//                    NONE -> SameSite.NONE
//                }
//            }?.let { ss -> from.sameSite(ss) }
//
//            store.add(uri, from.build())
//        }
//    }
//
//    private fun sslContext(settings: HttpClientSettings): SSLContext =
//        when {
//            settings.insecure ->
//                ClientSslContextFactory().apply { isTrustAll = true }
//
//            settings.sslSettings != null -> {
//                val sslSettings = settings.sslSettings ?: error("SSL settings cannot be 'null'")
//                val keyStore = sslSettings.keyStore
//                val trustStore = sslSettings.trustStore
//                val sslContextBuilder = ClientSslContextFactory()
//
//                if (keyStore != null) {
//                    val store = loadKeyStore(keyStore, sslSettings.keyStorePassword)
//                    sslContextBuilder.keyStore = store
//                    sslContextBuilder.keyStorePassword = sslSettings.keyStorePassword
//                }
//
//                if (trustStore != null) {
//                    val store = loadKeyStore(trustStore, sslSettings.trustStorePassword)
//                    sslContextBuilder.trustStore = store
//                    sslContextBuilder.setTrustStorePassword(sslSettings.trustStorePassword)
//                }
//
//                sslContextBuilder
//            }
//
//            else ->
//                ClientSslContextFactory()
//        }
}
