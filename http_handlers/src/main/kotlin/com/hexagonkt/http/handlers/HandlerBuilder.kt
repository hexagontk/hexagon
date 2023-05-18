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
        predicate: HttpPredicate = HttpPredicate(),
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
        use(
            FilterHandler(methods, pattern, exception, status, callback)
        )
    }

    fun filter(method: HttpMethod, pattern: String = "", callback: HttpCallback) {
        use(FilterHandler(method, pattern, callback))
    }

    fun filter(pattern: String, callback: HttpCallback) {
        use(FilterHandler(pattern, callback))
    }

    fun after(
        predicate: HttpPredicate = HttpPredicate(),
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

    fun <T : Exception> exception(
        exception: KClass<T>? = null,
        status: HttpStatus? = null,
        callback: HttpExceptionCallback<T>,
    ) {
        use(Exception(exception, status, callback))
    }

    inline fun <reified T : Exception> exception(
        status: HttpStatus? = null,
        noinline callback: HttpExceptionCallback<T>,
    ) {
        use(Exception(T::class, status, callback))
    }

    fun get(pattern: String = "", callback: HttpCallback) {
        use(Get(pattern, callback))
    }

    fun ws(pattern: String = "", callback: HttpCallback) {
        use(Ws(pattern, callback))
    }

    fun head(pattern: String = "", callback: HttpCallback) {
        use(Head(pattern, callback))
    }

    fun post(pattern: String = "", callback: HttpCallback) {
        use(Post(pattern, callback))
    }

    fun put(pattern: String = "", callback: HttpCallback) {
        use(Put(pattern, callback))
    }

    fun delete(pattern: String = "", callback: HttpCallback) {
        use(Delete(pattern, callback))
    }

    fun trace(pattern: String = "", callback: HttpCallback) {
        use(Trace(pattern, callback))
    }

    fun options(pattern: String = "", callback: HttpCallback) {
        use(Options(pattern, callback))
    }

    fun patch(pattern: String = "", callback: HttpCallback) {
        use(Patch(pattern, callback))
    }
}
