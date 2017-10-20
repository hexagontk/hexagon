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

// MAPPING /////////////////////////////////////////////////////////////////////////////////////////
fun Any.convertToMap(): Map<*, *> = mapper.convertValue (this, Map::class.java)

fun <T : Any> Map<*, *>.convertToObject(type: KClass<T>): T = mapper.convertValue(this, type.java)

fun <T : Any> List<Map<*, *>>.convertToObjects(type: KClass<T>): List<T> =
    this.map { it: Map<*, *> -> it.convertToObject(type) }

fun Any.serialize (format: SerializationFormat = defaultFormat): String = format.serialize(this)

// INPUT STREAM ////////////////////////////////////////////////////////////////////////////////////
fun <T : Any> InputStream.parse (type: KClass<T>, format: SerializationFormat = defaultFormat): T =
    format.parse(this, type)

fun InputStream.parse (format: SerializationFormat = defaultFormat): Map<*, *> =
    this.parse (Map::class, format)

fun <T : Any> InputStream.parseList (type: KClass<T>, format: SerializationFormat = defaultFormat) =
    format.parseList(this, type)

fun InputStream.parseList (format: SerializationFormat = defaultFormat): List<Map<*, *>> =
    this.parseList (Map::class, format)

// STRING //////////////////////////////////////////////////////////////////////////////////////////
fun <T : Any> String.parse (type: KClass<T>, format: SerializationFormat = defaultFormat): T =
    this.toStream().parse (type, format)

fun String.parse (format: SerializationFormat = defaultFormat): Map<*, *> =
    this.toStream().parse (format)

fun String.parseList(format: SerializationFormat = defaultFormat): List<Map<*, *>> =
    this.parseList (Map::class, format)

fun <T : Any> String.parseList (type: KClass<T>, format: SerializationFormat = defaultFormat) =
    this.toStream().parseList (type, format)

// FILE ////////////////////////////////////////////////////////////////////////////////////////////
fun <T : Any> File.parse (type: KClass<T>): T = this.inputStream().parse(type, contentType(this))

fun File.parse (): Map<*, *> = this.parse (Map::class)

fun File.parseList (): List<Map<*, *>> = this.parseList (Map::class)

fun <T : Any> File.parseList(type: KClass<T>): List<T> =
    this.inputStream().parseList(type, contentType(this))

// URL /////////////////////////////////////////////////////////////////////////////////////////////
fun <T : Any> URL.parse(type: KClass<T>): T = this.openStream().parse(type, contentType(this))

fun URL.parse (): Map<*, *> = this.parse (Map::class)

fun URL.parseList (): List<Map<*, *>> = this.parseList (Map::class)

fun <T : Any> URL.parseList(type: KClass<T>): List<T> =
    this.openStream().parseList(type, contentType(this))

// UTILITIES
private fun contentType(url: URL): SerializationFormat =
    getContentTypeFormat(mimeTypes.getContentType(url.file))

private fun contentType(file: File): SerializationFormat =
    getContentTypeFormat(mimeTypes.getContentType(file))
