package com.hexagonkt.http.server

import com.hexagonkt.helpers.RequiredKeysMap
import com.hexagonkt.http.Method
import java.net.HttpCookie

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
    val pathParameters: RequiredKeysMap<String, String> by lazy { RequiredKeysMap(pathParameters()) }
    val queryString: String by lazy { queryString() }
    val url: String by lazy { url() }
    val parameters: Map<String, List<String>> by lazy { parameters() }
    val parts: Map<String, Part> by lazy { parts() }

    val accept: List<String> by lazy { headers["accept"] ?: emptyList() }
    val preferredType: String by lazy { accept.firstOrNull() ?: "text/plain" }
    val secure: Boolean by lazy { scheme == "https" }
    val userAgent: String by lazy { headers["User-Agent"]?.firstOrNull() ?: "UNKNOWN" }
    val referer: String by lazy { headers["Referer"]?.firstOrNull() ?: "UNKNOWN" }

    val body: String by lazy { body() }
    val headers: Map<String, List<String>> by lazy { headers() }
    val cookies: Map<String, HttpCookie> by lazy { cookies() }
    val contentType: String? by lazy { contentType() }
    val contentLength: Long by lazy { contentLength() }

    protected abstract fun method(): Method        // "GET"
    protected abstract fun scheme(): String        // "http"
    protected abstract fun host(): String          // "example.com"
    protected abstract fun ip(): String            // client IP address
    protected abstract fun port(): Int             // 80
    protected abstract fun path(): String          // .path // "/foo" (servlet path + path info)
    protected abstract fun pathParameters(): Map<String, String>   // ["some_param"] // some_param path parameter
    protected abstract fun queryString(): String   // ""
    protected abstract fun url(): String           // "http://example.com/example/foo"
    protected abstract fun parameters(): Map<String, List<String>> // ["some_param"] // some_param query/form parameter
    protected abstract fun parts(): Map<String, Part>              // hash of multipart parts

    protected abstract fun body(): String          // request body sent by the client
    protected abstract fun headers(): Map<String, List<String>>    // ["SOME_HEADER"] // value of SOME_HEADER header
    protected abstract fun cookies(): Map<String, HttpCookie>      // hash of browser cookies
    protected abstract fun contentType(): String?  // media type of request.body
    protected abstract fun contentLength(): Long   // length of request.body
}
