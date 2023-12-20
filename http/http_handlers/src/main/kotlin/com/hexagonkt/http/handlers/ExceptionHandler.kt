package com.hexagonkt.http.handlers

import com.hexagonkt.handlers.ExceptionHandler
import com.hexagonkt.handlers.Handler
import com.hexagonkt.http.model.HttpCall
import kotlin.reflect.KClass

data class ExceptionHandler<E : Exception>(
    val exception: KClass<E>,
    val clear: Boolean = true,
    val block: HttpExceptionCallback<E>
) : HttpHandler, Handler<HttpCall> by ExceptionHandler(exception, clear, toCallback(block)) {

    override val handlerPredicate: HttpPredicate = HttpPredicate()

    override fun addPrefix(prefix: String): HttpHandler =
        this
}
