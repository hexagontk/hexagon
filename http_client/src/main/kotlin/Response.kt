package com.hexagonkt.http.client

import java.io.InputStream

/**
 * HTTP response fetched from a server request.
 */
data class Response(
    var status: Int,
    var body: String?, // TODO Change by generic T
    val headers: MutableMap<String, List<String>>,
    var contentType: String?,
    val inputStream: InputStream
)
