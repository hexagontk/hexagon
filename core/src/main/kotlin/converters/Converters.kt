package com.hexagonkt.core.converters

import kotlin.reflect.KClass

/**
 * Utility method to convert one type to another.
 *
 * @param target Target type for the source instance.
 * @receiver Value to convert to another type.
 *
 * @see ConvertersManager.convert
 */
fun <T : Any> Any.convert(target: KClass<T>): T =
    ConvertersManager.convert(this, target)

/**
 * Utility method to convert one type to another.
 *
 * @param T Target type for the source instance.
 * @receiver Value to convert to another type.
 *
 * @see ConvertersManager.convert
 */
inline fun <reified T : Any> Any.convert(): T =
    convert(T::class)

// TODO Add conversion utilities to transform collections (lists, sets or maps)
