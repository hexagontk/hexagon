package com.hexagonkt.serialization

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import kotlin.reflect.KClass

interface SerializationFormat {
    val contentType: String
    val extensions: Set<String>
    val isBinary: Boolean

    fun serialize(obj: Any, output: OutputStream)

    fun <T: Any> parse(input: InputStream, type: KClass<T>): T
    fun <T: Any> parseObjects(input: InputStream, type: KClass<T>): List<T>

    fun serialize(obj: Any): String =
        if (isBinary) error("$contentType is a binary format")
        else ByteArrayOutputStream().let {
            serialize(obj, it)
            it.toString()
        }
}
