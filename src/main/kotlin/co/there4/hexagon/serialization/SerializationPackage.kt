package co.there4.hexagon.serialization

import co.there4.hexagon.serialization.SerializationFormat.*
import kotlin.reflect.KClass

val serializer = JacksonSerializer ()
val defaultFormat = JSON

fun Any.toMap (): Map<*, *> = serializer.convertToMap (this)
fun <T : Any> Map<*, *>.toObject (type: KClass<T>) = serializer.convertToObject(this, type)

fun Any.serialize () = serializer.serialize(defaultFormat, this)
fun <T : Any> String.parse (type: KClass<T>) = serializer.parse (defaultFormat, this, type)
fun <T : Any> String.parseList (type: KClass<T>) = serializer.parseList (defaultFormat, this, type)
