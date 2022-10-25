package com.hexagonkt.http.model

interface HttpBase {
    // TODO Allow get by chunks with 'channel/flow'
    val body: Any
    val headers: Headers  // ["H"] // value of "H" header
    val contentType: ContentType?    // media type of request.body

    fun bodyString(): String =
        when (body) {
            is String -> body as String
            is ByteArray -> String(body as ByteArray)
            else -> body.toString()
        }
}
