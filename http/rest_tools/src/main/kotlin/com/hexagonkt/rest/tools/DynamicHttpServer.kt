package com.hexagonkt.rest.tools

import com.hexagonkt.http.handlers.HandlerBuilder
import com.hexagonkt.http.model.NOT_FOUND_404
import com.hexagonkt.http.server.HttpServer
import com.hexagonkt.http.server.HttpServerPort
import com.hexagonkt.http.handlers.PathHandler
import com.hexagonkt.http.server.HttpServerSettings
import com.hexagonkt.rest.SerializeResponseCallback
import java.net.URL

/**
 * Server with dynamic handler (delegated to [path]). Root handler can be replaced at any time
 * without restarting the server.
 */
data class DynamicHttpServer(
    private val adapter: HttpServerPort,
    private val settings: HttpServerSettings = HttpServerSettings(),
    var path: PathHandler = PathHandler(),
) {
    val runtimePort: Int by lazy { server.runtimePort }
    val binding: URL by lazy { server.binding }

    private val server: HttpServer by lazy {
        HttpServer(adapter, settings) {
            after("*", SerializeResponseCallback())
            after(pattern = "*", status = NOT_FOUND_404) {
                send(response = this@DynamicHttpServer.path.process(request).response)
            }
        }
    }

    fun path(block: HandlerBuilder.() -> Unit) {
        path = com.hexagonkt.http.handlers.path(block = block)
    }

    fun start() {
        server.start()
    }

    fun stop() {
        server.stop()
    }
}
