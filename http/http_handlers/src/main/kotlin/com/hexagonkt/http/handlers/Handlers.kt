@file:Suppress("FunctionName") // Uppercase functions are used for providing named constructors

package com.hexagonkt.http.handlers

import com.hexagonkt.core.logging.Logger
import com.hexagonkt.handlers.Context
import com.hexagonkt.http.model.*
import com.hexagonkt.http.model.HttpMethod.*
import com.hexagonkt.http.model.HttpProtocol.HTTP
import com.hexagonkt.http.model.HttpCall
import com.hexagonkt.http.model.HttpRequest
import java.lang.IllegalStateException
import java.math.BigInteger
import java.security.cert.X509Certificate

typealias HttpCallback = HttpContext.() -> HttpContext
typealias HttpExceptionCallback<T> = HttpContext.(T) -> HttpContext

private val logger: Logger by lazy { Logger(HttpHandler::class.java.packageName) }
private val BODY_TYPES_NAMES: String by lazy {
    val bodyTypes = setOf(String::class, ByteArray::class, Int::class, Long::class)
    bodyTypes.joinToString(", ") { it.simpleName.toString() }
}

internal fun toCallback(block: HttpCallback): (Context<HttpCall>) -> Context<HttpCall> =
    { context -> HttpContext(context).block() }

internal fun <E : Exception> toCallback(
    block: HttpExceptionCallback<E>
): (Context<HttpCall>, E) -> Context<HttpCall> =
    { context, e -> HttpContext(context).block(e) }

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

fun bodyToBytes(body: Any): ByteArray =
    when (body) {
        is String -> body.toByteArray()
        is ByteArray -> body
        is Int -> BigInteger.valueOf(body.toLong()).toByteArray()
        is Long -> BigInteger.valueOf(body).toByteArray()
        else -> {
            val className = body.javaClass.simpleName
            val message = "Unsupported body type: $className. Must be: $BODY_TYPES_NAMES"
            val exception = IllegalStateException(message)

            logger.error(exception)
            throw exception
        }
    }
