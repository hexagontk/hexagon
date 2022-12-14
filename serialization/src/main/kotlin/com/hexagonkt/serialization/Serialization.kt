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

fun Any.serialize(file: File): String =
    this.serialize(mediaTypeOf(file)).apply(file::writeText)

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

fun URL.parseMap(): Map<*, *> =
    this.parse().castToMap()

fun URL.parseList(): List<*> =
    this.parse().castToList()

fun File.parseMap(): Map<*, *> =
    this.parse().castToMap()

fun File.parseList(): List<*> =
    this.parse().castToList()

fun String.parseMap(format: SerializationFormat = requireDefaultFormat()): Map<*, *> =
    this.parse(format).castToMap()

fun String.parseList(format: SerializationFormat = requireDefaultFormat()): List<*> =
    this.parse(format).castToList()

fun String.parseMap(mediaType: MediaType): Map<*, *> =
    this.parse(mediaType).castToMap()

fun String.parseList(mediaType: MediaType): List<*> =
    this.parse(mediaType).castToList()

fun InputStream.parseMap(format: SerializationFormat = requireDefaultFormat()): Map<*, *> =
    this.parse(format).castToMap()

fun InputStream.parseList(format: SerializationFormat = requireDefaultFormat()): List<*> =
    this.parse(format).castToList()

fun InputStream.parseMap(mediaType: MediaType): Map<*, *> =
    this.parse(mediaType).castToMap()

fun InputStream.parseList(mediaType: MediaType): List<*> =
    this.parse(mediaType).castToList()

private fun Any?.castToMap(): Map<*, *> =
    this as? Map<*, *> ?: error("$this cannot be cast to Map")

private fun Any?.castToList(): List<*> =
    this as? List<*> ?: error("$this cannot be cast to List")
