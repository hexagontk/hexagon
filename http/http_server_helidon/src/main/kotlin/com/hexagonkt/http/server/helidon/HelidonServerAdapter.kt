package com.hexagonkt.http.server.helidon

import com.hexagonkt.core.fieldsMapOf
import com.hexagonkt.core.security.loadKeyStore
import com.hexagonkt.core.toText
import com.hexagonkt.http.SslSettings
import com.hexagonkt.http.handlers.bodyToBytes
import com.hexagonkt.http.handlers.HttpHandler
import com.hexagonkt.http.model.HttpProtocol
import com.hexagonkt.http.model.HttpProtocol.*
import com.hexagonkt.http.model.HttpResponse
import com.hexagonkt.http.model.HttpResponsePort
import com.hexagonkt.http.server.HttpServer
import com.hexagonkt.http.server.HttpServerFeature
import com.hexagonkt.http.server.HttpServerFeature.ZIP
import com.hexagonkt.http.server.HttpServerPort
import io.helidon.http.Method
import io.helidon.http.Status
import io.helidon.http.HeaderNames
import io.helidon.http.HttpMediaType
import io.helidon.http.SetCookie
import io.helidon.webserver.WebServer
import io.helidon.webserver.http.ServerResponse
import java.security.SecureRandom
import java.time.Duration
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLParameters
import javax.net.ssl.TrustManagerFactory

/**
 * Implements [HttpServerPort] using Helidon.
 */
class HelidonServerAdapter : HttpServerPort {

    private companion object {
        const val START_ERROR_MESSAGE = "Helidon server not started correctly"
    }

    private var helidonServer: WebServer? = null

    override fun runtimePort(): Int {
        return helidonServer?.port() ?: error(START_ERROR_MESSAGE)
    }

    override fun started() =
        helidonServer?.isRunning ?: false

    override fun startUp(server: HttpServer) {
        val settings = server.settings
        val sslSettings = settings.sslSettings

        val handlers: Map<Method, HttpHandler> =
            server.handler.addPrefix(settings.contextPath)
                .byMethod()
                .mapKeys { Method.create(it.key.toString()) }

        val serverBuilder = WebServer
            .builder()
            .host(settings.bindAddress.hostName)
            .port(settings.bindPort)
            .routing {
                it.any({ helidonRequest, helidonResponse ->
                    val method = helidonRequest.prologue().method()
                    val request = HelidonRequestAdapter(method, helidonRequest)
                    val response = handlers[method]?.process(request)?.response ?: HttpResponse()
                    setResponse(request.protocol.secure, response, helidonResponse)
                })
            }

        if (sslSettings != null)
            serverBuilder.tls {
                val sslClientAuth = sslSettings.clientAuth
                it
                    .sslParameters(SSLParameters().apply { needClientAuth = sslClientAuth })
                    .sslContext(sslContext(sslSettings))
            }

        helidonServer = serverBuilder.build()

        helidonServer?.start() ?: error(START_ERROR_MESSAGE)
    }

    override fun shutDown() {
        helidonServer?.stop() ?: error(START_ERROR_MESSAGE)
    }

    override fun supportedProtocols(): Set<HttpProtocol> =
        setOf(HTTP, HTTPS, HTTP2)

    override fun supportedFeatures(): Set<HttpServerFeature> =
        setOf(ZIP)

    override fun options(): Map<String, *> =
        fieldsMapOf<HelidonServerAdapter>()

    private fun setResponse(
        secureRequest: Boolean,
        response: HttpResponsePort,
        helidonResponse: ServerResponse
    ) {
        try {
            helidonResponse.status(Status.create(response.status.code))

            response.headers.values.forEach {
                helidonResponse.header(HeaderNames.create(it.name), *it.strings().toTypedArray())
            }

            val headers = helidonResponse.headers()
            response.cookies
                .filter { if (secureRequest) true else !it.secure }
                .forEach {
                    val cookie = SetCookie
                        .builder(it.name, it.value)
                        .maxAge(Duration.ofSeconds(it.maxAge))
                        .path(it.path)
                        .httpOnly(it.httpOnly)
                        .secure(it.secure)

                    if (it.expires != null)
                        cookie.expires(it.expires)

                    if (it.deleted)
                        headers.clearCookie(it.name)
                    else
                        headers.addCookie(cookie.build())
                }

            response.contentType?.let { ct -> headers.contentType(HttpMediaType.create(ct.text)) }

            helidonResponse.send(bodyToBytes(response.body))
        }
        catch (e: Exception) {
            helidonResponse.status(Status.INTERNAL_SERVER_ERROR_500)
            helidonResponse.send(bodyToBytes(e.toText()))
        }
    }

    private fun sslContext(sslSettings: SslSettings): SSLContext {
        val keyManager = createKeyManagerFactory(sslSettings)
        val trustManager = createTrustManagerFactory(sslSettings)

        val eng = SSLContext.getDefault().createSSLEngine()
        eng.needClientAuth = sslSettings.clientAuth
        val context = SSLContext.getInstance("TLSv1.3")
        context.init(
            keyManager.keyManagers,
            trustManager?.trustManagers ?: emptyArray(),
            SecureRandom.getInstanceStrong()
        )
        return context
    }

    private fun createTrustManagerFactory(sslSettings: SslSettings): TrustManagerFactory? {
        val trustStoreUrl = sslSettings.trustStore ?: return null

        val trustStorePassword = sslSettings.trustStorePassword
        val trustStore = loadKeyStore(trustStoreUrl, trustStorePassword)
        val trustAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
        val trustManager = TrustManagerFactory.getInstance(trustAlgorithm)

        trustManager.init(trustStore)
        return trustManager
    }

    private fun createKeyManagerFactory(sslSettings: SslSettings): KeyManagerFactory {
        val keyStoreUrl = sslSettings.keyStore ?: error("")
        val keyStorePassword = sslSettings.keyStorePassword
        val keyStore = loadKeyStore(keyStoreUrl, keyStorePassword)
        val keyManager = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        keyManager.init(keyStore, keyStorePassword.toCharArray())
        return keyManager
    }
}
