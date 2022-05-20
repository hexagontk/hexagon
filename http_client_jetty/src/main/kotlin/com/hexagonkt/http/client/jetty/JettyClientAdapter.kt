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
import com.hexagonkt.http.model.Header
import com.hexagonkt.http.model.HttpCookie
import com.hexagonkt.http.model.HttpStatus
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
import java.net.CookieStore
import java.net.URI
import java.util.concurrent.ExecutionException
import com.hexagonkt.http.model.HttpFields as HxHttpFields
import org.eclipse.jetty.util.ssl.SslContextFactory.Client as ClientSslContextFactory
import org.eclipse.jetty.client.HttpClient as JettyHttpClient

/**
 * Client to use other REST services.
 */
class JettyClientAdapter : HttpClientPort {

    private var jettyClient: JettyHttpClient? = null
    private var httpClient: HttpClient? = null

    override fun startUp(client: HttpClient) {
        val clientConnector = ClientConnector()
        clientConnector.sslContextFactory = sslContext(client.settings)
        val clientInstance = JettyHttpClient(HttpClientTransportDynamic(clientConnector))

        jettyClient = clientInstance
        httpClient = client

        clientInstance.userAgentField = null // Disable default user agent header
        clientInstance.start()
    }

    override fun shutDown() {
        jettyClient?.stop()
            ?: error("'null' Jetty HTTP client: Client *MUST BE STARTED* before shut-down")
    }

    override fun send(request: HttpClientRequest): HttpClientResponse {

        val adapterHttpClient = httpClient
            ?: error("'null' HTTP client: Client *MUST BE STARTED* before sending requests")
        val adapterJettyClient = jettyClient
            ?: error("'null' Jetty HTTP client: Client *MUST BE STARTED* before sending requests")

        val response =
            try {
                createJettyRequest(adapterHttpClient, adapterJettyClient, request).send()
            }
            catch (e: ExecutionException) {
                val cause = e.cause
                if (cause is HttpResponseException) cause.response
                else throw e
            }

        return convertJettyResponse(adapterHttpClient, adapterJettyClient, response)
    }

    private fun convertJettyResponse(
        adapterHttpClient: HttpClient, adapterJettyClient: JettyHttpClient, response: Response
    ): HttpClientResponse {

        val bodyString = if (response is ContentResponse) response.contentAsString else ""
        val settings = adapterHttpClient.settings

        if (settings.useCookies)
            adapterHttpClient.cookies = adapterJettyClient.cookieStore.cookies.map {
                HttpCookie(it.name, it.value, it.maxAge, it.secure)
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

    private fun convertHeaders(headers: HttpFields): HxHttpFields<Header> =
        HxHttpFields(
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

        if (settings.useCookies)
            addCookies(adapterHttpClient, adapterJettyClient.cookieStore, request.cookies)

        val jettyRequest = adapterJettyClient
            .newRequest(URI(settings.baseUrl.toString() + request.path))
            .method(HttpMethod.valueOf(request.method.toString()))
            .headers {
                it.remove("accept-encoding") // Don't send encoding by default
                if (contentType != null)
                    it.put("content-type", contentType.text)
                (settings.headers + request.headers).allValues.forEach { (k, v) -> it.put(k, v) }
            }
            .body(createBody(request))
            .accept(*request.accept.map { it.text }.toTypedArray())

        request.queryParameters.allPairs.forEach { (k, v) -> jettyRequest.param(k, v) }

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
            .allPairs
            .forEach { (k, v) ->
                // TODO Add content type if present
                multiPart.addFieldPart(k, StringRequestContent(v), EMPTY)
            }

        multiPart.close()

        return multiPart
    }

    private fun addCookies(client: HttpClient, store: CookieStore, cookies: List<HttpCookie>) {
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
