package com.hexagonkt.helpers

import com.hexagonkt.core.fail
import kotlin.reflect.KProperty1

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * Mermaid test:
 * ```mermaid
 * graph LR
 *   A --> B
 * ```
 *
 * @param mapA .
 * @param mapB .
 * @return .
 */
fun merge(mapA: Map<*, *>, mapB: Map<*, *>): Map<*, *> =
    (mapA.entries + mapB.entries)
        .groupBy { it.key }
        .mapValues { (_, v) -> v.map { it.value } }
        .mapValues { (_, v) ->
            val isCollection = v.all { it is Collection<*> }
            val isMap = v.all { it is Map<*, *> }
            when {
                isCollection -> v.map { it as Collection<*> }.reduce { a, b -> a + b }
                isMap -> v.map { it as Map<*, *> }.reduce { a, b -> merge(a, b) }
                else -> v.last()
            }
        }

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @param maps .
 * @return .
 */
fun merge(maps: Collection<Map<*, *>>): Map<*, *> =
    maps.reduce { a, b -> merge(a, b) }

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
 * @return .
 */
fun <K, V> Map<K, Collection<V>>.pairs(): Collection<Pair<K, V>> =
    flatMap { (k, v) -> v.map { k to it } }

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @receiver .
 * @return .
 */
fun <K, V> Map<K, V?>.filterNotEmpty(): Map<K, V> =
    this.filterValues(::notEmpty).mapValues { (_, v) -> v ?: fail }

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @receiver .
 * @return .
 */
fun <V> Collection<V?>.filterNotEmpty(): Collection<V> =
    this.filter(::notEmpty).map { it ?: fail }

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @receiver .
 * @return .
 */
fun Map<*, *>.filterNotEmptyRecursive(): Map<*, *> =
    mapValues { (_, v) ->
        when (v) {
            is Collection<*> -> v.filterNotEmptyRecursive()
            is Map<*, *> -> v.filterNotEmptyRecursive()
            else -> v
        }
    }
    .filterNotEmpty()

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @receiver .
 * @return .
 */
fun Collection<*>.filterNotEmptyRecursive(): Collection<*> =
    map {
        when (it) {
            is Collection<*> -> it.filterNotEmptyRecursive()
            is Map<*, *> -> it.filterNotEmptyRecursive()
            else -> it
        }
    }
    .filterNotEmpty()

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @param value .
 * @return .
 */
fun <V> notEmpty(value: V?): Boolean {
    return when (value) {
        null -> false
        is Collection<*> -> value.isNotEmpty()
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

fun Map<*, *>.getList(key: KProperty1<*, *>): Collection<*>? =
    getKey(key)

fun Map<*, *>.getMap(key: KProperty1<*, *>): Map<String, *>? =
    getKey(key)

fun Map<*, *>.getInts(key: KProperty1<*, *>): Collection<Int>? =
    getKey(key)

fun Map<*, *>.getLongs(key: KProperty1<*, *>): Collection<Long>? =
    getKey(key)

fun Map<*, *>.getFloats(key: KProperty1<*, *>): Collection<Float>? =
    getKey(key)

fun Map<*, *>.getDoubles(key: KProperty1<*, *>): Collection<Double>? =
    getKey(key)

fun Map<*, *>.getBooleans(key: KProperty1<*, *>): Collection<Boolean>? =
    getKey(key)

fun Map<*, *>.getStrings(key: KProperty1<*, *>): Collection<String>? =
    getKey(key)

fun Map<*, *>.getLists(key: KProperty1<*, *>): Collection<List<*>>? =
    getKey(key)

fun Map<*, *>.getMaps(key: KProperty1<*, *>): Collection<Map<String, *>>? =
    getKey(key)

fun Map<*, *>.getListOrEmpty(key: KProperty1<*, *>): Collection<*> =
    getList(key) ?: emptyList<Any>()

fun Map<*, *>.getMapOrEmpty(key: KProperty1<*, *>): Map<String, *> =
    getMap(key) ?: emptyMap<String, Any>()

fun Map<*, *>.getIntsOrEmpty(key: KProperty1<*, *>): Collection<Int> =
    getInts(key) ?: emptyList()

fun Map<*, *>.getLongsOrEmpty(key: KProperty1<*, *>): Collection<Long> =
    getLongs(key) ?: emptyList()

fun Map<*, *>.getFloatsOrEmpty(key: KProperty1<*, *>): Collection<Float> =
    getFloats(key) ?: emptyList()

fun Map<*, *>.getDoublesOrEmpty(key: KProperty1<*, *>): Collection<Double> =
    getDoubles(key) ?: emptyList()

fun Map<*, *>.getBooleansOrEmpty(key: KProperty1<*, *>): Collection<Boolean> =
    getBooleans(key) ?: emptyList()

fun Map<*, *>.getStringsOrEmpty(key: KProperty1<*, *>): Collection<String> =
    getStrings(key) ?: emptyList()

fun Map<*, *>.getListsOrEmpty(key: KProperty1<*, *>): Collection<Collection<*>> =
    getLists(key) ?: emptyList()

fun Map<*, *>.getMapsOrEmpty(key: KProperty1<*, *>): Collection<Map<String, *>> =
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

fun Map<*, *>.requireList(key: KProperty1<*, *>): Collection<*> =
    requireKey(key)

fun Map<*, *>.requireMap(key: KProperty1<*, *>): Map<String, *> =
    requireKey(key)

fun Map<*, *>.requireInts(key: KProperty1<*, *>): List<Int> =
    requireKey(key)

fun Map<*, *>.requireLongs(key: KProperty1<*, *>): Collection<Long> =
    requireKey(key)

fun Map<*, *>.requireFloats(key: KProperty1<*, *>): Collection<Float> =
    requireKey(key)

fun Map<*, *>.requireDoubles(key: KProperty1<*, *>): Collection<Double> =
    requireKey(key)

fun Map<*, *>.requireBooleans(key: KProperty1<*, *>): Collection<Boolean> =
    requireKey(key)

fun Map<*, *>.requireStrings(key: KProperty1<*, *>): Collection<String> =
    requireKey(key)

fun Map<*, *>.requireLists(key: KProperty1<*, *>): Collection<Collection<*>> =
    requireKey(key)

fun Map<*, *>.requireMaps(key: KProperty1<*, *>): Collection<Map<String, *>> =
    requireKey(key)
