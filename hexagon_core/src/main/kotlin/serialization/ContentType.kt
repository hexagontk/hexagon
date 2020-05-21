package com.hexagonkt.serialization

import java.nio.charset.Charset

data class ContentType(val format: SerializationFormat, val charset: Charset? = null) {
    override fun toString(): String =
        if (charset == null)
            format.contentType
        else
            "${format.contentType};charset=${charset.name()}"
}
