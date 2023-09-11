package com.hexagonkt.http.server.netty

import com.hexagonkt.http.handlers.coroutines.HandlerBuilder
import com.hexagonkt.http.handlers.coroutines.HttpHandler
import com.hexagonkt.http.server.coroutines.HttpServer
import com.hexagonkt.http.server.coroutines.HttpServerSettings

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
    HttpServer(NettyServerAdapter(), handlers, settings).apply { start() }

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
    HttpServer(NettyServerAdapter(), HandlerBuilder().apply { block() }.handler(), settings)
        .apply { start() }
