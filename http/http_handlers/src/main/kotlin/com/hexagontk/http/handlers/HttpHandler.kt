package com.hexagontk.http.handlers

import com.hexagontk.handlers.Handler
import com.hexagontk.http.model.*
import com.hexagontk.http.model.HttpCall

interface HttpHandler : Handler<HttpCall> {
    override val parent: HttpHandler?
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

    fun processHttp(context: HttpContext): HttpContext =
        if (handlerPredicate(context)) process(context) as HttpContext
        else context

    fun process(request: HttpRequestPort): HttpContext =
        processHttp(HttpContext(HttpCall(request = request), handlerPredicate))
}
