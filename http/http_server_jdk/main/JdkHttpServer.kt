package com.hexagontk.http.server.jdk

import com.hexagontk.core.fieldsMapOf
import com.hexagontk.core.media.TEXT_PLAIN
import com.hexagontk.core.security.createKeyManagerFactory
import com.hexagontk.core.security.createTrustManagerFactory
import com.hexagontk.core.toText
import com.hexagontk.http.SslSettings
import com.hexagontk.http.model.HttpProtocol
import com.hexagontk.http.model.HttpProtocol.*
import com.hexagontk.http.server.HttpServer
import com.hexagontk.http.HttpFeature
import com.hexagontk.http.handlers.bodyToBytes
import com.hexagontk.http.model.HttpResponse
import com.hexagontk.http.model.HttpResponsePort
import com.hexagontk.http.model.INTERNAL_SERVER_ERROR_500
import com.hexagontk.http.server.HttpServerPort
import com.sun.net.httpserver.Headers
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpsConfigurator
import com.sun.net.httpserver.HttpServer as SunHttpServer
import com.sun.net.httpserver.HttpsServer as SunHttpsServer
import java.net.InetSocketAddress
import com.sun.net.httpserver.HttpHandler as SunHttpHandler
import java.security.SecureRandom
import java.util.concurrent.Executor
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory

/**
 * Implements [HttpServerPort] using the JDK HTTP server.
 */
class JdkHttpServer(
    private val backlog: Int = 1_024,
    private val executor: Executor? = null,
    private val stopDelay: Int = 0,
) : HttpServerPort {

    private companion object {
        const val START_ERROR_MESSAGE = "JDK HTTP server not started correctly"
    }

    private var started = false
    private var sunServer: SunHttpServer? = null

    constructor() : this(
        backlog = 1_024,
        executor = null
    )

    override fun runtimePort(): Int {
        return sunServer?.address?.port ?: error(START_ERROR_MESSAGE)
    }

    override fun started() =
        started

    override fun startUp(server: HttpServer) {
        val settings = server.settings
        val sslSettings = settings.sslSettings
        val handlers = server.handler.byMethod().mapKeys { it.key.toString() }

        val host = settings.bindAddress.hostName
        val port = settings.bindPort
        val address = InetSocketAddress(host, port)

        var jdkServer =
            if (sslSettings == null)
                SunHttpServer.create(address, backlog)
            else
                SunHttpsServer.create(address, backlog).apply {
                    val sslContext = sslContext(sslSettings)
                    httpsConfigurator = HttpsConfigurator(sslContext)
                }

        jdkServer.createContext("/", object : SunHttpHandler {
            override fun handle(exchange: HttpExchange) {
                val method = exchange.requestMethod
                val request = JdkRequestAdapter(method, exchange)
                val response = handlers[method]?.process(request)?.response ?: HttpResponse()
                reply(response, exchange)
            }
        })

        jdkServer.executor = executor
        jdkServer.start()

        sunServer = jdkServer
        started = true
    }

    override fun shutDown() {
        sunServer?.stop(stopDelay) ?: error(START_ERROR_MESSAGE)
        started = false
    }

    override fun supportedProtocols(): Set<HttpProtocol> =
        setOf(HTTP, HTTPS)

    override fun supportedFeatures(): Set<HttpFeature> =
        emptySet()

    override fun options(): Map<String, *> =
        fieldsMapOf(
            JdkHttpServer::backlog to backlog,
            JdkHttpServer::executor to executor,
            JdkHttpServer::stopDelay to stopDelay,
        )

    private fun reply(response: HttpResponsePort, exchange: HttpExchange) {
        var headers = exchange.responseHeaders

        try {
            response.headers.forEach { headers.add(it.name, it.text) }
            send(response.status, response.body, response.contentType?.text, headers, exchange)
        }
        catch (e: Exception) {
            send(INTERNAL_SERVER_ERROR_500, e.toText(), TEXT_PLAIN.fullType, headers, exchange)
        }
        finally {
            exchange.close()
        }
    }

    private fun send(
        status: Int, result: Any, contentType: String?, headers: Headers, exchange: HttpExchange
    ) {
        if (contentType != null)
            headers.set("content-type", contentType)

        var body = bodyToBytes(result)
        val size = if (body.isEmpty()) -1 else body.size.toLong()
        exchange.sendResponseHeaders(status, size)
        if (size != -1L)
            exchange.responseBody.use { it.write(body) }
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
