package com.hexagonkt.core

import kotlin.reflect.KProperty1

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @receiver .
 * @param count .
 * @return .
 */
fun <Z> Collection<Z>.ensureSize(count: IntRange): Collection<Z> = this.apply {
    if (size !in count) error("$size items while expecting only $count element")
}

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @receiver .
 * @param keys .
 * @return .
 */
@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> Map<*, *>.keys(vararg keys: Any): T? {

    val mappedKeys = keys.map {
        when (it) {
            is KProperty1<*, *> -> it.name
            else -> it
        }
    }

    return mappedKeys
        .dropLast(1)
        .fold(this) { result, element ->
            val r = result as Map<Any, Any>
            when (val value = r[element]) {
                is Map<*, *> -> value
                is List<*> -> value.mapIndexed { ii, item -> ii to item }.toMap()
                else -> emptyMap<Any, Any>()
            }
        }[mappedKeys.last()] as? T
}

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @receiver .
 * @param keys .
 * @return .
 */
inline operator fun <reified T : Any> Map<*, *>.invoke(vararg keys: Any): T? =
    keys(*keys)

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @receiver .
 * @param name .
 * @return .
 */
inline fun <reified T : Any> Map<*, *>.requireKeys(vararg name: Any): T =
    this.keys(*name) ?: error("$name required key not found")

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @receiver .
 * @return .
 */
fun <K, V> Map<K, List<V>>.pairs(): List<Pair<K, V>> =
    flatMap { (k, v) -> v.map { k to it } }

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @param fields .
 * @return .
 */
fun <T : Any> fieldsMapOf(vararg fields: Pair<KProperty1<T, *>, *>): Map<String, *> =
    fields.associate { it.first.name to it.second }

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @param fields .
 * @return .
 */
fun <T : Any> fieldsMapOfNotNull(vararg fields: Pair<KProperty1<T, *>, *>): Map<String, *> =
    fieldsMapOf(*fields).filterValues { it != null }

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @receiver .
 * @param name .
 * @return .
 */
fun <K, V> Map<K, V>.require(name: K): V =
    this[name] ?: error("$name required key not found")

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @receiver .
 * @return .
 */
fun <K, V> Map<K, V?>.filterEmpty(): Map<K, V> =
    this.filterValues(::notEmpty).mapValues { (_, v) -> v ?: fail }

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @receiver .
 * @return .
 */
fun <V> List<V?>.filterEmpty(): List<V> =
    this.filter(::notEmpty).map { it ?: fail }

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @receiver .
 * @return .
 */
fun Map<*, *>.filterEmptyRecursive(): Map<*, *> =
    mapValues { (_, v) ->
        when (v) {
            is List<*> -> v.filterEmptyRecursive()
            is Map<*, *> -> v.filterEmptyRecursive()
            else -> v
        }
    }
        .filterEmpty()

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @receiver .
 * @return .
 */
fun List<*>.filterEmptyRecursive(): List<*> =
    map {
        when (it) {
            is List<*> -> it.filterEmptyRecursive()
            is Map<*, *> -> it.filterEmptyRecursive()
            else -> it
        }
    }
        .filterEmpty()

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @param value .
 * @return .
 */
fun <V> notEmpty(value: V?): Boolean {
    return when (value) {
        null -> false
        is List<*> -> value.isNotEmpty()
        is Map<*, *> -> value.isNotEmpty()
        else -> true
    }
}

inline fun <reified T : Any> Map<*, *>.getKey(key: KProperty1<*, *>): T? =
    this[key.name] as? T

fun Map<*, *>.getInt(key: KProperty1<*, *>): Int? =
    getKey(key)

fun Map<*, *>.getLong(key: KProperty1<*, *>): Long? =
    getKey(key)

fun Map<*, *>.getFloat(key: KProperty1<*, *>): Float? =
    getKey(key)

fun Map<*, *>.getDouble(key: KProperty1<*, *>): Double? =
    getKey(key)

fun Map<*, *>.getBoolean(key: KProperty1<*, *>): Boolean? =
    getKey(key)

fun Map<*, *>.getString(key: KProperty1<*, *>): String? =
    getKey(key)

fun Map<*, *>.getList(key: KProperty1<*, *>): List<*>? =
    getKey(key)

fun Map<*, *>.getMap(key: KProperty1<*, *>): Map<String, *>? =
    getKey(key)

