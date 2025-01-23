package com.hexagontk.http.handlers

import com.hexagontk.handlers.Handler
import com.hexagontk.http.model.*
import com.hexagontk.http.model.HttpCall

interface HttpHandler : Handler<HttpCall> {
    val handlerPredicate: HttpPredicate

    fun addPrefix(prefix: String): HttpHandler

    fun byMethod(): Map<HttpMethod, HttpHandler> =
        handlerPredicate.methods.associateWith { filter(it) }

    fun filter(method: HttpMethod): HttpHandler =
        when (this) {
            is PathHandler ->
                PathHandler(
                    handlerPredicate.clearMethods(),
                    handlers
                        .filter {
                            val methods = it.handlerPredicate.methods
                            method in methods || methods.isEmpty()
                        }
                        .map { it.filter(method) }
                )

            is OnHandler ->
                OnHandler(handlerPredicate.clearMethods(), block)

            is FilterHandler ->
                FilterHandler(handlerPredicate.clearMethods(), block)

            is AfterHandler ->
                AfterHandler(handlerPredicate.clearMethods(), block)

            is BeforeHandler ->
                BeforeHandler(handlerPredicate.clearMethods(), block)

            else ->
                this
        }

    fun processHttp(context: HttpContext): HttpContext =
        if (handlerPredicate(context)) process(context) as HttpContext
        else context

    fun process(request: HttpRequestPort): HttpContext =
        processHttp(HttpContext(HttpCall(request = request), handlerPredicate))
}
