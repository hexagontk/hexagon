package com.hexagonkt.serialization

import com.hexagonkt.helpers.toStream
import com.hexagonkt.serialization.SerializationManager.formatOf
import com.hexagonkt.serialization.SerializationManager.requireDefaultFormat
import com.hexagonkt.serialization.SerializationManager.requireMapper
import java.io.File
import java.io.InputStream
import java.net.URL
import kotlin.reflect.KClass

// MAPPING /////////////////////////////////////////////////////////////////////////////////////////
// TODO Use toFieldsMap and toObject
fun Any.toFieldsMap(): Map<*, *> =
    requireMapper().toFieldsMap(this)

fun <T : Any> Map<*, *>.toObject(type: KClass<T>): T =
    requireMapper().toObject(this, type)

inline fun <reified T : Any> Map<*, *>.toObject(): T =
    this.toObject(T::class)

fun Any.serialize(format: SerializationFormat = requireDefaultFormat()): String =
    format.serialize(this)

fun Any.serialize(contentType: String): String =
    this.serialize(formatOf(contentType))

// INPUT STREAM ////////////////////////////////////////////////////////////////////////////////////
fun <T : Any> InputStream.parse(
    type: KClass<T>, format: SerializationFormat = requireDefaultFormat()
): T =
    format.parse(this, type)

fun <T : Any> InputStream.parseObjects(
    type: KClass<T>, format: SerializationFormat = requireDefaultFormat()) =
        format.parseObjects(this, type)

inline fun <reified T : Any> InputStream.parse(
    format: SerializationFormat = requireDefaultFormat()
): T =
    this.parse(T::class, format)

inline fun <reified T : Any> InputStream.parseObjects(
    format: SerializationFormat = requireDefaultFormat()
) =
    this.parseObjects(T::class, format)

// STRING //////////////////////////////////////////////////////////////////////////////////////////
fun <T : Any> String.parse(
    type: KClass<T>, format: SerializationFormat = requireDefaultFormat()
): T =
    this.toStream().parse(type, format)

fun <T : Any> String.parseObjects(
    type: KClass<T>, format: SerializationFormat = requireDefaultFormat()
) =
    this.toStream().parseObjects(type, format)

inline fun <reified T : Any> String.parse(format: SerializationFormat = requireDefaultFormat()): T =
    this.parse(T::class, format)

inline fun <reified T : Any> String.parseObjects(
    format: SerializationFormat = requireDefaultFormat()
) =
    this.parseObjects(T::class, format)

// FILE ////////////////////////////////////////////////////////////////////////////////////////////
fun <T : Any> File.parse(type: KClass<T>): T =
    this.inputStream().parse(type, formatOf(this))

fun <T : Any> File.parseObjects(type: KClass<T>): List<T> =
    this.inputStream().parseObjects(type, formatOf(this))

inline fun <reified T : Any> File.parse(): T =
    this.parse(T::class)

inline fun <reified T : Any> File.parseObjects(): List<T> =
    this.parseObjects(T::class)

// URL /////////////////////////////////////////////////////////////////////////////////////////////
fun <T : Any> URL.parse(type: KClass<T>): T =
    this.openStream().parse(type, formatOf(this))

fun <T : Any> URL.parseObjects(type: KClass<T>): List<T> =
    this.openStream().parseObjects(type, formatOf(this))

inline fun <reified T : Any> URL.parse(): T =
    this.parse(T::class)

inline fun <reified T : Any> URL.parseObjects(): List<T> =
    this.parseObjects(T::class)
