package com.hexagonkt.http.handlers

import com.hexagonkt.core.logging.Logger
import com.hexagonkt.core.media.TEXT_PLAIN
import com.hexagonkt.core.toText
import com.hexagonkt.handlers.ChainHandler
import com.hexagonkt.handlers.Handler
import com.hexagonkt.http.model.*
import com.hexagonkt.http.model.HttpMethod.Companion.ALL
import com.hexagonkt.http.model.HttpStatusType.SERVER_ERROR

data class PathHandler(
    override val handlerPredicate: HttpPredicate,
    val handlers: List<HttpHandler>
) :
    HttpHandler,
    Handler<HttpCall> by ChainHandler(
        handlers.map { it.addPrefix(handlerPredicate.pathPattern.pattern) },
        handlerPredicate
    )
{

    private companion object {
        val logger: Logger = Logger(PathHandler::class)
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
                logger.error(exception) {
                    "Exception received at call processing end. Clear/handle exception in a handler"
                }
                if (response.status.type != SERVER_ERROR)
                    it.with(
                        event = event.copy(
                            response = response.with(
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
        copy(handlerPredicate = handlerPredicate.addPrefix(prefix))

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
