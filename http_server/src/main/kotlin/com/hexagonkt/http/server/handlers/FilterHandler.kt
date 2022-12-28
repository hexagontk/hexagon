package com.hexagonkt.http.server.handlers

import com.hexagonkt.handlers.FilterHandler
import com.hexagonkt.handlers.Handler
import com.hexagonkt.http.model.HttpMethod
import com.hexagonkt.http.model.HttpStatus
import com.hexagonkt.http.server.model.HttpServerCall
import kotlin.reflect.KClass

data class FilterHandler(
    override val serverPredicate: HttpServerPredicate = HttpServerPredicate(),
    val block: HttpCallback
) : HttpHandler, Handler<HttpServerCall> by FilterHandler(serverPredicate, toCallback(block)) {

    constructor(
        methods: Set<HttpMethod> = emptySet(),
        pattern: String = "",
        exception: KClass<out Exception>? = null,
        status: HttpStatus? = null,
        block: HttpCallback,
    ) :
        this(HttpServerPredicate(methods, pattern, exception, status), block)

    constructor(method: HttpMethod, pattern: String = "", block: HttpCallback) :
        this(setOf(method), pattern, block = block)

    constructor(pattern: String, block: HttpCallback) :
        this(emptySet(), pattern, block = block)

    override fun addPrefix(prefix: String): HttpHandler =
        copy(serverPredicate = serverPredicate.addPrefix(prefix))
}
