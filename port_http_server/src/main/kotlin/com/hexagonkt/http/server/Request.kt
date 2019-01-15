package com.hexagonkt.http.server

import com.hexagonkt.http.Method
import java.net.HttpCookie

/**
 * Lists would be initialized loading all elements when they are used (set it as lazy in
 * implementations) this will have a performace penalty in favor of ease of use. The alternative
 * would be using a 'Map/List wrapper that delegates calls to abstract methods in the interface
 * (I won't do this just now).
 *
 * HTTP request context. It holds client supplied data and methods to change the response.
 */
class Request(private val request: EngineRequest) {
    val method: Method by lazy { request.method }
    val scheme: String by lazy { request.scheme }
    val host: String by lazy { request.host }
    val port: Int by lazy { request.port }
    val path: String by lazy { request.path }
    val queryString: String by lazy { request.queryString }
    val body: String by lazy { request.body }

    val contentLength: Long by lazy { request.contentLength }
    val contentType: String? by lazy { request.contentType }
    val url: String by lazy { request.url }
    val ip: String by lazy { request.ip }
    val accept: List<String> by lazy { headers["accept"] ?: emptyList() }
    val preferredType: String by lazy { accept.firstOrNull() ?: "text/plain" }

    val parameters: Map<String, List<String>> by lazy { request.parameters }
    val headers: Map<String, List<String>> by lazy { request.headers }
    val cookies: Map<String, HttpCookie> by lazy { request.cookies }
    val parts: Map<String, Part> by lazy { request.parts }

    operator fun get(name: String): String? = parameters[name]?.first()

    fun parameter(name: String): String =
        singleParameter(name)

    fun singleParameter(name: String): String =
        singleParameters[name] ?: error("'$name' parameter not found")

    val singleParameters: Map<String, String> by lazy { parameters.mapValues { it.value.first() } }
    val secure: Boolean by lazy { scheme == "https" }
    val userAgent: String by lazy { headers["User-Agent"]?.first() ?: "UNKNOWN" }
    val referer: String by lazy { headers["Referer"]?.first() ?: "UNKNOWN" }

//    fun parameter(name: String): List<String> =
//        parameters[name] ?: error("'$name' parameter not found")

    // parameters, singleParameters, headers, singleHeaders
}
