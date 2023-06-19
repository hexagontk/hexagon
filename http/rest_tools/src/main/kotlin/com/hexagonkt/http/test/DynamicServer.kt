package com.hexagonkt.http.test

import com.hexagonkt.http.model.NOT_FOUND_404
import com.hexagonkt.http.server.HttpServer
import com.hexagonkt.http.server.HttpServerPort
import com.hexagonkt.http.handlers.HttpContext
import com.hexagonkt.http.handlers.PathHandler
import com.hexagonkt.http.server.HttpServerSettings

/**
 * Server with dynamic handler (delegated to [path]). Root handler can be replaced at any time
 * without restarting the server.
 */
data class DynamicServer(
    private val adapter: HttpServerPort,
    var path: PathHandler = PathHandler(),
    private val serverSettings: HttpServerSettings = HttpServerSettings(),
) {

    val runtimePort: Int by lazy { server.runtimePort }
    val settings: HttpServerSettings by lazy { server.settings }

    private val server: HttpServer by lazy {
        HttpServer(adapter, serverSettings) {
            after(pattern = "*", status = NOT_FOUND_404) {
                HttpContext(response = this@DynamicServer.path.process(request).response)
            }
        }
    }

    fun start() {
        server.start()
    }

    fun stop() {
        server.stop()
    }
}
