package com.hexagonkt.http.server.vertx

import com.hexagonkt.core.Jvm
import com.hexagonkt.core.fieldsMapOf
import com.hexagonkt.core.media.TEXT_PLAIN
import com.hexagonkt.core.toText
import com.hexagonkt.http.bodyToBytes
import com.hexagonkt.http.model.HttpProtocol
import com.hexagonkt.http.model.HttpProtocol.*
import com.hexagonkt.http.server.async.HttpServer
import com.hexagonkt.http.server.async.HttpServerFeature
import com.hexagonkt.http.server.async.HttpServerFeature.ZIP
import com.hexagonkt.http.server.async.HttpServerPort
import com.hexagonkt.http.handlers.async.HttpHandler
import com.hexagonkt.http.model.HttpResponse
import com.hexagonkt.http.model.HttpResponsePort
import com.hexagonkt.http.model.INTERNAL_SERVER_ERROR_500
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.buffer.Buffer.buffer
import io.vertx.core.eventbus.EventBusOptions
import io.vertx.core.http.*
import io.vertx.core.http.ClientAuth.NONE
import io.vertx.core.http.ClientAuth.REQUIRED
import io.vertx.core.http.CookieSameSite.STRICT
import io.vertx.core.http.CookieSameSite.NONE as SAME_SITE_NONE
import io.vertx.core.net.JksOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import java.net.URL
import java.nio.file.Files
import kotlin.io.path.absolutePathString

/**
 * Implements [HttpServerPort] using Vert.x.
 */
class VertxServerAdapter(
    private val eventLoopPoolSize: Int = Jvm.cpuCount,
    private val preferNativeTransport: Boolean = false,
    private val tcpFastOpen: Boolean = false,
    private val tcpCork: Boolean = false,
    private val tcpQuickAck: Boolean = false,
    private val reusePort: Boolean = false,
) : HttpServerPort {

    private var started: Boolean = false
    private lateinit var vertxServer: io.vertx.core.http.HttpServer

    constructor() : this(
        eventLoopPoolSize = Jvm.cpuCount,
        preferNativeTransport = false,
    )

    override fun runtimePort(): Int =
        vertxServer.actualPort()

    override fun started(): Boolean =
        started

    override fun supportedProtocols(): Set<HttpProtocol> =
        setOf(HTTP, HTTPS, HTTP2)

    override fun supportedFeatures(): Set<HttpServerFeature> =
        setOf(ZIP)

    override fun options(): Map<String, *> =
        fieldsMapOf(
            VertxServerAdapter::eventLoopPoolSize to eventLoopPoolSize,
            VertxServerAdapter::preferNativeTransport to preferNativeTransport,
            VertxServerAdapter::tcpFastOpen to tcpFastOpen,
            VertxServerAdapter::tcpCork to tcpCork,
            VertxServerAdapter::tcpQuickAck to tcpQuickAck,
            VertxServerAdapter::reusePort to reusePort,
        )

    override fun shutDown() {
        vertxServer
            .close()
            .onSuccess { started = false }
            .toCompletionStage()
            .toCompletableFuture()
            .join()
    }

    override fun startUp(server: HttpServer) {
        val settings = server.settings
        val sslSettings = settings.sslSettings
        val vertx = Vertx.vertx(VertxOptions()
            .setEventLoopPoolSize(eventLoopPoolSize)
            .setPreferNativeTransport(preferNativeTransport)
            .setEventBusOptions(EventBusOptions())
        )

        val handler = server.handler.addPrefix(settings.contextPath)
        val handlers = handler.byMethod().mapKeys { HttpMethod.valueOf(it.key.toString()) }

        val uploadDirectory = Files.createTempDirectory("hexagon")
        uploadDirectory.toFile().deleteOnExit()
        val router = Router.router(vertx)
        val bodyHandler = BodyHandler.create(uploadDirectory.absolutePathString())
        router.route().handler(bodyHandler)
        router.route().handler { handle(handlers, it) }

        val hasTrustStore = sslSettings?.trustStore != null
        vertxServer = vertx
            .createHttpServer(
                HttpServerOptions()
                    .setPort(settings.bindPort)
                    .setHost(settings.bindAddress.hostName)
                    .setCompressionSupported(settings.zip)
                    .setSsl(hasTrustStore || sslSettings?.keyStore != null)
                    .setClientAuth(if (hasTrustStore) REQUIRED else NONE)
                    .setTrustStoreOptions(
                        createStoreOptions(sslSettings?.trustStore, sslSettings?.trustStorePassword)
                    )
                    .setKeyStoreOptions(
                        createStoreOptions(sslSettings?.keyStore, sslSettings?.keyStorePassword)
                    )
                    .setTcpFastOpen(tcpFastOpen)
                    .setTcpCork(tcpCork)
                    .setTcpQuickAck(tcpQuickAck)
                    .setReusePort(reusePort)
            )
            .requestHandler(router)
            .listen()
            .onSuccess { started = true }
            .toCompletionStage()
            .toCompletableFuture()
            .join()
    }

    private fun createStoreOptions(store: URL?, password: String?): JksOptions {
        val trustStoreUrl = store ?: return JksOptions()
        return JksOptions()
            .setPassword(password)
            .setValue(buffer(trustStoreUrl.readBytes()))
    }

    private fun handle(handlers: Map<HttpMethod, HttpHandler>, context: RoutingContext) {
        val request = context.request()
        handlers[request.method()]
            ?.process(VertxRequestAdapter(context))
            ?.thenApply { createResponse(request, it.response) }
            ?: createResponse(request, HttpResponse())
    }

    private fun createResponse(request: HttpServerRequest, response: HttpResponsePort) {
        val vertxResponse = request.response()
        val vertxHeaders = vertxResponse.headers()
        val contentLength = response.contentLength

        vertxResponse.setStatusCode(response.status.code)
        contentLength.let { if (it >= 0) vertxHeaders.add("content-length", it.toString()) }
        response.contentType?.text?.let { vertxHeaders.add("content-type", it) }
        response.headers.values.forEach { vertxHeaders.add(it.name, it.values) }
        response.cookies.forEach {
            val cookie = Cookie.cookie(it.name, it.value)
            cookie.domain = it.domain
            cookie.path = it.path
            cookie.isHttpOnly = it.httpOnly
            cookie.isSecure = it.secure
            cookie.sameSite = if (it.sameSite) STRICT else SAME_SITE_NONE
            if (it.maxAge >= 0)
                cookie.maxAge = it.maxAge

            if (it.deleted) vertxResponse.removeCookie(cookie.name)
            else vertxResponse.addCookie(cookie)
        }

        try {
            val bytes = bodyToBytes(response.body)
            vertxResponse.end(buffer(bytes))
        }
        catch (e: Exception) {
            vertxHeaders.add("content-type", TEXT_PLAIN.fullType)
            vertxResponse.setStatusCode(INTERNAL_SERVER_ERROR_500.code).end(buffer(e.toText().toByteArray()))
        }
    }
}
