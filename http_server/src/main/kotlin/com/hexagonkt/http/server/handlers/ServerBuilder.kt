package com.hexagonkt.http.server.handlers

import com.hexagonkt.http.model.HttpMethod
import com.hexagonkt.http.model.HttpMethod.*
import com.hexagonkt.http.model.HttpStatus
import kotlin.reflect.KClass

class ServerBuilder(var handlers: List<ServerHandler> = emptyList()) {

    fun use(handler: HttpHandler) {
        this.handlers += handler
    }

    fun path(pattern: String, block: ServerBuilder.() -> Unit) {
        use(com.hexagonkt.http.server.handlers.path(pattern, block))
    }

    fun path(pattern: String, pathHandler: PathHandler) {
        use(pathHandler.addPrefix(pattern))
    }

    fun path(pattern: String, pathHandlers: List<HttpHandler>) {
        use(PathHandler(pattern, pathHandlers))
    }

    fun path(pattern: String, vararg pathHandlers: HttpHandler) {
        path(pattern, pathHandlers.toList())
    }

    fun on(
        predicate: HttpServerPredicate = HttpServerPredicate(),
        callback: HttpCallback
    ) {
        use(OnHandler(predicate, callback))
    }

    fun on(
        methods: Set<HttpMethod> = emptySet(),
        pattern: String = "",
        exception: KClass<out Exception>? = null,
        status: HttpStatus? = null,
        callback: HttpCallback,
    ) {
        use(OnHandler(methods, pattern, exception, status, callback))
    }

    fun on(method: HttpMethod, pattern: String = "", callback: HttpCallback) {
        use(OnHandler(method, pattern, callback))
    }

    fun on(pattern: String, callback: HttpCallback) {
        use(OnHandler(pattern, callback))
    }

    fun filter(
        predicate: HttpServerPredicate = HttpServerPredicate(),
        callback: HttpCallback
    ) {
        use(FilterHandler(predicate, callback))
    }

    fun filter(
        methods: Set<HttpMethod> = emptySet(),
        pattern: String = "",
        exception: KClass<out Exception>? = null,
        status: HttpStatus? = null,
        callback: HttpCallback,
    ) {
        use(FilterHandler(methods, pattern, exception, status, callback))
    }

    fun filter(method: HttpMethod, pattern: String = "", callback: HttpCallback) {
        use(FilterHandler(method, pattern, callback))
    }

    fun filter(pattern: String, callback: HttpCallback) {
        use(FilterHandler(pattern, callback))
    }

    fun after(
        predicate: HttpServerPredicate = HttpServerPredicate(),
        callback: HttpCallback
    ) {
        use(AfterHandler(predicate, callback))
    }

    fun after(
        methods: Set<HttpMethod> = emptySet(),
        pattern: String = "",
        exception: KClass<out Exception>? = null,
        status: HttpStatus? = null,
        callback: HttpCallback,
    ) {
        use(AfterHandler(methods, pattern, exception, status, callback))
    }

    fun after(method: HttpMethod, pattern: String = "", callback: HttpCallback) {
        use(AfterHandler(method, pattern, callback))
    }

    fun after(pattern: String, callback: HttpCallback) {
        use(AfterHandler(pattern, callback))
    }

    fun exception(
        exception: KClass<out Exception>? = null,
        status: HttpStatus? = null,
        callback: HttpCallback,
    ) {
        after(emptySet(), "*", exception, status, callback)
    }

    inline fun <reified T : Exception> exception(
        status: HttpStatus? = null,
        noinline callback: HttpCallback,
    ) {
        exception(T::class, status, callback)
    }

    fun get(pattern: String = "", callback: HttpCallback) {
        on(GET, pattern, callback)
    }

    fun head(pattern: String = "", callback: HttpCallback) {
        on(HEAD, pattern, callback)
    }

    fun post(pattern: String = "", callback: HttpCallback) {
        on(POST, pattern, callback)
    }

    fun put(pattern: String = "", callback: HttpCallback) {
        on(PUT, pattern, callback)
    }

    fun delete(pattern: String = "", callback: HttpCallback) {
        on(DELETE, pattern, callback)
    }

    fun trace(pattern: String = "", callback: HttpCallback) {
        on(TRACE, pattern, callback)
    }

    fun options(pattern: String = "", callback: HttpCallback) {
        on(OPTIONS, pattern, callback)
    }

    fun patch(pattern: String = "", callback: HttpCallback) {
        on(PATCH, pattern, callback)
    }
}
