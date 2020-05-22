package com.hexagonkt.serialization

import com.hexagonkt.helpers.Resource
import com.hexagonkt.helpers.toStream
import com.hexagonkt.serialization.JacksonHelper.mapper
import com.hexagonkt.serialization.SerializationManager.defaultFormat
import com.hexagonkt.serialization.SerializationManager.formatOf
import java.io.File
import java.io.InputStream
import java.net.URL
import kotlin.reflect.KClass

// MAPPING /////////////////////////////////////////////////////////////////////////////////////////
fun Any.convertToMap(): Map<*, *> =
    mapper.convertValue(this, Map::class.java)

fun <T : Any> Map<*, *>.convertToObject(type: KClass<T>): T =
    mapper.convertValue(this, type.java)

fun <T : Any> List<Map<*, *>>.convertToObjects(type: KClass<T>): List<T> =
    this.map { it.convertToObject(type) }

fun Any.serialize(format: SerializationFormat = defaultFormat): String =
    format.serialize(this)

fun Any.serialize(contentType: String): String =
    this.serialize(formatOf(contentType))

inline fun <reified T : Any> Map<*, *>.convertToObject(): T =
    this.convertToObject(T::class)

inline fun <reified T : Any> List<Map<*, *>>.convertToObjects(): List<T> =
    this.convertToObjects(T::class)

// INPUT STREAM ////////////////////////////////////////////////////////////////////////////////////
fun <T : Any> InputStream.parse(type: KClass<T>, format: SerializationFormat = defaultFormat): T =
    format.parse(this, type)

fun <T : Any> InputStream.parseObjects(
    type: KClass<T>, format: SerializationFormat = defaultFormat) =
        format.parseObjects(this, type)

inline fun <reified T : Any> InputStream.parse(format: SerializationFormat = defaultFormat): T =
    this.parse(T::class, format)

inline fun <reified T : Any> InputStream.parseObjects(format: SerializationFormat = defaultFormat) =
    this.parseObjects(T::class, format)

// STRING //////////////////////////////////////////////////////////////////////////////////////////
fun <T : Any> String.parse(type: KClass<T>, format: SerializationFormat = defaultFormat): T =
    this.toStream().parse(type, format)

fun <T : Any> String.parseObjects(type: KClass<T>, format: SerializationFormat = defaultFormat) =
    this.toStream().parseObjects(type, format)

inline fun <reified T : Any> String.parse(format: SerializationFormat = defaultFormat): T =
    this.parse(T::class, format)

inline fun <reified T : Any> String.parseObjects(format: SerializationFormat = defaultFormat) =
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

// RESOURCE ////////////////////////////////////////////////////////////////////////////////////////
fun <T : Any> Resource.parse(type: KClass<T>): T =
    this.requireUrl().parse(type)

fun <T : Any> Resource.parseObjects(type: KClass<T>): List<T> =
    this.requireUrl().parseObjects(type)

inline fun <reified T : Any> Resource.parse(): T =
    this.parse(T::class)

inline fun <reified T : Any> Resource.parseObjects(): List<T> =
    this.parseObjects(T::class)
