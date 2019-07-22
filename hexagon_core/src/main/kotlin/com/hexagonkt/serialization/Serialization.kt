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

fun <T : Any> InputStream.parseList(type: KClass<T>, format: SerializationFormat = defaultFormat) =
    format.parseList(this, type)

inline fun <reified T : Any> InputStream.parse(format: SerializationFormat = defaultFormat): T =
    this.parse(T::class, format)

inline fun <reified T : Any> InputStream.parseList(format: SerializationFormat = defaultFormat) =
    this.parseList(T::class, format)

// STRING //////////////////////////////////////////////////////////////////////////////////////////
fun <T : Any> String.parse(type: KClass<T>, format: SerializationFormat = defaultFormat): T =
    this.toStream().parse(type, format)

fun <T : Any> String.parseList(type: KClass<T>, format: SerializationFormat = defaultFormat) =
    this.toStream().parseList(type, format)

inline fun <reified T : Any> String.parse(format: SerializationFormat = defaultFormat): T =
    this.parse(T::class, format)

inline fun <reified T : Any> String.parseList(format: SerializationFormat = defaultFormat) =
    this.parseList(T::class, format)

// FILE ////////////////////////////////////////////////////////////////////////////////////////////
fun <T : Any> File.parse(type: KClass<T>): T =
    this.inputStream().parse(type, formatOf(this))

fun <T : Any> File.parseList(type: KClass<T>): List<T> =
    this.inputStream().parseList(type, formatOf(this))

inline fun <reified T : Any> File.parse(): T =
    this.parse(T::class)

inline fun <reified T : Any> File.parseList(): List<T> =
    this.parseList(T::class)

// URL /////////////////////////////////////////////////////////////////////////////////////////////
fun <T : Any> URL.parse(type: KClass<T>): T =
    this.openStream().parse(type, formatOf(this))

fun <T : Any> URL.parseList(type: KClass<T>): List<T> =
    this.openStream().parseList(type, formatOf(this))

inline fun <reified T : Any> URL.parse(): T =
    this.parse(T::class)

inline fun <reified T : Any> URL.parseList(): List<T> =
    this.parseList(T::class)

// RESOURCE ////////////////////////////////////////////////////////////////////////////////////////
fun <T : Any> Resource.parse(type: KClass<T>): T =
    this.requireUrl().parse(type)

fun <T : Any> Resource.parseList(type: KClass<T>): List<T> =
    this.requireUrl().parseList(type)

inline fun <reified T : Any> Resource.parse(): T =
    this.parse(T::class)

inline fun <reified T : Any> Resource.parseList(): List<T> =
    this.parseList(T::class)

