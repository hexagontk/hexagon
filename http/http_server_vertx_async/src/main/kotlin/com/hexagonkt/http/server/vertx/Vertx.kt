package com.hexagonkt.http.server.vertx

import com.hexagonkt.http.server.async.HttpServer
import com.hexagonkt.http.server.async.HttpServerSettings
import com.hexagonkt.http.handlers.async.HandlerBuilder
import com.hexagonkt.http.handlers.async.HttpHandler

/**
 * Create a Vert.x server and start it. It is a shortcut to avoid passing the adapter.
 *
 * @param settings Server settings info .
 * @param handlers List of [HttpHandler] handlers used in this server instance.
 *
 * @return The started [HttpServer] instance.
 */
fun serve(
    settings: HttpServerSettings = HttpServerSettings(), handlers: HttpHandler
): HttpServer =
    HttpServer(VertxServerAdapter(), handlers, settings).apply { start() }

/**
 * Create a Vert.x server and start it. It is a shortcut to avoid passing the adapter.
 *
 * @param settings Server settings info.
 * @param block Lambda to be used to create the list of [HttpHandler] handlers used in the server.
 *
 * @return The started [HttpServer] instance.
 */
fun serve(
    settings: HttpServerSettings = HttpServerSettings(), block: HandlerBuilder.() -> Unit
): HttpServer =
    HttpServer(VertxServerAdapter(), HandlerBuilder().apply { block() }.handler(), settings)
        .apply { start() }
