package com.hexagontk.http.handlers

import com.hexagontk.handlers.OnHandler
import com.hexagontk.handlers.Handler
import com.hexagontk.http.model.HttpMethod
import com.hexagontk.http.model.HttpCall

class OnHandler(
    override val handlerPredicate: HttpPredicate = HttpPredicate(),
    val block: HttpCallbackType,
) : HttpHandler, Handler<HttpCall> by OnHandler(handlerPredicate, toCallback(block)) {

    constructor(
        methods: Set<HttpMethod> = emptySet(),
        pattern: String = "",
        status: Int? = null,
        block: HttpCallbackType,
    ) :
        this(HttpPredicate(methods, pattern, status), block)

    constructor(method: HttpMethod, pattern: String = "", block: HttpCallbackType) :
        this(setOf(method), pattern, block = block)

    constructor(pattern: String, block: HttpCallbackType) :
        this(emptySet(), pattern, block = block)

    override fun addPrefix(prefix: String): HttpHandler =
        OnHandler(handlerPredicate = handlerPredicate.addPrefix(prefix), block)
}
