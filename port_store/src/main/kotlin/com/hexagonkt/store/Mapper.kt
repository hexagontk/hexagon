package com.hexagonkt.store

import kotlin.reflect.KProperty1

/**
 * Maps objects and fields from/to stores and filters.
 */
interface Mapper<T : Any> {

    val fields: Map<String, KProperty1<*, *>>

    fun toStore(instance: T): Map<String, Any>

    fun fromStore(map: Map<String, Any>): T

    fun toStore(property: String, value: Any): Any = value

    fun fromStore(property: String, value: Any): Any = value
}
