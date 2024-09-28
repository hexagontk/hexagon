package com.hexagontk.http.model

interface HttpBase {
    val body: Any
    // TODO Headers can be 'core.Node'???
    val headers: Headers
    val contentType: ContentType?

    fun bodyString(): String =
        when (body) {
            is String -> body as String
            is ByteArray -> String(body as ByteArray)
            else -> body.toString()
        }
}
