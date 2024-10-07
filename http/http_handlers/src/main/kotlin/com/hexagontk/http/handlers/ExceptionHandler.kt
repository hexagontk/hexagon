package com.hexagontk.http.handlers

import com.hexagontk.handlers.ExceptionHandler
import com.hexagontk.handlers.Handler
import com.hexagontk.http.model.HttpCall
import kotlin.reflect.KClass

data class ExceptionHandler<E : Exception>(
    val exception: KClass<E>,
    val clear: Boolean = true,
    override val parent: HttpHandler? = null,
    val block: HttpExceptionCallbackType<E>,
) : HttpHandler,
    Handler<HttpCall> by ExceptionHandler(exception, clear, parent, toCallback(block)) {

    constructor(
        exception: KClass<E>,
        clear: Boolean = true,
        block: HttpExceptionCallbackType<E>,
    ) : this(exception, clear, null, block)

    override val handlerPredicate: HttpPredicate = HttpPredicate()

    override fun addPrefix(prefix: String): HttpHandler =
        this
}
