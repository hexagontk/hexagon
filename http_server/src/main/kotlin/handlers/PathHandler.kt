package com.hexagonkt.http.server.handlers

import com.hexagonkt.core.handlers.ChainHandler
import com.hexagonkt.core.handlers.Handler
import com.hexagonkt.http.model.HttpMethod
import com.hexagonkt.http.model.HttpMethod.Companion.ALL
import com.hexagonkt.http.patterns.createPathPattern
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
                pathPattern = createPathPattern(pattern, prefix = true),
            ),
            handlers
        )

    constructor(pattern: String, vararg handlers: HttpHandler) :
        this(pattern, handlers.toList())

    suspend fun process(request: HttpServerRequestPort): HttpServerResponse =
        process(HttpServerCall(request = request)).response

    override fun addPrefix(prefix: String): HttpHandler =
        copy(serverPredicate = serverPredicate.addPrefix(prefix))

    fun byMethod(): Map<HttpMethod, PathHandler> =
        serverPredicate.methods.associateWith { filter(it) }

    fun filter(method: HttpMethod): PathHandler =
        copy(
            serverPredicate = serverPredicate.clearMethods(),
            handlers = handlers
                .filter {
                    method in it.serverPredicate.methods || it.serverPredicate.methods.isEmpty()
                }
                .map {
                    when (it) {
                        is PathHandler ->
                            it.filter(method)
                        is OnHandler ->
                            it.copy(serverPredicate = it.serverPredicate.clearMethods())
                        is FilterHandler ->
                            it.copy(serverPredicate = it.serverPredicate.clearMethods())
                        is AfterHandler ->
                            it.copy(serverPredicate = it.serverPredicate.clearMethods())
                    }
                }
        )

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
