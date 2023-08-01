@file:Suppress("FunctionName") // Uppercase functions are used for providing named constructors

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

// TODO Optionally (with flag) clear exception after callback
fun <T : Exception> Exception(
    exception: KClass<T>? = null,
    status: HttpStatus? = null,
    callback: HttpExceptionCallback<T>,
): AfterHandler =
    AfterHandler(emptySet(), "*", exception, status) {
        callback(this.exception.castException(exception))
    }

inline fun <reified T : Exception> Exception(
    status: HttpStatus? = null,
    noinline callback: HttpExceptionCallback<T>,
): AfterHandler =
    Exception(T::class, status, callback)

internal fun <T : Exception> Exception?.castException(exception: KClass<T>?) =
    this?.let { exception?.cast(this) } ?: error("Exception 'null' or incorrect type")

fun Get(pattern: String = "", callback: HttpCallback): OnHandler =
    OnHandler(GET, pattern, callback)

fun Ws(pattern: String = "", callback: HttpCallback): OnHandler =
    Get(pattern, callback)

fun Head(pattern: String = "", callback: HttpCallback): OnHandler =
    OnHandler(HEAD, pattern, callback)

fun Post(pattern: String = "", callback: HttpCallback): OnHandler =
    OnHandler(POST, pattern, callback)

fun Put(pattern: String = "", callback: HttpCallback): OnHandler =
    OnHandler(PUT, pattern, callback)

fun Delete(pattern: String = "", callback: HttpCallback): OnHandler =
    OnHandler(DELETE, pattern, callback)

fun Trace(pattern: String = "", callback: HttpCallback): OnHandler =
    OnHandler(TRACE, pattern, callback)

fun Options(pattern: String = "", callback: HttpCallback): OnHandler =
    OnHandler(OPTIONS, pattern, callback)

fun Patch(pattern: String = "", callback: HttpCallback): OnHandler =
    OnHandler(PATCH, pattern, callback)