fun Map<*, *>.getInts(key: KProperty1<*, *>): List<Int>? =
    getKey(key)

fun Map<*, *>.getLongs(key: KProperty1<*, *>): List<Long>? =
    getKey(key)

fun Map<*, *>.getFloats(key: KProperty1<*, *>): List<Float>? =
    getKey(key)

fun Map<*, *>.getDoubles(key: KProperty1<*, *>): List<Double>? =
    getKey(key)

fun Map<*, *>.getBooleans(key: KProperty1<*, *>): List<Boolean>? =
    getKey(key)

fun Map<*, *>.getStrings(key: KProperty1<*, *>): List<String>? =
    getKey(key)

fun Map<*, *>.getLists(key: KProperty1<*, *>): List<List<*>>? =
    getKey(key)

fun Map<*, *>.getMaps(key: KProperty1<*, *>): List<Map<String, *>>? =
    getKey(key)

fun Map<*, *>.getListOrEmpty(key: KProperty1<*, *>): List<*> =
    getList(key) ?: emptyList<Any>()

fun Map<*, *>.getMapOrEmpty(key: KProperty1<*, *>): Map<String, *> =
    getMap(key) ?: emptyMap<String, Any>()

fun Map<*, *>.getIntsOrEmpty(key: KProperty1<*, *>): List<Int> =
    getInts(key) ?: emptyList()

fun Map<*, *>.getLongsOrEmpty(key: KProperty1<*, *>): List<Long> =
    getLongs(key) ?: emptyList()

fun Map<*, *>.getFloatsOrEmpty(key: KProperty1<*, *>): List<Float> =
    getFloats(key) ?: emptyList()

fun Map<*, *>.getDoublesOrEmpty(key: KProperty1<*, *>): List<Double> =
    getDoubles(key) ?: emptyList()

fun Map<*, *>.getBooleansOrEmpty(key: KProperty1<*, *>): List<Boolean> =
    getBooleans(key) ?: emptyList()

fun Map<*, *>.getStringsOrEmpty(key: KProperty1<*, *>): List<String> =
    getStrings(key) ?: emptyList()

fun Map<*, *>.getListsOrEmpty(key: KProperty1<*, *>): List<List<*>> =
    getLists(key) ?: emptyList()

fun Map<*, *>.getMapsOrEmpty(key: KProperty1<*, *>): List<Map<String, *>> =
    getMaps(key) ?: emptyList()

inline fun <reified T : Any> Map<*, *>.requireKey(key: KProperty1<*, *>): T =
    getKey(key)
        ?: error("'${key.name}' key not found, or wrong type (must be ${T::class.qualifiedName})")

fun Map<*, *>.requireInt(key: KProperty1<*, *>): Int =
    requireKey(key)

fun Map<*, *>.requireLong(key: KProperty1<*, *>): Long =
    requireKey(key)

fun Map<*, *>.requireFloat(key: KProperty1<*, *>): Float =
    requireKey(key)

fun Map<*, *>.requireDouble(key: KProperty1<*, *>): Double =
    requireKey(key)

fun Map<*, *>.requireBoolean(key: KProperty1<*, *>): Boolean =
    requireKey(key)

fun Map<*, *>.requireString(key: KProperty1<*, *>): String =
    requireKey(key)

fun Map<*, *>.requireList(key: KProperty1<*, *>): List<*> =
    requireKey(key)

fun Map<*, *>.requireMap(key: KProperty1<*, *>): Map<String, *> =
    requireKey(key)

fun Map<*, *>.requireInts(key: KProperty1<*, *>): List<Int> =
    requireKey(key)

fun Map<*, *>.requireLongs(key: KProperty1<*, *>): List<Long> =
    requireKey(key)

fun Map<*, *>.requireFloats(key: KProperty1<*, *>): List<Float> =
    requireKey(key)

fun Map<*, *>.requireDoubles(key: KProperty1<*, *>): List<Double> =
    requireKey(key)

fun Map<*, *>.requireBooleans(key: KProperty1<*, *>): List<Boolean> =
    requireKey(key)

fun Map<*, *>.requireStrings(key: KProperty1<*, *>): List<String> =
    requireKey(key)

fun Map<*, *>.requireLists(key: KProperty1<*, *>): List<List<*>> =
    requireKey(key)

fun Map<*, *>.requireMaps(key: KProperty1<*, *>): List<Map<String, *>> =
    requireKey(key)
