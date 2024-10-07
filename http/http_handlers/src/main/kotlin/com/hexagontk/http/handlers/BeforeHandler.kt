package com.hexagontk.http.handlers

import com.hexagontk.handlers.BeforeHandler
import com.hexagontk.handlers.Handler
import com.hexagontk.http.model.HttpMethod
import com.hexagontk.http.model.HttpStatus
import com.hexagontk.http.model.HttpCall
import kotlin.reflect.KClass

data class BeforeHandler(
    override val handlerPredicate: HttpPredicate = HttpPredicate(),
    val block: HttpCallbackType,
    override val parent: HttpHandler? = null,
) : HttpHandler, Handler<HttpCall> by BeforeHandler(handlerPredicate, parent, toCallback(block)) {

    constructor(
        methods: Set<HttpMethod> = emptySet(),
        pattern: String = "",
        exception: KClass<out Exception>? = null,
        status: HttpStatus? = null,
        block: HttpCallbackType,
    ) :
        this(HttpPredicate(methods, pattern, exception, status), block)

    constructor(method: HttpMethod, pattern: String = "", block: HttpCallbackType) :
        this(setOf(method), pattern, block = block)

    constructor(pattern: String, block: HttpCallbackType) :
        this(emptySet(), pattern, block = block)

    override fun addPrefix(prefix: String): HttpHandler =
        copy(handlerPredicate = handlerPredicate.addPrefix(prefix))
}
