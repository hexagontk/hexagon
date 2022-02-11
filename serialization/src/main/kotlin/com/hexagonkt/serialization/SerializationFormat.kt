package com.hexagonkt.serialization

import com.hexagonkt.core.toStream
import com.hexagonkt.core.media.MediaType
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

interface SerializationFormat {

    private companion object {
        const val PARSING_ERROR = "String parsing only possible for text serialization formats"
        const val SERIALIZATION_ERROR =
            "String serialization only possible for text serialization formats"
    }

    val textFormat: Boolean
    val mediaType: MediaType

    fun serialize(instance: Any, output: OutputStream)
    fun parse(input: InputStream): Any

    fun serializeBytes(instance: Any): ByteArray =
        ByteArrayOutputStream().let {
            serialize(instance, it)
            it.toByteArray()
        }

    fun serialize(instance: Any): String {
        check(textFormat) { SERIALIZATION_ERROR }
        return String(serializeBytes(instance))
    }

    fun parse(input: String): Any {
        check(textFormat) { PARSING_ERROR }
        return parse(input.toStream())
    }
}
