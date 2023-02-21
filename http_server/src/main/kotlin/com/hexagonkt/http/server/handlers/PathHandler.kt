package com.hexagonkt.http.server.handlers

import com.hexagonkt.handlers.ChainHandler
import com.hexagonkt.handlers.Handler
import com.hexagonkt.http.model.HttpMethod
import com.hexagonkt.http.model.HttpMethod.Companion.ALL
import com.hexagonkt.http.server.model.HttpServerCall
import com.hexagonkt.http.server.model.HttpServerRequestPort
import com.hexagonkt.http.server.model.HttpServerResponse

data class PathHandler(
    override val serverPredicate: HttpServerPredicate,
    val handlers: List<HttpHandler>
) :
    HttpHandler,
    Handler<HttpServerCall> by ChainHandler(
        handlers.map { it.addPrefix(serverPredicate.pathPattern.pattern) },
        serverPredicate
    )
{

    private companion object {
        fun nestedMethods(handlers: List<HttpHandler>): Set<HttpMethod> =
            handlers
                .flatMap { it.serverPredicate.methods.ifEmpty { ALL } }
                .toSet()
    }

    constructor(vararg handlers: HttpHandler) :
        this(
            HttpServerPredicate(methods = nestedMethods(handlers.toList())),
            handlers.toList()
        )

    constructor(pattern: String, handlers: List<HttpHandler>) :
        this(
            HttpServerPredicate(
                methods = nestedMethods(handlers.toList()),
                pattern = pattern,
                prefix = true,
            ),
            handlers
        )

    constructor(pattern: String, vararg handlers: HttpHandler) :
        this(pattern, handlers.toList())

    override fun process(request: HttpServerRequestPort): HttpServerResponse =
        processContext(request).event.response

    override fun addPrefix(prefix: String): HttpHandler =
        copy(serverPredicate = serverPredicate.addPrefix(prefix))

    fun describe(): String =
        listOf(
            listOf(serverPredicate.describe()),
            handlers
                .map {
                    when (it) {
                        is PathHandler -> it.describe()
                        else -> it.serverPredicate.describe()
                    }
                }
                .map {it.prependIndent(" ".repeat(4)) }
        )
        .flatten()
        .joinToString("\n") { it }
}
