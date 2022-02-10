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
 * Convert a group of instances of one type to another type.
 *
 * @param target Target type for the source instances in the group.
 * @receiver Value to convert to another type.
 * @return List of converted instances, returns empty list if target is `null`.
 *
 * @see ConvertersManager.convertObjects
 */
fun <T : Any> Iterable<Any>?.convertObjects(target: KClass<T>): List<T> =
    ConvertersManager.convertObjects(this ?: emptyList(), target)

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

/**
 * Convert a group of instances of one type to another type.
 *
 * @param T Target type for the source instances in the group.
 * @receiver Value to convert to another type.
 * @return List of converted instances, returns empty list if target is `null`.
 *
 * @see ConvertersManager.convertObjects
 */
inline fun <reified T : Any> Iterable<Any>?.convertObjects(): List<T> =
    convertObjects(T::class)
