package com.hexagonkt.http.handlers

import com.hexagonkt.http.model.HttpMethod
import com.hexagonkt.http.model.HttpStatus
import kotlin.reflect.KClass

class HandlerBuilder(var handlers: List<HttpHandler> = emptyList()) {

    fun handler(contextPath: String = ""): HttpHandler =
        if (handlers.size == 1) handlers[0].addPrefix(contextPath)
        else PathHandler(contextPath, handlers)

    fun use(handler: HttpHandler) {
        this.handlers += handler
    }

    fun path(pattern: String, block: HandlerBuilder.() -> Unit) {
        use(com.hexagonkt.http.handlers.path(pattern, block))
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
        predicate: HttpPredicate = HttpPredicate(),
        callback: HttpCallback
    ) {
        use(com.hexagonkt.http.handlers.on(predicate, callback))
    }

    fun on(
        methods: Set<HttpMethod> = emptySet(),
        pattern: String = "",
        exception: KClass<out Exception>? = null,
        status: HttpStatus? = null,
        callback: HttpCallback,
    ) {
        use(com.hexagonkt.http.handlers.on(methods, pattern, exception, status, callback))
    }

    fun on(method: HttpMethod, pattern: String = "", callback: HttpCallback) {
        use(com.hexagonkt.http.handlers.on(method, pattern, callback))
    }

    fun on(pattern: String, callback: HttpCallback) {
        use(com.hexagonkt.http.handlers.on(pattern, callback))
    }

    fun filter(
        predicate: HttpPredicate = HttpPredicate(),
        callback: HttpCallback
    ) {
        use(com.hexagonkt.http.handlers.filter(predicate, callback))
    }

    fun filter(
        methods: Set<HttpMethod> = emptySet(),
        pattern: String = "",
        exception: KClass<out Exception>? = null,
        status: HttpStatus? = null,
        callback: HttpCallback,
    ) {
        use(
            com.hexagonkt.http.handlers.filter(methods, pattern, exception, status, callback)
        )
    }

    fun filter(method: HttpMethod, pattern: String = "", callback: HttpCallback) {
        use(com.hexagonkt.http.handlers.filter(method, pattern, callback))
    }

    fun filter(pattern: String, callback: HttpCallback) {
        use(com.hexagonkt.http.handlers.filter(pattern, callback))
    }

    fun after(
        predicate: HttpPredicate = HttpPredicate(),
        callback: HttpCallback
    ) {
        use(com.hexagonkt.http.handlers.after(predicate, callback))
    }

    fun after(
        methods: Set<HttpMethod> = emptySet(),
        pattern: String = "",
        exception: KClass<out Exception>? = null,
        status: HttpStatus? = null,
        callback: HttpCallback,
    ) {
        use(com.hexagonkt.http.handlers.after(methods, pattern, exception, status, callback))
    }

    fun after(method: HttpMethod, pattern: String = "", callback: HttpCallback) {
        use(com.hexagonkt.http.handlers.after(method, pattern, callback))
    }

    fun after(pattern: String, callback: HttpCallback) {
        use(com.hexagonkt.http.handlers.after(pattern, callback))
    }

    fun <T : Exception> exception(
        exception: KClass<T>? = null,
        status: HttpStatus? = null,
        callback: HttpExceptionCallback<T>,
    ) {
        use(com.hexagonkt.http.handlers.exception(exception, status, callback))
    }

    inline fun <reified T : Exception> exception(
        status: HttpStatus? = null,
        noinline callback: HttpExceptionCallback<T>,
    ) {
        use(com.hexagonkt.http.handlers.exception(T::class, status, callback))
    }

    fun get(pattern: String = "", callback: HttpCallback) {
        use(com.hexagonkt.http.handlers.get(pattern, callback))
    }

    fun ws(pattern: String = "", callback: HttpCallback) {
        use(com.hexagonkt.http.handlers.ws(pattern, callback))
    }

    fun head(pattern: String = "", callback: HttpCallback) {
        use(com.hexagonkt.http.handlers.head(pattern, callback))
    }

    fun post(pattern: String = "", callback: HttpCallback) {
        use(com.hexagonkt.http.handlers.post(pattern, callback))
    }

    fun put(pattern: String = "", callback: HttpCallback) {
        use(com.hexagonkt.http.handlers.put(pattern, callback))
    }

    fun delete(pattern: String = "", callback: HttpCallback) {
        use(com.hexagonkt.http.handlers.delete(pattern, callback))
    }

    fun trace(pattern: String = "", callback: HttpCallback) {
        use(com.hexagonkt.http.handlers.trace(pattern, callback))
    }

    fun options(pattern: String = "", callback: HttpCallback) {
        use(com.hexagonkt.http.handlers.options(pattern, callback))
    }

    fun patch(pattern: String = "", callback: HttpCallback) {
        use(com.hexagonkt.http.handlers.patch(pattern, callback))
    }
}
