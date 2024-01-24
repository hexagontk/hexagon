package com.hexagonkt.http.handlers

import com.hexagonkt.handlers.FilterHandler
import com.hexagonkt.handlers.Handler
import com.hexagonkt.http.model.HttpMethod
import com.hexagonkt.http.model.HttpStatus
import com.hexagonkt.http.model.HttpCall
import kotlin.reflect.KClass

data class FilterHandler(
    override val handlerPredicate: HttpPredicate = HttpPredicate(),
    val block: HttpCallbackType
) : HttpHandler, Handler<HttpCall> by FilterHandler(handlerPredicate, toCallback(block)) {

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
