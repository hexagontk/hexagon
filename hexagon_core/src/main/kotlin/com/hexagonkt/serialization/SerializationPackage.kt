package com.hexagonkt.serialization

import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.WRITE_DOC_START_MARKER
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.hexagonkt.helpers.mimeTypes
import com.hexagonkt.helpers.toStream
import com.hexagonkt.serialization.JacksonHelper.mapper
import com.hexagonkt.serialization.SerializationManager.defaultFormat
import com.hexagonkt.serialization.SerializationManager.getContentTypeFormat
import java.io.File
import java.io.InputStream
import java.net.URL
import kotlin.reflect.KClass

object JsonFormat : SerializationFormat by JacksonTextFormat(linkedSetOf("json"))

object YamlFormat : SerializationFormat by JacksonTextFormat(linkedSetOf("yaml", "yml"), {
    with(YAMLFactory()) { configure(WRITE_DOC_START_MARKER, false) }
})

val coreFormats: LinkedHashSet<SerializationFormat> = linkedSetOf (JsonFormat, YamlFormat)

fun Any.convertToMap(): Map<*, *> = mapper.convertValue (this, Map::class.java)
fun <T : Any> Map<*, *>.convertToObject(type: KClass<T>): T = mapper.convertValue(this, type.java)

fun <T : Any> List<Map<*, *>>.convertToObjects(type: KClass<T>): List<T> =
    this.map { it: Map<*, *> -> it.convertToObject(type) }

fun Any.serialize (contentType: String = defaultFormat) =
    getContentTypeFormat(contentType).serialize(this)

fun <T : Any> InputStream.parse (type: KClass<T>, contentType: String = defaultFormat) =
    getContentTypeFormat(contentType).parse(this, type)
fun InputStream.parse (contentType: String = defaultFormat) = this.parse (Map::class, contentType)
fun <T : Any> InputStream.parseList (type: KClass<T>, contentType: String = defaultFormat) =
    getContentTypeFormat(contentType).parseList(this, type)
fun InputStream.parseList (contentType: String = defaultFormat) =
    this.parseList (Map::class, contentType)

fun <T : Any> String.parse (type: KClass<T>, contentType: String = defaultFormat) =
    this.toStream().parse (type, contentType)
fun String.parse (contentType: String = defaultFormat) = this.parse (Map::class, contentType)
fun String.parseList(contentType: String = defaultFormat) = this.parseList (Map::class, contentType)
fun <T : Any> String.parseList (type: KClass<T>, contentType: String = defaultFormat) =
    this.toStream().parseList (type, contentType)

fun <T : Any> File.parse (type: KClass<T>) = this.inputStream().parse(type, contentType(this))
fun File.parse () = this.parse (Map::class)
fun File.parseList () = this.parseList (Map::class)
fun <T : Any> File.parseList(type: KClass<T>): List<T> =
    this.inputStream().parseList(type, contentType(this))

fun <T : Any> URL.parse(type: KClass<T>) = this.openStream().parse(type, contentType(this))
fun URL.parse () = this.parse (Map::class)
fun URL.parseList () = this.parseList (Map::class)
fun <T : Any> URL.parseList(type: KClass<T>): List<T> =
    this.openStream().parseList(type, contentType(this))

private fun contentType(url: URL): String = mimeTypes.getContentType(url.file)
private fun contentType(file: File): String = mimeTypes.getContentType(file)
