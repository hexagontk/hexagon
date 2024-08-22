package com.hexagontk.serialization

import com.hexagontk.core.text.toStream
import com.hexagontk.core.media.MediaType
import com.hexagontk.core.media.mediaTypeOf
import com.hexagontk.serialization.SerializationManager.formatOf
import java.io.File
import java.io.InputStream
import java.net.URL
import java.nio.file.Path
import kotlin.io.path.inputStream

fun Any.serializeBytes(format: SerializationFormat): ByteArray =
    format.serializeBytes(this)

fun Any.serializeBytes(mediaType: MediaType): ByteArray =
    this.serializeBytes(formatOf(mediaType))

fun Any.serialize(format: SerializationFormat): String =
    format.serialize(this)

fun Any.serialize(mediaType: MediaType): String =
    this.serialize(formatOf(mediaType))

fun InputStream.parse(format: SerializationFormat): Any =
    format.parse(this)

fun InputStream.parse(mediaType: MediaType): Any =
    parse(formatOf(mediaType))

fun String.parse(format: SerializationFormat): Any =
    this.toStream().parse(format)

fun String.parse(mediaType: MediaType): Any =
    this.toStream().parse(mediaType)

fun File.parse(): Any =
    this.inputStream().parse(mediaTypeOf(this))

fun Path.parse(): Any =
    this.inputStream().parse(mediaTypeOf(this))

fun URL.parse(): Any =
    this.openStream().parse(mediaTypeOf(this))

fun File.parseMap(): Map<String, *> =
    this.parse().castToMap()

fun File.parseList(): List<*> =
    this.parse().castToList()

fun File.parseMaps(): List<Map<String, *>> =
    this.parseList().map(Any?::castToMap)

fun Path.parseMap(): Map<String, *> =
    this.parse().castToMap()

fun Path.parseList(): List<*> =
    this.parse().castToList()

fun Path.parseMaps(): List<Map<String, *>> =
    this.parseList().map(Any?::castToMap)

fun URL.parseMap(): Map<String, *> =
    this.parse().castToMap()

fun URL.parseList(): List<*> =
    this.parse().castToList()

fun URL.parseMaps(): List<Map<String, *>> =
    this.parseList().map(Any?::castToMap)

fun String.parseMap(format: SerializationFormat): Map<String, *> =
    this.parse(format).castToMap()

fun String.parseList(format: SerializationFormat): List<*> =
    this.parse(format).castToList()

fun String.parseMaps(format: SerializationFormat): List<Map<String, *>> =
    this.parseList(format).map(Any?::castToMap)

fun String.parseMap(mediaType: MediaType): Map<String, *> =
    this.parse(mediaType).castToMap()

fun String.parseList(mediaType: MediaType): List<*> =
    this.parse(mediaType).castToList()

fun String.parseMaps(mediaType: MediaType): List<Map<String, *>> =
    this.parseList(mediaType).map(Any?::castToMap)

fun InputStream.parseMap(format: SerializationFormat): Map<String, *> =
    this.parse(format).castToMap()

fun InputStream.parseList(format: SerializationFormat): List<*> =
    this.parse(format).castToList()

fun InputStream.parseMaps(format: SerializationFormat): List<Map<String, *>> =
    this.parseList(format).map(Any?::castToMap)

fun InputStream.parseMap(mediaType: MediaType): Map<String, *> =
    this.parse(mediaType).castToMap()

fun InputStream.parseList(mediaType: MediaType): List<*> =
    this.parse(mediaType).castToList()

fun InputStream.parseMaps(mediaType: MediaType): List<Map<String, *>> =
    this.parseList(mediaType).map(Any?::castToMap)

@Suppress("UNCHECKED_CAST") // Cast exception handled in function
private fun Any?.castToMap(): Map<String, *> =
    this as? Map<String, *> ?: error("$this cannot be cast to Map")

private fun Any?.castToList(): List<*> =
    this as? List<*> ?: error("$this cannot be cast to List")
