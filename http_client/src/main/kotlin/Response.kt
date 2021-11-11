package com.hexagonkt.http.client

import java.io.InputStream

/**
 * HTTP response fetched from a server request.
 */
data class Response <T> (
    var status: Int,
    var body: T?,
    val headers: MutableMap<String, List<String>>,
    var contentType: String?,
    val inputStream: InputStream
)
