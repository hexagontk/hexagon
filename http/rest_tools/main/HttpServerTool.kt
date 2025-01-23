package com.hexagontk.rest.tools

import com.hexagontk.http.handlers.HandlerBuilder
import com.hexagontk.http.model.NOT_FOUND_404
import com.hexagontk.http.server.HttpServer
import com.hexagontk.http.server.HttpServerPort
import com.hexagontk.http.handlers.PathHandler
import com.hexagontk.http.server.HttpServerSettings
import com.hexagontk.rest.SerializeResponseCallback
import java.net.URI

/**
 * Server with dynamic handler (delegated to [path]). Root handler can be replaced at any time
 * without restarting the server.
 */
class HttpServerTool(
    adapter: HttpServerPort,
    settings: HttpServerSettings = HttpServerSettings(),
    var path: PathHandler = PathHandler(),
) {
    val runtimePort: Int by lazy { server.runtimePort }
    val binding: URI by lazy { server.binding }

    private val server: HttpServer by lazy {
        HttpServer(adapter, settings) {
            // TODO Use SerializeResponseHandler when created
            after("*", SerializeResponseCallback())
            after(pattern = "*", status = NOT_FOUND_404) {
                send(response = this@HttpServerTool.path.process(request).response)
            }
        }
    }

    fun path(block: HandlerBuilder.() -> Unit) {
        path = com.hexagontk.http.handlers.path(block = block)
    }

    fun start() {
        server.start()
    }

    fun stop() {
        server.stop()
    }
}
