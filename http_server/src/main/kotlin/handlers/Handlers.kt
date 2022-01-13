package com.hexagonkt.http.server.handlers

import com.hexagonkt.core.handlers.Callback
import com.hexagonkt.http.server.model.HttpServerCall

typealias HttpCallback = suspend HttpServerContext.() -> HttpServerContext

val exceptionHandler = AfterHandler(pattern = "*", exception = Exception::class) {
    internalServerError(exception ?: RuntimeException("Unhandled Exception"))
}

internal fun toCallback(handler: HttpCallback): Callback<HttpServerCall> =
    { context -> HttpServerContext(context).handler().context }

fun path(pattern: String = "", block: PathBuilder.() -> Unit): PathHandler {
    val builder = PathBuilder()
    builder.block()
    return PathHandler(pattern, builder.handlers)
}

// TODO Add first filter with error handling and 'bodyToBytes' checks
fun path(contextPath: String = "", handlers: List<ServerHandler>): PathHandler =
    handlers
        .filterIsInstance<HttpHandler>()
        .let {
            if (it.size == 1 && it[0] is PathHandler)
                (it[0] as PathHandler).addPrefix(contextPath) as PathHandler
            else
                PathHandler(contextPath, it)
        }
