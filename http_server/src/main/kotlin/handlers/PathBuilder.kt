package com.hexagonkt.http.server.handlers

import com.hexagonkt.http.model.HttpMethod
import com.hexagonkt.http.model.HttpMethod.*
import com.hexagonkt.http.model.HttpStatus
import kotlin.reflect.KClass

class PathBuilder(var handlers: List<HttpHandler> = emptyList()) {

    fun path(pattern: String, block: PathBuilder.() -> Unit) {
        this.handlers += com.hexagonkt.http.server.handlers.path(pattern, block)
    }

    fun path(pattern: String, vararg pathHandlers: HttpHandler) {
        this.handlers += PathHandler(pattern, pathHandlers.toList())
    }

    fun on(
        predicate: HttpServerPredicate = HttpServerPredicate(),
        callback: HttpCallback
    ) {
        this.handlers += OnHandler(predicate, callback)
    }

    fun on(
        methods: Set<HttpMethod> = emptySet(),
        pattern: String = "",
        exception: KClass<out Exception>? = null,
        status: HttpStatus? = null,
        callback: HttpCallback,
    ) {
        this.handlers += OnHandler(methods, pattern, exception, status, callback)
    }

    fun on(method: HttpMethod, pattern: String = "", callback: HttpCallback) {
        this.handlers += OnHandler(method, pattern, callback)
    }

    fun on(pattern: String, callback: HttpCallback) {
        this.handlers += OnHandler(pattern, callback)
    }

    fun filter(
        predicate: HttpServerPredicate = HttpServerPredicate(),
        callback: HttpCallback
    ) {
        this.handlers += FilterHandler(predicate, callback)
    }

    fun filter(
        methods: Set<HttpMethod> = emptySet(),
        pattern: String = "",
        exception: KClass<out Exception>? = null,
        status: HttpStatus? = null,
        callback: HttpCallback,
    ) {
        this.handlers += FilterHandler(methods, pattern, exception, status, callback)
    }

    fun filter(method: HttpMethod, pattern: String = "", callback: HttpCallback) {
        this.handlers += FilterHandler(method, pattern, callback)
    }

    fun filter(pattern: String, callback: HttpCallback) {
        this.handlers += FilterHandler(pattern, callback)
    }

    fun after(
        predicate: HttpServerPredicate = HttpServerPredicate(),
        callback: HttpCallback
    ) {
        this.handlers += AfterHandler(predicate, callback)
    }

    fun after(
        methods: Set<HttpMethod> = emptySet(),
        pattern: String = "",
        exception: KClass<out Exception>? = null,
        status: HttpStatus? = null,
        callback: HttpCallback,
    ) {
        this.handlers += AfterHandler(methods, pattern, exception, status, callback)
    }

    fun after(method: HttpMethod, pattern: String = "", callback: HttpCallback) {
        this.handlers += AfterHandler(method, pattern, callback)
    }

    fun after(pattern: String, callback: HttpCallback) {
        this.handlers += AfterHandler(pattern, callback)
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
