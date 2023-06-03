package com.hexagonkt.http.server

import com.hexagonkt.http.handlers.HandlerBuilder
import com.hexagonkt.http.handlers.HttpHandler

/**
 * Create a server and start it.
 *
 * @param adapter Adapter instance which implements [HttpServerPort].
 * @param handler Handler to be used by the server.
 * @param settings Server settings info.
 *
 * @return The started [HttpServer] instance.
 */
fun serve(
    adapter: HttpServerPort,
    handler: HttpHandler,
    settings: HttpServerSettings = HttpServerSettings()
): HttpServer =
    HttpServer(adapter, handler, settings).apply { start() }

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
    block: HandlerBuilder.() -> Unit
): HttpServer =
    HttpServer(adapter, settings, block).apply { start() }
