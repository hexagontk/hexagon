package com.hexagonkt.http.server

import com.hexagonkt.http.Method
import com.hexagonkt.serialization.SerializationFormat
import com.hexagonkt.serialization.SerializationManager
import com.hexagonkt.serialization.SerializationManager.defaultFormat
import com.hexagonkt.serialization.parse
import com.hexagonkt.serialization.parseObjects
import java.net.HttpCookie
import java.security.cert.X509Certificate
import kotlin.reflect.KClass

/**
 * Lists would be initialized loading all elements when they are used (set it as lazy in
 * implementations) this will have a performance penalty in favor of ease of use. The alternative
 * would be using a 'Map/List wrapper that delegates calls to abstract methods in the interface
 * (I won't do this just now).
 *
 * HTTP request context. It holds client supplied data and methods to change the response.
 */
abstract class Request {
    val method: Method by lazy { method() }
    val scheme: String by lazy { scheme() }
    val host: String by lazy { host() }
    val ip: String by lazy { ip() }
    val port: Int by lazy { port() }
    val path: String by lazy { path() }
    val queryString: String by lazy { queryString() }
    val url: String by lazy { url() }
    val parts: Map<String, Part> by lazy { parts() }
    val parameters: Map<String, List<String>> by lazy { parameters() }
    val queryParameters: Map<String, List<String>> by lazy { queryParameters() }
    val formParameters: Map<String, List<String>> by lazy { formParameters() }
    val pathParameters: Map<String, String> by lazy { pathParameters() }
    val certificateChain: List<X509Certificate> by lazy { certificateChain() }

    val secure: Boolean by lazy { scheme == "https" }
    val accept: List<String> by lazy { headers["Accept"] ?: emptyList() }
    val preferredType: String? by lazy { accept.firstOrNull() }
    val userAgent: String? by lazy { headers["User-Agent"]?.firstOrNull() }
    val referer: String? by lazy { headers["Referer"]?.firstOrNull() }
    val origin: String? by lazy { headers["Origin"]?.firstOrNull() }
    val certificate: X509Certificate? by lazy { certificateChain.firstOrNull() }

    val body: String by lazy { loadBody() }
    val headers: Map<String, List<String>> by lazy { headers() }
    val cookies: Map<String, HttpCookie> by lazy { cookies() }
    val contentType: String? by lazy { contentType() }
    val contentLength: Long by lazy { contentLength() }

    fun <T : Any> body(type: KClass<T>): T = body.parse(type, requestFormat())
    fun <T : Any> bodyObjects(type: KClass<T>): List<T> = body.parseObjects(type, requestFormat())

    inline fun <reified T : Any> body(): T = body(T::class)
    inline fun <reified T : Any> bodyObjects(): List<T> = bodyObjects(T::class)

    protected abstract fun method(): Method                      // "GET"
    protected abstract fun scheme(): String                      // "http"
    protected abstract fun host(): String                        // "example.com"
    protected abstract fun ip(): String                          // client IP address
    protected abstract fun port(): Int                           // 80
    protected abstract fun path(): String                        // "/foo" servlet path + path info
    protected abstract fun pathParameters(): Map<String, String> // ["p"] "p" path parameter
    protected abstract fun queryString(): String                 // ""
    protected abstract fun url(): String                         // "http://example.com/example/foo"
    protected abstract fun parts(): Map<String, Part>            // hash of multipart parts
    protected abstract fun parameters(): Map<String, List<String>> // ["p"] "p" query/form parameter
    protected abstract fun queryParameters(): Map<String, List<String>>
    protected abstract fun formParameters(): Map<String, List<String>>
    protected abstract fun certificateChain(): List<X509Certificate>

    protected abstract fun loadBody(): String                    // request body sent by the client
    protected abstract fun headers(): Map<String, List<String>>  // ["H"] // value of "H" header
    protected abstract fun cookies(): Map<String, HttpCookie>    // hash of browser cookies
    protected abstract fun contentType(): String?                // media type of request.body
    protected abstract fun contentLength(): Long                 // length of request.body

    internal fun requestType(): String =
        contentType ?: defaultFormat.contentType

    internal fun requestFormat(): SerializationFormat =
        SerializationManager.formatOf(requestType())
}
