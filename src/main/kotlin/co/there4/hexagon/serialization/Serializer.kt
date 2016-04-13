package co.there4.hexagon.serialization

import kotlin.reflect.KClass

interface Serializer {
    fun convertToMap(obj: Any): Map<*, *>
    fun <T : Any> convertToObject(obj: Map<*, *>, type: KClass<T>): T

    fun serialize (format: SerializationFormat, obj: Any): String
    fun <T : Any> parse (format: SerializationFormat, text: String, type: KClass<T>): T
    fun <T : Any> parseList (format: SerializationFormat, text: String, type: KClass<T>): List<T>
}
