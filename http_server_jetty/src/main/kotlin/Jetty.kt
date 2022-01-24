package com.hexagonkt.http.server.jetty

import com.hexagonkt.http.server.*
import com.hexagonkt.http.server.handlers.ServerBuilder
import com.hexagonkt.http.server.handlers.ServerHandler

/**
 * Create a Jetty server and start it. It is a shortcut to avoid passing the adapter.
 *
 * @param settings Server settings info .
 * @param handlers List of [ServerHandler] handlers used in this server instance.
 *
 * @return The started [HttpServer] instance.
 */
fun serve(
    settings: HttpServerSettings = HttpServerSettings(), handlers: List<ServerHandler>
): HttpServer =
    HttpServer(JettyServletAdapter(), handlers, settings).apply { start() }

/**
 * Create a Jetty server and start it. It is a shortcut to avoid passing the adapter.
 *
 * @param settings Server settings info.
 * @param block Lambda to be used to create the list of [ServerHandler] handlers used in the server.
 *
 * @return The started [HttpServer] instance.
 */
fun serve(
    settings: HttpServerSettings = HttpServerSettings(), block: ServerBuilder.() -> Unit
): HttpServer =
    HttpServer(JettyServletAdapter(), ServerBuilder().apply {block()}.handlers, settings)
        .apply { start() }
