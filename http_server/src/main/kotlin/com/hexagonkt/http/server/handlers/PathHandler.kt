package com.hexagonkt.http.server.handlers

import com.hexagonkt.core.handlers.ChainHandler
import com.hexagonkt.core.handlers.Context
import com.hexagonkt.core.handlers.Handler
import com.hexagonkt.core.media.TextMedia.PLAIN
import com.hexagonkt.core.toText
import com.hexagonkt.http.model.ContentType
import com.hexagonkt.http.model.HttpMethod
import com.hexagonkt.http.model.HttpMethod.Companion.ALL
import com.hexagonkt.http.model.HttpStatusType.SERVER_ERROR
import com.hexagonkt.http.model.ServerErrorStatus.INTERNAL_SERVER_ERROR
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
        process(Context(HttpServerCall(request = request), predicate)).let {
            val response = it.event.response
            val exception = it.exception

            if (exception != null && response.status.type != SERVER_ERROR)
                response.copy(
                    body = exception.toText(),
                    contentType = ContentType(PLAIN),
                    status = INTERNAL_SERVER_ERROR,
                )
            else response
        }

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
