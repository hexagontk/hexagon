package com.hexagontk.http.server

import com.hexagontk.core.text.Ansi.RESET
import com.hexagontk.core.text.AnsiColor.BLUE
import com.hexagontk.core.text.AnsiColor.CYAN
import com.hexagontk.core.text.AnsiColor.DEFAULT
import com.hexagontk.core.text.AnsiEffect.BOLD
import com.hexagontk.http.handlers.HandlerBuilder
import com.hexagontk.http.handlers.HttpHandler

/**
 * Default server banner message.
 */
val serverBanner: String = """
    $CYAN          _________
    $CYAN         /         \
    $CYAN        /   ____   /
    $CYAN       /   /   /  /
    $CYAN      /   /   /__/$BLUE   /\$BOLD    H E X A G O N$RESET
    $CYAN     /   /$BLUE          /  \$DEFAULT        ___
    $CYAN     \  /$BLUE   ___    /   /
    $CYAN      \/$BLUE   /  /   /   /$CYAN    T O O L K I T$RESET
    $BLUE          /  /___/   /
    $BLUE         /          /
    $BLUE         \_________/       https://hexagontk.com/http_server
    $RESET
    """.trimIndent()

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
