package co.there4.hexagon.serialization

import kotlin.reflect.KClass

fun Any.convertToMap(): Map<*, *> = JacksonSerializer.toMap (this)
fun <T : Any> Map<*, *>.convertToObject(type: KClass<T>) = JacksonSerializer.toObject(this, type)

fun Any.serialize () = JacksonSerializer.serialize(this)
fun <T : Any> String.parse (type: KClass<T>) = JacksonSerializer.parse (this, type)
fun <T : Any> String.parseList (type: KClass<T>) = JacksonSerializer.parseList (this, type)
