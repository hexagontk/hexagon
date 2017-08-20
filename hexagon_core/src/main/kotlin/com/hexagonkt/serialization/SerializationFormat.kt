package com.hexagonkt.serialization

import java.io.InputStream
import kotlin.reflect.KClass

internal interface SerializationFormat {
    val contentType: String

    fun serialize(obj: Any): String

    fun <T: Any> parse(input: InputStream, type: KClass<T>): T
    fun <T: Any> parseList(input: InputStream, type: KClass<T>): List<T>
}
