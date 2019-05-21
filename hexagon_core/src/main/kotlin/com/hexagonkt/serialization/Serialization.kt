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

// INPUT STREAM ////////////////////////////////////////////////////////////////////////////////////
fun <T : Any> InputStream.parse(type: KClass<T>, format: SerializationFormat = defaultFormat): T =
    format.parse(this, type)

fun InputStream.parse(format: SerializationFormat = defaultFormat): Map<*, *> =
    this.parse(Map::class, format)

fun <T : Any> InputStream.parseList(type: KClass<T>, format: SerializationFormat = defaultFormat) =
    format.parseList(this, type)

fun InputStream.parseList(format: SerializationFormat = defaultFormat): List<Map<*, *>> =
    this.parseList(Map::class, format)

// STRING //////////////////////////////////////////////////////////////////////////////////////////
fun <T : Any> String.parse(type: KClass<T>, format: SerializationFormat = defaultFormat): T =
    this.toStream().parse(type, format)

fun String.parse(format: SerializationFormat = defaultFormat): Map<*, *> =
    this.toStream().parse(format)

fun String.parseList(format: SerializationFormat = defaultFormat): List<Map<*, *>> =
    this.parseList(Map::class, format)

fun <T : Any> String.parseList(type: KClass<T>, format: SerializationFormat = defaultFormat) =
    this.toStream().parseList(type, format)

// FILE ////////////////////////////////////////////////////////////////////////////////////////////
fun <T : Any> File.parse(type: KClass<T>): T =
    this.inputStream().parse(type, formatOf(this))

fun File.parse(): Map<*, *> =
    this.parse(Map::class)

fun File.parseList(): List<Map<*, *>> =
    this.parseList(Map::class)

fun <T : Any> File.parseList(type: KClass<T>): List<T> =
    this.inputStream().parseList(type, formatOf(this))

// URL /////////////////////////////////////////////////////////////////////////////////////////////
fun <T : Any> URL.parse(type: KClass<T>): T =
    this.openStream().parse(type, formatOf(this))

fun URL.parse(): Map<*, *> =
    this.parse(Map::class)

fun URL.parseList(): List<Map<*, *>> =
    this.parseList (Map::class)

fun <T : Any> URL.parseList(type: KClass<T>): List<T> =
    this.openStream().parseList(type, formatOf(this))

// RESOURCE ////////////////////////////////////////////////////////////////////////////////////////
fun <T : Any> Resource.parse(type: KClass<T>): T =
    this.requireUrl().parse(type)

fun Resource.parse(): Map<*, *> =
    this.parse (Map::class)

fun Resource.parseList(): List<Map<*, *>> =
    this.parseList(Map::class)

fun <T : Any> Resource.parseList(type: KClass<T>): List<T> =
    this.requireUrl().parseList(type)
