package com.hexagonkt.http.handlers

import com.hexagonkt.handlers.Context
import com.hexagonkt.http.model.*
import com.hexagonkt.http.model.HttpMethod.*
import com.hexagonkt.http.model.HttpProtocol.HTTP
import com.hexagonkt.http.model.HttpCall
import com.hexagonkt.http.model.HttpRequest
import java.security.cert.X509Certificate
import kotlin.reflect.KClass
import kotlin.reflect.cast

typealias HttpCallback = HttpContext.() -> HttpContext
typealias HttpExceptionCallback<T> = HttpContext.(T) -> HttpContext

internal fun toCallback(block: HttpCallback): (Context<HttpCall>) -> Context<HttpCall> =
    { context -> HttpContext(context).block() }

fun HttpCallback.process(
    request: HttpRequest,
    attributes: Map<*, *> = emptyMap<Any, Any>()
): HttpContext =
    this(HttpContext(request = request, attributes = attributes))

fun HttpCallback.process(
    method: HttpMethod = GET,
    protocol: HttpProtocol = HTTP,
    host: String = "localhost",
    port: Int = 80,
    path: String = "",
    queryParameters: QueryParameters = QueryParameters(),
    headers: Headers = Headers(),
    body: Any = "",
    parts: List<HttpPart> = emptyList(),
    formParameters: FormParameters = FormParameters(),
    cookies: List<Cookie> = emptyList(),
    contentType: ContentType? = null,
    certificateChain: List<X509Certificate> = emptyList(),
    accept: List<ContentType> = emptyList(),
    contentLength: Long = -1L,
    attributes: Map<*, *> = emptyMap<Any, Any>(),
): HttpContext =
    this.process(
        HttpRequest(
            method,
            protocol,
            host,
            port,
            path,
            queryParameters,
            headers,
            body,
            parts,
            formParameters,
            cookies,
            contentType,
            certificateChain,
            accept,
            contentLength,
        ),
        attributes,
    )

fun path(pattern: String = "", block: HandlerBuilder.() -> Unit): PathHandler {
    val builder = HandlerBuilder()
    builder.block()
    return path(pattern, builder.handlers)
}

fun path(contextPath: String = "", handlers: List<HttpHandler>): PathHandler =
    handlers
        .let {
            if (it.size == 1 && it[0] is PathHandler)
                (it[0] as PathHandler).addPrefix(contextPath) as PathHandler
            else
                PathHandler(contextPath, it)
        }

fun on(
    predicate: HttpPredicate = HttpPredicate(),
    callback: HttpCallback
): OnHandler =
    OnHandler(predicate, callback)

fun on(
    methods: Set<HttpMethod> = emptySet(),
    pattern: String = "",
    exception: KClass<out Exception>? = null,
    status: HttpStatus? = null,
    callback: HttpCallback,
): OnHandler =
    OnHandler(methods, pattern, exception, status, callback)

fun on(method: HttpMethod, pattern: String = "", callback: HttpCallback): OnHandler =
    OnHandler(method, pattern, callback)

fun on(pattern: String, callback: HttpCallback): OnHandler =
    OnHandler(pattern, callback)

fun filter(
    predicate: HttpPredicate = HttpPredicate(),
    callback: HttpCallback
): FilterHandler =
    FilterHandler(predicate, callback)

fun filter(
    methods: Set<HttpMethod> = emptySet(),
    pattern: String = "",
    exception: KClass<out Exception>? = null,
    status: HttpStatus? = null,
    callback: HttpCallback,
): FilterHandler =
    FilterHandler(methods, pattern, exception, status, callback)

fun filter(method: HttpMethod, pattern: String = "", callback: HttpCallback): FilterHandler =
    FilterHandler(method, pattern, callback)

fun filter(pattern: String, callback: HttpCallback): FilterHandler =
    FilterHandler(pattern, callback)

fun after(
    predicate: HttpPredicate = HttpPredicate(),
    callback: HttpCallback
): AfterHandler =
    AfterHandler(predicate, callback)

fun after(
    methods: Set<HttpMethod> = emptySet(),
    pattern: String = "",
    exception: KClass<out Exception>? = null,
    status: HttpStatus? = null,
    callback: HttpCallback,
): AfterHandler =
    AfterHandler(methods, pattern, exception, status, callback)

fun after(method: HttpMethod, pattern: String = "", callback: HttpCallback): AfterHandler =
    AfterHandler(method, pattern, callback)

fun after(pattern: String, callback: HttpCallback): AfterHandler =
    AfterHandler(pattern, callback)

fun <T : Exception> exception(
    exception: KClass<T>? = null,
    status: HttpStatus? = null,
    callback: HttpExceptionCallback<T>,
): AfterHandler =
    after(emptySet(), "*", exception, status) {
        callback(this.exception.castException(exception))
    }

inline fun <reified T : Exception> exception(
    status: HttpStatus? = null,
    noinline callback: HttpExceptionCallback<T>,
): AfterHandler =
    exception(T::class, status, callback)

internal fun <T : Exception> Exception?.castException(exception: KClass<T>?) =
    this?.let { exception?.cast(this) } ?: error("Exception 'null' or incorrect type")

fun get(pattern: String = "", callback: HttpCallback): OnHandler =
    on(GET, pattern, callback)

fun ws(pattern: String = "", callback: HttpCallback): OnHandler =
    get(pattern, callback)

fun head(pattern: String = "", callback: HttpCallback): OnHandler =
    on(HEAD, pattern, callback)

fun post(pattern: String = "", callback: HttpCallback): OnHandler =
    on(POST, pattern, callback)

fun put(pattern: String = "", callback: HttpCallback): OnHandler =
    on(PUT, pattern, callback)

fun delete(pattern: String = "", callback: HttpCallback): OnHandler =
    on(DELETE, pattern, callback)

fun trace(pattern: String = "", callback: HttpCallback): OnHandler =
    on(TRACE, pattern, callback)

fun options(pattern: String = "", callback: HttpCallback): OnHandler =
    on(OPTIONS, pattern, callback)

fun patch(pattern: String = "", callback: HttpCallback): OnHandler =
    on(PATCH, pattern, callback)
