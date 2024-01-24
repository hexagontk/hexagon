package com.hexagonkt.http.handlers

import com.hexagonkt.handlers.Handler
import com.hexagonkt.http.model.*
import com.hexagonkt.http.model.HttpCall

sealed interface HttpHandler : Handler<HttpCall> {
    val handlerPredicate: HttpPredicate

    fun addPrefix(prefix: String): HttpHandler

    fun byMethod(): Map<HttpMethod, HttpHandler> =
        handlerPredicate.methods.associateWith { filter(it) }

    fun filter(method: HttpMethod): HttpHandler =
        when (this) {
            is PathHandler ->
                copy(
                    handlerPredicate = handlerPredicate.clearMethods(),
                    handlers = handlers
                        .filter {
                            val methods = it.handlerPredicate.methods
                            method in methods || methods.isEmpty()
                        }
                        .map { it.filter(method) }
                )

            is OnHandler ->
                copy(handlerPredicate = handlerPredicate.clearMethods())

            is FilterHandler ->
                copy(handlerPredicate = handlerPredicate.clearMethods())

            is AfterHandler ->
                copy(handlerPredicate = handlerPredicate.clearMethods())

            is BeforeHandler ->
                copy(handlerPredicate = handlerPredicate.clearMethods())

            else ->
                this
        }

    fun process(request: HttpRequestPort): HttpContext =
        HttpContext(HttpCall(request = request), handlerPredicate).let { context ->
            if (handlerPredicate(context)) process(context) as HttpContext
            else context
        }
}
