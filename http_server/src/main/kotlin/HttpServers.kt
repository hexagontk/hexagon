package com.hexagonkt.http.server

import com.hexagonkt.http.server.handlers.PathBuilder
import com.hexagonkt.http.server.handlers.ServerHandler

/**
 * Create a server and start it.
 *
 * @param adapter Adapter instance which implements [HttpServerPort].
 * @param handlers List of handlers to be used by the server.
 * @param settings Server settings info .
 *
 * @return The started [HttpServer] instance.
 */
fun serve(
    adapter: HttpServerPort,
    handlers: List<ServerHandler>,
    settings: HttpServerSettings = HttpServerSettings()
): HttpServer =
    HttpServer(adapter, handlers, settings).apply { start() }

/**
 * Create a server and start it.
 *
 * @param adapter Adapter instance which implements [HttpServerPort].
 * @param settings Server settings info.
 * @param block Lambda to be used to create the server's handlers.
 *
 * @return The started [HttpServer] instance.
 */
fun serve(
    adapter: HttpServerPort,
    settings: HttpServerSettings = HttpServerSettings(),
    block: PathBuilder.() -> Unit
): HttpServer =
    HttpServer(adapter, settings, block).apply { start() }
