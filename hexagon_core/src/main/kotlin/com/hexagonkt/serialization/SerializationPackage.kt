package com.hexagonkt.serialization

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.WRITE_DOC_START_MARKER
import com.hexagonkt.settings.SettingsManager.settings
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.net.URL
import java.nio.charset.StandardCharsets.UTF_8
import kotlin.reflect.KClass


/** List of formats. NOTE should be defined AFTER mapper definition to avoid runtime issues. */
private val formatList = listOf (
    JacksonTextFormat("json"),
    JacksonTextFormat("yaml") {
        with(YAMLFactory()) { configure(WRITE_DOC_START_MARKER, false) }
    }
)

private val formats = mapOf (*formatList.map { it.contentType to it }.toTypedArray())

val contentTypes = formatList.map { it.contentType }

val defaultFormat by lazy { settings["contentType"] as? String ?: contentTypes.first() }

internal fun getSerializationFormat (contentType: String) =
    formats[contentType] ?: error("$contentType not found")

fun serializeObject(obj: Any, contentType: String = defaultFormat) =
    getSerializationFormat (contentType).serialize(obj)

fun <T: Any> parseObject(input: InputStream, type: KClass<T>, contentType: String = defaultFormat) =
    getSerializationFormat (contentType).parse (input, type)

fun <T: Any> parseObjectList(
    input: InputStream, type: KClass<T>, contentType: String = defaultFormat) =
    getSerializationFormat (contentType).parseList (input, type)

fun Any.convertToMap(): Map<*, *> = JacksonSerializer.toMap (this)

fun <T : Any> Map<*, *>.convertToObject(type: KClass<T>): T =
    JacksonSerializer.toObject(this, type)

fun <T : Any> List<Map<*, *>>.convertToObjects(type: KClass<T>): List<T> =
    this.map { it: Map<*, *> -> it.convertToObject(type) }

fun Any.serialize (contentType: String = defaultFormat) =
    serializeObject(this, contentType)

fun <T : Any> InputStream.parse (type: KClass<T>, contentType: String = defaultFormat) =
    parseObject (this, type, contentType)
fun InputStream.parse (contentType: String = defaultFormat) = this.parse (Map::class, contentType)
fun <T : Any> InputStream.parseList (type: KClass<T>, contentType: String = defaultFormat) =
    parseObjectList (this, type, contentType)
fun InputStream.parseList (contentType: String = defaultFormat) =
    this.parseList (Map::class, contentType)

fun <T : Any> String.parse (type: KClass<T>, contentType: String = defaultFormat) =
    toStream(this).parse (type, contentType)
fun String.parse (contentType: String = defaultFormat) = this.parse (Map::class, contentType)
fun <T : Any> String.parseList (type: KClass<T>, contentType: String = defaultFormat) =
    toStream(this).parseList (type, contentType)
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

private fun toStream(text: String) = ByteArrayInputStream(text.toByteArray(UTF_8))
