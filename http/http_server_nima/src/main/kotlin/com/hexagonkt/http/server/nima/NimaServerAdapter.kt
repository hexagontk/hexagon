package com.hexagonkt.http.server.nima

import com.hexagonkt.core.fieldsMapOf
import com.hexagonkt.core.security.loadKeyStore
import com.hexagonkt.core.toText
import com.hexagonkt.http.SslSettings
import com.hexagonkt.http.bodyToBytes
import com.hexagonkt.http.handlers.HttpHandler
import com.hexagonkt.http.model.HttpProtocol
import com.hexagonkt.http.model.HttpProtocol.*
import com.hexagonkt.http.model.HttpResponse
import com.hexagonkt.http.model.HttpResponsePort
import com.hexagonkt.http.server.HttpServer
import com.hexagonkt.http.server.HttpServerFeature
import com.hexagonkt.http.server.HttpServerFeature.ZIP
import com.hexagonkt.http.server.HttpServerPort
import io.helidon.common.http.Http.*
import io.helidon.common.http.HttpMediaType
import io.helidon.common.http.SetCookie
import io.helidon.common.http.SetCookie.SameSite.NONE
import io.helidon.common.http.SetCookie.SameSite.STRICT
import io.helidon.nima.common.tls.Tls
import io.helidon.nima.http.media.multipart.MultiPartSupport
import io.helidon.nima.webserver.WebServer
import io.helidon.nima.webserver.http.ServerResponse
import java.security.SecureRandom
import java.time.Duration
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLParameters
import javax.net.ssl.TrustManagerFactory

/**
 * Implements [HttpServerPort] using Helidon Nima.
 */
class NimaServerAdapter : HttpServerPort {

    private companion object {
        const val startErrorMessage = "Nima server not started correctly"
    }

    private var nimaServer: WebServer? = null

    override fun runtimePort(): Int {
        return nimaServer?.port() ?: error(startErrorMessage)
    }

    override fun started() =
        nimaServer?.isRunning ?: false

    override fun startUp(server: HttpServer) {
        val settings = server.settings
        val sslSettings = settings.sslSettings

        val handlers: Map<Method, HttpHandler> =
            server.handler.addPrefix(settings.contextPath)
                .byMethod()
                .mapKeys { Method.create(it.key.toString()) }

        nimaServer = WebServer
            .builder()
            .host(settings.bindAddress.hostName)
            .port(settings.bindPort)
            .addMediaSupport(MultiPartSupport.create(io.helidon.common.config.Config.empty()))
            .defaultSocket {
                val b = it
                    .port(settings.bindPort)
                    .host(settings.bindAddress.hostName)
                    .backlog(8192)

                if (sslSettings == null)
                    b
                else
                    b.tls(
                        Tls.builder()
                            .sslParameters(
                                SSLParameters().apply { needClientAuth = sslSettings.clientAuth }
                            )
                            .sslContext(sslContext(sslSettings))
                            .build()
                    )
            }
            .routing {
                it.any({ nimaRequest, nimaResponse ->
                    val method = nimaRequest.prologue().method()
                    val request = NimaRequestAdapter(method, nimaRequest)
                    val response = handlers[method]?.process(request)?.response ?: HttpResponse()
                    setResponse(response, nimaResponse)
                })
            }
            .build()

        nimaServer?.start() ?: error(startErrorMessage)
    }

    override fun shutDown() {
        nimaServer?.stop() ?: error(startErrorMessage)
    }

    override fun supportedProtocols(): Set<HttpProtocol> =
        setOf(HTTP, HTTPS, HTTP2)

    override fun supportedFeatures(): Set<HttpServerFeature> =
        setOf(ZIP)

    override fun options(): Map<String, *> =
        fieldsMapOf<NimaServerAdapter>()

    private fun setResponse(response: HttpResponsePort, nimaResponse: ServerResponse) {
        try {
            nimaResponse.status(Status.create(response.status.code))

            response.headers.values.forEach {
                nimaResponse.header(Header.create(Header.create(it.name), it.strings()))
            }

            val headers = nimaResponse.headers()
            response.cookies.forEach {
                val cookie = SetCookie
                    .builder(it.name, it.value)
                    .maxAge(Duration.ofSeconds(it.maxAge))
                    .path(it.path)
                    .domain(it.domain)
                    .httpOnly(it.httpOnly)
                    .sameSite(if (it.sameSite) STRICT else NONE)
                    .secure(it.secure)

                if (it.expires != null)
                    cookie.expires(it.expires)

                headers.addCookie(cookie.build())
            }

            response.contentType?.let { ct -> headers.contentType(HttpMediaType.create(ct.text)) }

            nimaResponse.send(bodyToBytes(response.body))
        }
        catch (e: Exception) {
            nimaResponse.status(Status.INTERNAL_SERVER_ERROR_500)
            nimaResponse.send(bodyToBytes(e.toText()))
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
