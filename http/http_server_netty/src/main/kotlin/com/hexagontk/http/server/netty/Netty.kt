package com.hexagontk.http.server.netty

import com.hexagontk.http.server.*
import com.hexagontk.http.handlers.HandlerBuilder
import com.hexagontk.http.handlers.HttpHandler

/**
 * Create a Netty server and start it. It is a shortcut to avoid passing the adapter.
 *
 * @param settings Server settings info .
 * @param handlers List of [HttpHandler] handlers used in this server instance.
 *
 * @return The started [HttpServer] instance.
 */
fun serve(
    settings: HttpServerSettings = HttpServerSettings(), handlers: HttpHandler
): HttpServer =
    HttpServer(NettyHttpServer(), handlers, settings).apply { start() }

/**
 * Create a Netty server and start it. It is a shortcut to avoid passing the adapter.
 *
 * @param settings Server settings info.
 * @param block Lambda to be used to create the list of [HttpHandler] handlers used in the server.
 *
 * @return The started [HttpServer] instance.
 */
fun serve(
    settings: HttpServerSettings = HttpServerSettings(), block: HandlerBuilder.() -> Unit
): HttpServer =
    HttpServer(NettyHttpServer(), HandlerBuilder().apply { block() }.handler(), settings)
        .apply { start() }
