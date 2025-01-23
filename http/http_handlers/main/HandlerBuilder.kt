package com.hexagontk.http.handlers

import com.hexagontk.http.model.HttpMethod
import kotlin.reflect.KClass

class HandlerBuilder(var handlers: List<HttpHandler> = emptyList()) {

    fun handler(contextPath: String = ""): HttpHandler =
        if (handlers.size == 1) handlers[0].addPrefix(contextPath)
        else PathHandler(contextPath, handlers)

    fun use(handler: HttpHandler) {
        this.handlers += handler
    }

    fun path(pattern: String, block: HandlerBuilder.() -> Unit) {
        use(com.hexagontk.http.handlers.path(pattern, block))
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
        callback: HttpCallbackType
    ) {
        use(OnHandler(predicate, callback))
    }

    fun on(
        methods: Set<HttpMethod> = emptySet(),
        pattern: String = "",
        status: Int? = null,
        callback: HttpCallbackType,
    ) {
        use(OnHandler(methods, pattern, status, callback))
    }

    fun on(method: HttpMethod, pattern: String = "", callback: HttpCallbackType) {
        use(OnHandler(method, pattern, callback))
    }

    fun on(pattern: String, callback: HttpCallbackType) {
        use(OnHandler(pattern, callback))
    }

    fun filter(
        predicate: HttpPredicate = HttpPredicate(),
        callback: HttpCallbackType
    ) {
        use(FilterHandler(predicate, callback))
    }

    fun filter(
        methods: Set<HttpMethod> = emptySet(),
        pattern: String = "",
        status: Int? = null,
        callback: HttpCallbackType,
    ) {
        use(
            FilterHandler(methods, pattern, status, callback)
        )
    }

    fun filter(method: HttpMethod, pattern: String = "", callback: HttpCallbackType) {
        use(FilterHandler(method, pattern, callback))
    }

    fun filter(pattern: String, callback: HttpCallbackType) {
        use(FilterHandler(pattern, callback))
    }

    fun after(
        predicate: HttpPredicate = HttpPredicate(),
        callback: HttpCallbackType
    ) {
        use(AfterHandler(predicate, callback))
    }

    fun after(
        methods: Set<HttpMethod> = emptySet(),
        pattern: String = "",
        status: Int? = null,
        callback: HttpCallbackType,
    ) {
        use(AfterHandler(methods, pattern, status, callback))
    }

    fun after(method: HttpMethod, pattern: String = "", callback: HttpCallbackType) {
        use(AfterHandler(method, pattern, callback))
    }

    fun after(pattern: String, callback: HttpCallbackType) {
        use(AfterHandler(pattern, callback))
    }

    fun before(
        predicate: HttpPredicate = HttpPredicate(),
        callback: HttpCallbackType
    ) {
        use(BeforeHandler(predicate, callback))
    }

    fun before(
        methods: Set<HttpMethod> = emptySet(),
        pattern: String = "",
        status: Int? = null,
        callback: HttpCallbackType,
    ) {
        use(BeforeHandler(methods, pattern, status, callback))
    }

    fun before(method: HttpMethod, pattern: String = "", callback: HttpCallbackType) {
        use(BeforeHandler(method, pattern, callback))
    }

    fun before(pattern: String, callback: HttpCallbackType) {
        use(BeforeHandler(pattern, callback))
    }

    fun <T : Exception> exception(exception: KClass<T>, callback: HttpExceptionCallbackType<T>) {
        use(ExceptionHandler(exception, callback))
    }

    inline fun <reified T : Exception> exception(noinline callback: HttpExceptionCallbackType<T>) {
        use(ExceptionHandler(T::class, callback))
    }

    fun get(pattern: String = "", callback: HttpCallbackType) {
        use(Get(pattern, callback))
    }

    fun ws(pattern: String = "", callback: HttpCallbackType) {
        use(Ws(pattern, callback))
    }

    fun head(pattern: String = "", callback: HttpCallbackType) {
        use(Head(pattern, callback))
    }

    fun post(pattern: String = "", callback: HttpCallbackType) {
        use(Post(pattern, callback))
    }

    fun put(pattern: String = "", callback: HttpCallbackType) {
        use(Put(pattern, callback))
    }

    fun delete(pattern: String = "", callback: HttpCallbackType) {
        use(Delete(pattern, callback))
    }

    fun trace(pattern: String = "", callback: HttpCallbackType) {
        use(Trace(pattern, callback))
    }

    fun options(pattern: String = "", callback: HttpCallbackType) {
        use(Options(pattern, callback))
    }

    fun patch(pattern: String = "", callback: HttpCallbackType) {
        use(Patch(pattern, callback))
    }
}
