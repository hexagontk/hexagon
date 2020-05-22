package com.hexagonkt.http.client

import java.io.InputStream

/**
 * Check 'Request' comment. And also note that lists should be updated by engines after callback
 * is processed, this data is like a buffer that needs to be dumped to the real response.
 *
 * HTTP response context.
 */
data class Response(
    var status: Int,
    var body: String?, // TODO Change by generic T
    val headers: MutableMap<String, List<String>>,
    var contentType: String?,
    val inputStream: InputStream
)
