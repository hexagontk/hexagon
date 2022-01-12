package com.hexagonkt.http.model

import com.hexagonkt.core.MultiMap

interface HttpBase {
    // TODO Allow get by chunks with 'channel/flow'
    val body: Any
    val headers: MultiMap<String, String>  // ["H"] // value of "H" header
    val contentType: ContentType?          // media type of request.body

    fun bodyString(): String =
        when (body) {
            is String -> body as String
            is ByteArray -> String(body as ByteArray)
            else -> body.toString()
        }
}
