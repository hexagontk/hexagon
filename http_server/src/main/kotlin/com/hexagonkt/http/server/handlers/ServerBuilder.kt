package com.hexagonkt.http.server.handlers

import com.hexagonkt.http.model.HttpMethod
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
        use(com.hexagonkt.http.server.handlers.on(predicate, callback))
    }

    fun on(
        methods: Set<HttpMethod> = emptySet(),
        pattern: String = "",
        exception: KClass<out Exception>? = null,
        status: HttpStatus? = null,
        callback: HttpCallback,
    ) {
        use(com.hexagonkt.http.server.handlers.on(methods, pattern, exception, status, callback))
    }

    fun on(method: HttpMethod, pattern: String = "", callback: HttpCallback) {
        use(com.hexagonkt.http.server.handlers.on(method, pattern, callback))
    }

    fun on(pattern: String, callback: HttpCallback) {
        use(com.hexagonkt.http.server.handlers.on(pattern, callback))
    }

    fun filter(
        predicate: HttpServerPredicate = HttpServerPredicate(),
        callback: HttpCallback
    ) {
        use(com.hexagonkt.http.server.handlers.filter(predicate, callback))
    }

    fun filter(
        methods: Set<HttpMethod> = emptySet(),
        pattern: String = "",
        exception: KClass<out Exception>? = null,
        status: HttpStatus? = null,
        callback: HttpCallback,
    ) {
        use(
            com.hexagonkt.http.server.handlers.filter(methods, pattern, exception, status, callback)
        )
    }

    fun filter(method: HttpMethod, pattern: String = "", callback: HttpCallback) {
        use(com.hexagonkt.http.server.handlers.filter(method, pattern, callback))
    }

    fun filter(pattern: String, callback: HttpCallback) {
        use(com.hexagonkt.http.server.handlers.filter(pattern, callback))
    }

    fun after(
        predicate: HttpServerPredicate = HttpServerPredicate(),
        callback: HttpCallback
    ) {
        use(com.hexagonkt.http.server.handlers.after(predicate, callback))
    }

    fun after(
        methods: Set<HttpMethod> = emptySet(),
        pattern: String = "",
        exception: KClass<out Exception>? = null,
        status: HttpStatus? = null,
        callback: HttpCallback,
    ) {
        use(com.hexagonkt.http.server.handlers.after(methods, pattern, exception, status, callback))
    }

    fun after(method: HttpMethod, pattern: String = "", callback: HttpCallback) {
        use(com.hexagonkt.http.server.handlers.after(method, pattern, callback))
    }

    fun after(pattern: String, callback: HttpCallback) {
        use(com.hexagonkt.http.server.handlers.after(pattern, callback))
    }

    fun <T : Exception> exception(
        exception: KClass<T>? = null,
        status: HttpStatus? = null,
        callback: HttpExceptionCallback<T>,
    ) {
        use(com.hexagonkt.http.server.handlers.exception(exception, status, callback))
    }

    inline fun <reified T : Exception> exception(
        status: HttpStatus? = null,
        noinline callback: HttpExceptionCallback<T>,
    ) {
        use(com.hexagonkt.http.server.handlers.exception(T::class, status, callback))
    }

    fun get(pattern: String = "", callback: HttpCallback) {
        use(com.hexagonkt.http.server.handlers.get(pattern, callback))
    }

    fun head(pattern: String = "", callback: HttpCallback) {
        use(com.hexagonkt.http.server.handlers.head(pattern, callback))
    }

    fun post(pattern: String = "", callback: HttpCallback) {
        use(com.hexagonkt.http.server.handlers.post(pattern, callback))
    }

    fun put(pattern: String = "", callback: HttpCallback) {
        use(com.hexagonkt.http.server.handlers.put(pattern, callback))
    }

    fun delete(pattern: String = "", callback: HttpCallback) {
        use(com.hexagonkt.http.server.handlers.delete(pattern, callback))
    }

    fun trace(pattern: String = "", callback: HttpCallback) {
        use(com.hexagonkt.http.server.handlers.trace(pattern, callback))
    }

    fun options(pattern: String = "", callback: HttpCallback) {
        use(com.hexagonkt.http.server.handlers.options(pattern, callback))
    }

    fun patch(pattern: String = "", callback: HttpCallback) {
        use(com.hexagonkt.http.server.handlers.patch(pattern, callback))
    }
}
