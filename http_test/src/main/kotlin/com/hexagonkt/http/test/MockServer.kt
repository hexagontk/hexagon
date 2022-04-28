package com.hexagonkt.http.test

import com.hexagonkt.http.model.ClientErrorStatus.NOT_FOUND
import com.hexagonkt.http.server.HttpServer
import com.hexagonkt.http.server.HttpServerPort
import com.hexagonkt.http.server.handlers.PathHandler

/**
 * Server with dynamic handler (delegated to [path]). Root handler can be replaced at any time
 * without restarting the server.
 */
data class MockServer(
    val adapter: HttpServerPort,
    var path: PathHandler = PathHandler()
) {

    val server: HttpServer by lazy {
        HttpServer(adapter) {
            after(pattern = "*", status = NOT_FOUND) {
                copy(
                    context = context.copy(
                        event = context.event.copy(
                            response = this@MockServer.path.process(request)
                        )
                    )
                )
            }
        }
    }
}
