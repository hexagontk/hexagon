package com.hexagonkt.http.model

interface HttpBase {
    val body: Any
    val headers: Headers
    val contentType: ContentType?

    fun bodyString(): String =
        when (body) {
            is String -> body as String
            is ByteArray -> String(body as ByteArray)
            else -> body.toString()
        }
}
