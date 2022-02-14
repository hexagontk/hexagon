package com.hexagonkt.http.server.handlers

import com.hexagonkt.core.handlers.Handler
import com.hexagonkt.http.server.model.HttpServerCall

sealed interface HttpHandler : ServerHandler, Handler<HttpServerCall> {
    val serverPredicate: HttpServerPredicate

    fun addPrefix(prefix: String): HttpHandler
}
