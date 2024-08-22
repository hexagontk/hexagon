package com.hexagontk.http.handlers

import com.hexagontk.handlers.ExceptionHandler
import com.hexagontk.handlers.Handler
import com.hexagontk.http.model.HttpCall
import kotlin.reflect.KClass

data class ExceptionHandler<E : Exception>(
    val exception: KClass<E>,
    val clear: Boolean = true,
    val block: HttpExceptionCallbackType<E>
) : HttpHandler, Handler<HttpCall> by ExceptionHandler(exception, clear, toCallback(block)) {

    override val handlerPredicate: HttpPredicate = HttpPredicate()

    override fun addPrefix(prefix: String): HttpHandler =
        this
}
