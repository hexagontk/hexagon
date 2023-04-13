package com.hexagonkt.http.server.handlers

import com.hexagonkt.handlers.Handler
import com.hexagonkt.http.model.*
import com.hexagonkt.http.server.model.HttpServerCall
import com.hexagonkt.http.server.model.HttpServerRequestPort

sealed interface HttpHandler : Handler<HttpServerCall> {
    val serverPredicate: HttpServerPredicate

    fun addPrefix(prefix: String): HttpHandler

    fun byMethod(): Map<HttpMethod, HttpHandler> =
        serverPredicate.methods.associateWith { filter(it) }

    fun filter(method: HttpMethod): HttpHandler =
        when (this) {
            is PathHandler ->
                copy(
                    serverPredicate = serverPredicate.clearMethods(),
                    handlers = handlers
                        .filter {
                            val methods = it.serverPredicate.methods
                            method in methods || methods.isEmpty()
                        }
                        .map { it.filter(method) }
                )

            is OnHandler ->
                copy(serverPredicate = serverPredicate.clearMethods())

            is FilterHandler ->
                copy(serverPredicate = serverPredicate.clearMethods())

            is AfterHandler ->
                copy(serverPredicate = serverPredicate.clearMethods())
        }

    fun process(request: HttpServerRequestPort): HttpServerContext =
        HttpServerContext(HttpServerCall(request = request), predicate).let { context ->
            if (serverPredicate(context)) process(context) as HttpServerContext
            else context
        }
}
