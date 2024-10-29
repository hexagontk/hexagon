package com.hexagontk.http.model

interface HttpBase {
    val body: Any
    val headers: Headers
    val contentType: ContentType?

    fun contentType(): ContentType? =
        headers["content-type"] as? ContentType

    fun bodyString(): String =
        when (body) {
            is String -> body as String
            is ByteArray -> String(body as ByteArray)
            else -> body.toString()
        }
}
