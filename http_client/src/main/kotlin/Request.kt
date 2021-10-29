package com.hexagonkt.http.client

import com.hexagonkt.http.Method
import com.hexagonkt.http.Part
import com.hexagonkt.http.Path

/**
 * HTTP request send to the server.
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
) {
    constructor(
        method: Method,
        path: String,
        body: Any? = null,
        headers: Map<String, List<String>> = emptyMap(),
        pathParameters: Map<String, List<String>> = emptyMap(),
        formParameters: Map<String, List<String>> = emptyMap(),
        parts: Map<String, Part> = emptyMap(),
        contentType: String? = null
    ) : this (
        method,
        Path(path),
        body,
        headers,
        pathParameters,
        formParameters,
        parts,
        contentType
    )
}
