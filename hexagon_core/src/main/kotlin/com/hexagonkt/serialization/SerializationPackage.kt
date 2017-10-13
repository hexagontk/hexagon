package com.hexagonkt.serialization

import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.WRITE_DOC_START_MARKER
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.hexagonkt.helpers.toStream
import com.hexagonkt.serialization.JacksonSerializer.mapper
import com.hexagonkt.serialization.SerializationManager.defaultFormat
import com.hexagonkt.serialization.SerializationManager.getFormat
import java.io.File
import java.io.InputStream
import java.net.URL
import kotlin.reflect.KClass

object JsonFormat : SerializationFormat by JacksonTextFormat("json")

object YamlFormat : SerializationFormat by JacksonTextFormat("yaml", {
    with(YAMLFactory()) { configure(WRITE_DOC_START_MARKER, false) }
})

val coreFormats: LinkedHashSet<SerializationFormat> = linkedSetOf (JsonFormat, YamlFormat)

fun Any.convertToMap(): Map<*, *> = mapper.convertValue (this, Map::class.java)
fun <T : Any> Map<*, *>.convertToObject(type: KClass<T>): T = mapper.convertValue(this, type.java)

fun <T : Any> List<Map<*, *>>.convertToObjects(type: KClass<T>): List<T> =
    this.map { it: Map<*, *> -> it.convertToObject(type) }

fun Any.serialize (contentType: String = defaultFormat) = getFormat(contentType).serialize(this)

fun <T : Any> InputStream.parse (type: KClass<T>, contentType: String = defaultFormat) =
    getFormat(contentType).parse(this, type)
fun InputStream.parse (contentType: String = defaultFormat) = this.parse (Map::class, contentType)
fun <T : Any> InputStream.parseList (type: KClass<T>, contentType: String = defaultFormat) =
    getFormat(contentType).parseList(this, type)
fun InputStream.parseList (contentType: String = defaultFormat) =
    this.parseList (Map::class, contentType)

fun <T : Any> String.parse (type: KClass<T>, contentType: String = defaultFormat) =
    this.toStream().parse (type, contentType)
fun String.parse (contentType: String = defaultFormat) = this.parse (Map::class, contentType)
fun <T : Any> String.parseList (type: KClass<T>, contentType: String = defaultFormat) =
    this.toStream().parseList (type, contentType)
fun String.parseList(contentType: String = defaultFormat) = this.parseList (Map::class, contentType)

fun <T : Any> File.parse (type: KClass<T>) =
    this.inputStream().parse (type, "application/" + this.extension)
fun File.parse () = this.parse (Map::class)
fun <T : Any> File.parseList (type: KClass<T>): List<T> =
    this.inputStream().parseList (type, "application/" + this.extension)
fun File.parseList () = this.parseList (Map::class)

fun <T : Any> URL.parse (type: KClass<T>) =
    this.openStream().parse (type, "application/" + this.file.substringAfterLast('.'))
fun URL.parse () = this.parse (Map::class)
fun <T : Any> URL.parseList (type: KClass<T>): List<T> =
    this.openStream().parseList (type, "application/" + this.file.substringAfterLast('.'))
fun URL.parseList () = this.parseList (Map::class)
