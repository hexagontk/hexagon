package com.hexagontk.http.handlers

import com.hexagontk.core.media.TEXT_PLAIN
import com.hexagontk.core.toText
import com.hexagontk.handlers.ChainHandler
import com.hexagontk.handlers.Handler
import com.hexagontk.http.model.*
import com.hexagontk.http.model.HttpMethod.Companion.ALL

class PathHandler(
    override val handlerPredicate: HttpPredicate,
    val handlers: List<HttpHandler>,
) :
    HttpHandler,
    Handler<HttpCall> by ChainHandler(
        handlers.map { it.addPrefix(handlerPredicate.pathPattern.pattern) },
        handlerPredicate,
    )
{

    private companion object {
        fun nestedMethods(handlers: List<HttpHandler>): Set<HttpMethod> =
            handlers
                .flatMap { it.handlerPredicate.methods.ifEmpty { ALL } }
                .toSet()
    }

    constructor(vararg handlers: HttpHandler) :
        this(
            HttpPredicate(methods = nestedMethods(handlers.toList())),
            handlers.toList()
        )

    constructor(pattern: String, handlers: List<HttpHandler>) :
        this(
            HttpPredicate(
                methods = nestedMethods(handlers.toList()),
                pattern = pattern,
                prefix = true,
            ),
            handlers
        )

    constructor(pattern: String, vararg handlers: HttpHandler) :
        this(pattern, handlers.toList())

    override fun process(request: HttpRequestPort): HttpContext =
        process(HttpContext(HttpCall(request = request), predicate)).let {
            val event = it.event
            val response = event.response
            val exception = it.exception

            if (exception != null) {
                if (response.status !in SERVER_ERROR)
                    it.with(
                        event = HttpCall(
                            event.request,
                            response.with(
                                body = exception.toText(),
                                contentType = ContentType(TEXT_PLAIN),
                                status = INTERNAL_SERVER_ERROR_500,
                            )
                        )
                    )
                else
                    it
            }
            else {
                it
            }
        } as HttpContext

    override fun addPrefix(prefix: String): HttpHandler =
        PathHandler(handlerPredicate.addPrefix(prefix), handlers)

    fun describe(): String =
        listOf(
            listOf(handlerPredicate.describe()),
            handlers
                .map {
                    when (it) {
                        is PathHandler -> it.describe()
                        else -> it.handlerPredicate.describe()
                    }
                }
                .map {it.prependIndent(" ".repeat(4)) }
        )
        .flatten()
        .joinToString("\n") { it }
}
