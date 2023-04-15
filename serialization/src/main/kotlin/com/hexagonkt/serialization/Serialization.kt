package com.hexagonkt.serialization

import com.hexagonkt.core.toStream
import com.hexagonkt.core.media.MediaType
import com.hexagonkt.core.media.mediaTypeOf
import com.hexagonkt.serialization.SerializationManager.formatOf
import com.hexagonkt.serialization.SerializationManager.requireDefaultFormat
import java.io.File
import java.io.InputStream
import java.net.URL

fun Any.serializeBytes(format: SerializationFormat = requireDefaultFormat()): ByteArray =
    format.serializeBytes(this)

fun Any.serializeBytes(mediaType: MediaType): ByteArray =
    this.serializeBytes(formatOf(mediaType))

fun Any.serialize(format: SerializationFormat = requireDefaultFormat()): String =
    format.serialize(this)

fun Any.serialize(mediaType: MediaType): String =
    this.serialize(formatOf(mediaType))

fun InputStream.parse(format: SerializationFormat = requireDefaultFormat()): Any =
    format.parse(this)

fun InputStream.parse(mediaType: MediaType): Any =
    parse(formatOf(mediaType))

fun String.parse(format: SerializationFormat = requireDefaultFormat()): Any =
    this.toStream().parse(format)

fun String.parse(mediaType: MediaType): Any =
    this.toStream().parse(mediaType)

fun File.parse(): Any =
    this.inputStream().parse(mediaTypeOf(this))

fun URL.parse(): Any =
    this.openStream().parse(mediaTypeOf(this))

fun URL.parseMap(): Map<String, *> =
    this.parse().castToMap()

fun URL.parseList(): List<*> =
    this.parse().castToList()

fun File.parseMap(): Map<String, *> =
    this.parse().castToMap()

fun File.parseList(): List<*> =
    this.parse().castToList()

fun String.parseMap(format: SerializationFormat = requireDefaultFormat()): Map<String, *> =
    this.parse(format).castToMap()

fun String.parseList(format: SerializationFormat = requireDefaultFormat()): List<*> =
    this.parse(format).castToList()

fun String.parseMap(mediaType: MediaType): Map<String, *> =
    this.parse(mediaType).castToMap()

fun String.parseList(mediaType: MediaType): List<*> =
    this.parse(mediaType).castToList()

fun InputStream.parseMap(format: SerializationFormat = requireDefaultFormat()): Map<String, *> =
    this.parse(format).castToMap()

fun InputStream.parseList(format: SerializationFormat = requireDefaultFormat()): List<*> =
    this.parse(format).castToList()

fun InputStream.parseMap(mediaType: MediaType): Map<String, *> =
    this.parse(mediaType).castToMap()

fun InputStream.parseList(mediaType: MediaType): List<*> =
    this.parse(mediaType).castToList()

fun <T> Any.toData(data: () -> Data<T>): List<T> =
    when (this) {
        is Map<*, *> -> listOf(this.castToMap().toData(data))
        is List<*> -> toData(data)
        else -> error("Instance of type: ${this::class.simpleName} cannot be transformed to data")
    }

fun <T> Map<String, *>.toData(data: () -> Data<T>): T =
    data().with(this)

fun <T> List<*>.toData(data: () -> Data<T>): List<T> =
    map { it.castToMap() }.map { it.toData(data) }

@Suppress("UNCHECKED_CAST") // Cast exception handled in function
private fun Any?.castToMap(): Map<String, *> =
    this as? Map<String, *> ?: error("$this cannot be cast to Map")

private fun Any?.castToList(): List<*> =
    this as? List<*> ?: error("$this cannot be cast to List")
