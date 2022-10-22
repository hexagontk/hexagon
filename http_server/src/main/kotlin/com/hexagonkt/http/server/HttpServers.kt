package com.hexagonkt.http.server

import com.hexagonkt.http.server.handlers.ServerBuilder
import com.hexagonkt.http.server.handlers.HttpHandler

/**
 * Create a server and start it.
 *
 * @param adapter Adapter instance which implements [HttpServerPort].
 * @param handlers List of handlers to be used by the server.
 * @param settings Server settings info.
 *
 * @return The started [HttpServer] instance.
 */
fun serve(
    adapter: HttpServerPort,
    handlers: List<HttpHandler>,
    settings: HttpServerSettings = HttpServerSettings()
): HttpServer =
    HttpServer(adapter, handlers, settings).apply { start() }

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
    serve(adapter, listOf(handler), settings)

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
    block: ServerBuilder.() -> Unit
): HttpServer =
    HttpServer(adapter, settings, block).apply { start() }
