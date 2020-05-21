package com.hexagonkt.http.client

import com.hexagonkt.http.Method
import com.hexagonkt.http.Part
import com.hexagonkt.http.Path

/**
 * Lists would be initialized loading all elements when they are used (set it as lazy in
 * implementations) this will have a performance penalty in favor of ease of use. The alternative
 * would be using a 'Map/List wrapper that delegates calls to abstract methods in the interface
 * (I won't do this just now).
 *
 * HTTP request context. It holds client supplied data and methods to change the response.
 */
data class Request(
    val method: Method,
    val path: Path,
    val body: Any? = null,
    val headers: Map<String, List<String>> = emptyMap(),
    val pathParameters: Map<String, List<String>> = emptyMap(),
    val formParameters: Map<String, List<String>> = emptyMap(),
    val parts: Map<String, Part> = emptyMap(),
    val contentType: String? = null
)
