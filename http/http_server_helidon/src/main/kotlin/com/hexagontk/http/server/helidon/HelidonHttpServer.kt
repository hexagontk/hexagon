package com.hexagontk.http.server.helidon

import com.hexagontk.core.fieldsMapOf
import com.hexagontk.core.security.createKeyManagerFactory
import com.hexagontk.core.security.createTrustManagerFactory
import com.hexagontk.core.toText
import com.hexagontk.http.SslSettings
import com.hexagontk.http.handlers.bodyToBytes
import com.hexagontk.http.handlers.HttpHandler
import com.hexagontk.http.model.HttpProtocol
import com.hexagontk.http.model.HttpProtocol.*
import com.hexagontk.http.model.HttpResponse
import com.hexagontk.http.model.HttpResponsePort
import com.hexagontk.http.server.HttpServer
import com.hexagontk.http.HttpFeature
import com.hexagontk.http.HttpFeature.*
import com.hexagontk.http.server.HttpServerPort
import io.helidon.common.socket.SocketOptions
import io.helidon.http.Method
import io.helidon.http.Status
import io.helidon.http.HeaderNames
import io.helidon.http.HttpMediaType
import io.helidon.http.SetCookie
import io.helidon.webserver.WebServer
import io.helidon.webserver.http.ServerResponse
import io.helidon.webserver.http1.Http1Config
import io.helidon.webserver.http2.Http2Config
import java.security.SecureRandom
import java.time.Duration
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLParameters
import javax.net.ssl.TrustManagerFactory

/**
 * Implements [HttpServerPort] using Helidon.
 *
 * TODO Add settings for HTTP2 and separate them on constructor parameters
 */
class HelidonHttpServer(
    private val backlog: Int = 1_024,
    private val writeQueueLength: Int = 0,
    private val readTimeout: Duration = Duration.ofSeconds(30),
    private val connectTimeout: Duration = Duration.ofSeconds(10),
    private val tcpNoDelay: Boolean = false,
    private val receiveLog: Boolean = true,
    private val sendLog: Boolean = true,
    private val validatePath: Boolean = true,
    private val validateRequestHeaders: Boolean = true,
    private val validateResponseHeaders: Boolean = false,
) : HttpServerPort {

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

        // TODO features(): [Config, Encoding, Media, WebServer] Maybe Multipart can be added there
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

        val protocolConfig =
            if (settings.protocol == HTTP || settings.protocol == HTTPS)
                Http1Config
                    .builder()
                    .receiveLog(receiveLog)
                    .sendLog(sendLog)
                    .validatePath(validatePath)
                    .validateRequestHeaders(validateRequestHeaders)
                    .validateResponseHeaders(validateResponseHeaders)
                    .build()
            else
                Http2Config
                    .builder()
                    .validatePath(validatePath)
                    .build()

        helidonServer = serverBuilder
            .backlog(backlog)
            .writeQueueLength(writeQueueLength)
            .connectionOptions(SocketOptions
                .builder()
                .readTimeout(readTimeout)
                .connectTimeout(connectTimeout)
                .tcpNoDelay(tcpNoDelay)
                .build()
            )
            .protocols(listOf(protocolConfig))
            .build()

        helidonServer?.start() ?: error(START_ERROR_MESSAGE)
    }

    override fun shutDown() {
        helidonServer?.stop() ?: error(START_ERROR_MESSAGE)
    }

    override fun supportedProtocols(): Set<HttpProtocol> =
        setOf(HTTP, HTTPS, HTTP2)

    override fun supportedFeatures(): Set<HttpFeature> =
        setOf(ZIP, COOKIES)

    override fun options(): Map<String, *> =
        fieldsMapOf(
            HelidonHttpServer::backlog to backlog,
            HelidonHttpServer::writeQueueLength to writeQueueLength,
            HelidonHttpServer::readTimeout to readTimeout,
            HelidonHttpServer::connectTimeout to connectTimeout,
            HelidonHttpServer::tcpNoDelay to tcpNoDelay,
            HelidonHttpServer::receiveLog to receiveLog,
            HelidonHttpServer::sendLog to sendLog,
            HelidonHttpServer::validatePath to validatePath,
            HelidonHttpServer::validateRequestHeaders to validateRequestHeaders,
            HelidonHttpServer::validateResponseHeaders to validateResponseHeaders,
        )

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
        val keyManager = keyManagerFactory(sslSettings)
        val trustManager = trustManagerFactory(sslSettings)

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

    private fun trustManagerFactory(sslSettings: SslSettings): TrustManagerFactory? {
        val trustStoreUrl = sslSettings.trustStore ?: return null
        val trustStorePassword = sslSettings.trustStorePassword
        return createTrustManagerFactory(trustStoreUrl, trustStorePassword)
    }

    private fun keyManagerFactory(sslSettings: SslSettings): KeyManagerFactory {
        val keyStoreUrl = sslSettings.keyStore ?: error("")
        val keyStorePassword = sslSettings.keyStorePassword
        return createKeyManagerFactory(keyStoreUrl, keyStorePassword)
    }
}
