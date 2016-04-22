package co.there4.hexagon.serialization

import kotlin.reflect.KClass

interface SerializationFormat {
    val contentType: String

    fun serialize(obj: Any): String
    fun <T: Any> parse(text: String, type: KClass<T>): T
    fun <T: Any> parseList(text: String, type: KClass<T>): List<T>
}
