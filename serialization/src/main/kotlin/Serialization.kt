package com.hexagonkt.serialization

import com.hexagonkt.core.helpers.toStream
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
