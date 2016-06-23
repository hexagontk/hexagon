package co.there4.hexagon.serialization

import java.io.InputStream
import kotlin.reflect.KClass

val defaultFormat = JacksonSerializer.contentTypes.first()

fun Any.convertToMap(): Map<*, *> = JacksonSerializer.toMap (this)
fun <T : Any> Map<*, *>.convertToObject(type: KClass<T>) = JacksonSerializer.toObject(this, type)

fun Any.serialize (contentType: String = defaultFormat) =
    JacksonSerializer.serialize(this, contentType)

fun <T : Any> String.parse (type: KClass<T>, contentType: String = defaultFormat) =
    JacksonSerializer.parse (this, type, contentType)
fun <T : Any> String.parseList (type: KClass<T>, contentType: String = defaultFormat) =
    JacksonSerializer.parseList (this, type, contentType)

fun <T : Any> InputStream.parse (type: KClass<T>, contentType: String = defaultFormat) =
    JacksonSerializer.parse (this, type, contentType)
fun <T : Any> InputStream.parseList (type: KClass<T>, contentType: String = defaultFormat) =
    JacksonSerializer.parseList (this, type, contentType)
