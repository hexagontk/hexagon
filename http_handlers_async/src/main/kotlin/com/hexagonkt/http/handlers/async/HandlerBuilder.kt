package com.hexagonkt.http.handlers.async

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
        use(com.hexagonkt.http.handlers.async.path(pattern, block))
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
        use(com.hexagonkt.http.handlers.async.on(predicate, callback))
    }

    fun on(
        methods: Set<HttpMethod> = emptySet(),
        pattern: String = "",
        exception: KClass<out Exception>? = null,
        status: HttpStatus? = null,
        callback: HttpCallback,
    ) {
        use(
            com.hexagonkt.http.handlers.async.on(
                methods,
                pattern,
                exception,
                status,
                callback
            )
        )
    }

    fun on(method: HttpMethod, pattern: String = "", callback: HttpCallback) {
        use(com.hexagonkt.http.handlers.async.on(method, pattern, callback))
    }

    fun on(pattern: String, callback: HttpCallback) {
        use(com.hexagonkt.http.handlers.async.on(pattern, callback))
    }

    fun filter(
        predicate: HttpPredicate = HttpPredicate(),
        callback: HttpCallback
    ) {
        use(com.hexagonkt.http.handlers.async.filter(predicate, callback))
    }

    fun filter(
        methods: Set<HttpMethod> = emptySet(),
        pattern: String = "",
        exception: KClass<out Exception>? = null,
        status: HttpStatus? = null,
        callback: HttpCallback,
    ) {
        use(
            com.hexagonkt.http.handlers.async.filter(
                methods,
                pattern,
                exception,
                status,
                callback
            )
        )
    }

    fun filter(method: HttpMethod, pattern: String = "", callback: HttpCallback) {
        use(com.hexagonkt.http.handlers.async.filter(method, pattern, callback))
    }

    fun filter(pattern: String, callback: HttpCallback) {
        use(com.hexagonkt.http.handlers.async.filter(pattern, callback))
    }

    fun after(
        predicate: HttpPredicate = HttpPredicate(),
        callback: HttpCallback
    ) {
        use(com.hexagonkt.http.handlers.async.after(predicate, callback))
    }

    fun after(
        methods: Set<HttpMethod> = emptySet(),
        pattern: String = "",
        exception: KClass<out Exception>? = null,
        status: HttpStatus? = null,
        callback: HttpCallback,
    ) {
        use(
            com.hexagonkt.http.handlers.async.after(
                methods,
                pattern,
                exception,
                status,
                callback
            )
        )
    }

    fun after(method: HttpMethod, pattern: String = "", callback: HttpCallback) {
        use(com.hexagonkt.http.handlers.async.after(method, pattern, callback))
    }

    fun after(pattern: String, callback: HttpCallback) {
        use(com.hexagonkt.http.handlers.async.after(pattern, callback))
    }

    fun <T : Exception> exception(
        exception: KClass<T>? = null,
        status: HttpStatus? = null,
        callback: HttpExceptionCallback<T>,
    ) {
        use(com.hexagonkt.http.handlers.async.exception(exception, status, callback))
    }

    inline fun <reified T : Exception> exception(
        status: HttpStatus? = null,
        noinline callback: HttpExceptionCallback<T>,
    ) {
        use(com.hexagonkt.http.handlers.async.exception(T::class, status, callback))
    }

    fun get(pattern: String = "", callback: HttpCallback) {
        use(com.hexagonkt.http.handlers.async.get(pattern, callback))
    }

    fun ws(pattern: String = "", callback: HttpCallback) {
        use(com.hexagonkt.http.handlers.async.ws(pattern, callback))
    }

    fun head(pattern: String = "", callback: HttpCallback) {
        use(com.hexagonkt.http.handlers.async.head(pattern, callback))
    }

    fun post(pattern: String = "", callback: HttpCallback) {
        use(com.hexagonkt.http.handlers.async.post(pattern, callback))
    }

    fun put(pattern: String = "", callback: HttpCallback) {
        use(com.hexagonkt.http.handlers.async.put(pattern, callback))
    }

    fun delete(pattern: String = "", callback: HttpCallback) {
        use(com.hexagonkt.http.handlers.async.delete(pattern, callback))
    }

    fun trace(pattern: String = "", callback: HttpCallback) {
        use(com.hexagonkt.http.handlers.async.trace(pattern, callback))
    }

    fun options(pattern: String = "", callback: HttpCallback) {
        use(com.hexagonkt.http.handlers.async.options(pattern, callback))
    }

    fun patch(pattern: String = "", callback: HttpCallback) {
        use(com.hexagonkt.http.handlers.async.patch(pattern, callback))
    }
}
