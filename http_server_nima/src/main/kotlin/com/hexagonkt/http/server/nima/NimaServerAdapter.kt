package com.hexagonkt.http.server.nima

import com.hexagonkt.core.fieldsMapOf
import com.hexagonkt.core.logging.LoggingManager
import com.hexagonkt.http.model.HttpProtocol
import com.hexagonkt.http.model.HttpProtocol.*
import com.hexagonkt.http.server.HttpServer
import com.hexagonkt.http.server.HttpServerFeature
import com.hexagonkt.http.server.HttpServerFeature.WEB_SOCKETS
import com.hexagonkt.http.server.HttpServerFeature.ZIP
import com.hexagonkt.http.server.HttpServerPort
import com.hexagonkt.logging.jul.JulLoggingAdapter
import io.helidon.common.http.Http.Status
import io.helidon.nima.webserver.WebServer

/**
 * Implements [HttpServerPort] using Helidon Nima.
 */
open class NimaServerAdapter : HttpServerPort {

    private var nimaServer: WebServer? = null

    override fun runtimePort(): Int =
        nimaServer?.port() ?: error("")

    override fun started() =
        nimaServer?.isRunning ?: false

    override fun startUp(server: HttpServer) {
        LoggingManager.adapter = JulLoggingAdapter()
        val settings = server.settings

        nimaServer = WebServer
            .builder()
            .host(settings.bindAddress.canonicalHostName)
            .port(settings.bindPort)
            .routing {
                it.any { _, nimaResponse ->
                    nimaResponse.status(Status.OK_200)
                    nimaResponse.send("Hello!")
                }
            }
            .build()

        nimaServer?.start()
    }

    override fun shutDown() {
        nimaServer?.stop()
    }

    override fun supportedProtocols(): Set<HttpProtocol> =
        setOf(HTTP, HTTPS, HTTP2)

    override fun supportedFeatures(): Set<HttpServerFeature> =
        setOf(ZIP, WEB_SOCKETS)

    override fun options(): Map<String, *> =
        fieldsMapOf<NimaServerAdapter>()
}
