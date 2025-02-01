package com.hexagontk.core

import kotlin.reflect.KProperty1

/**
 * .
 *
 * @receiver .
 * @param keys .
 * @return .
 */
inline fun <reified T : Any> Map<*, *>.getPath(vararg keys: Any): T? {
    val mappedKeys = keys.map {
        when (it) {
            is KProperty1<*, *> -> it.name
            else -> it
        }
    }

    return mappedKeys
        .dropLast(1)
        .fold(this) { result, element ->
            when (val value = result[element]) {
                is Map<*, *> -> value
                is Collection<*> -> value.mapIndexed { ii, item -> ii to item }.toMap()
                else -> emptyMap<Any, Any>()
            }
        }[mappedKeys.last()] as? T
}

/**
 * .
 *
 * @receiver .
 * @param name .
 * @return .
 */
inline fun <reified T : Any> Map<*, *>.requirePath(vararg name: Any): T =
    this.getPath(*name) ?: error("$name required key not found")

/**
 * .
 *
 * @param fields .
 * @return .
 */
fun <T : Any> fieldsMapOf(vararg fields: Pair<KProperty1<T, *>, *>): Map<String, *> =
    fields.associate { it.first.name to it.second }

/**
 * .
 *
 * @param fields .
 * @return .
 */
fun <T : Any> fieldsMapOfNotNull(vararg fields: Pair<KProperty1<T, *>, *>): Map<String, *> =
    fieldsMapOf(*fields).filterValues { it != null }

/**
 * .
 *
 * @param pairs .
 * @return .
 */
fun <K : Any> mapOfNotNull(vararg pairs: Pair<K, *>): Map<K, *> =
    mapOf(*pairs).filterValues { it != null }

/**
 * .
 *
 * @receiver .
 * @param name .
 * @return .
 */
fun <K, V> Map<K, V>.require(name: K): V =
    this[name] ?: error("$name required key not found")

/**
 * .
 *
 * @param T .
 * @param key .
 * @return .
 */
inline operator fun <reified T : Any> Map<*, *>.get(key: KProperty1<*, *>): T? =
    this[key.name] as? T

/**
 * .
 *
 * @param T .
 * @param key .
 * @param default .
 * @return .
 */
inline fun <reified T : Any> Map<*, *>.getOrDefault(key: KProperty1<*, *>, default: T): T =
    this[key] ?: default

/**
 * .
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
 * .
 *
 * @param maps .
 * @return .
 */
fun merge(maps: Collection<Map<*, *>>): Map<*, *> =
    maps.reduce { a, b -> merge(a, b) }

/**
 * .
 *
 * @receiver .
 * @return .
 */
fun <K, V> Map<K, Collection<V>>.pairs(): Collection<Pair<K, V>> =
    flatMap { (k, v) -> v.map { k to it } }

/**
 * .
 *
 * @receiver .
 * @return .
 */
fun <K, V> Map<K, V?>.filterNotEmpty(): Map<K, V> =
    this.filterValues(::notEmpty).mapValues { (_, v) -> v ?: fail }

/**
 * .
 *
 * @receiver .
 * @return .
 */
fun <V> Collection<V?>.filterNotEmpty(): Collection<V> =
    this.filter(::notEmpty).map { it ?: fail }

/**
 * .
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
 * .
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
 * .
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

fun Map<*, *>.getInt(key: KProperty1<*, *>): Int? =
    get(key)

fun Map<*, *>.getLong(key: KProperty1<*, *>): Long? =
    get(key)

fun Map<*, *>.getFloat(key: KProperty1<*, *>): Float? =
    get(key)

fun Map<*, *>.getDouble(key: KProperty1<*, *>): Double? =
    get(key)

fun Map<*, *>.getBoolean(key: KProperty1<*, *>): Boolean? =
    get(key)

fun Map<*, *>.getString(key: KProperty1<*, *>): String? =
    get(key)

fun Map<*, *>.getList(key: KProperty1<*, *>): Collection<*>? =
    get(key)

fun Map<*, *>.getMap(key: KProperty1<*, *>): Map<String, *>? =
    get(key)

fun Map<*, *>.getInts(key: KProperty1<*, *>): Collection<Int>? =
    get(key)

fun Map<*, *>.getLongs(key: KProperty1<*, *>): Collection<Long>? =
    get(key)

fun Map<*, *>.getFloats(key: KProperty1<*, *>): Collection<Float>? =
    get(key)

fun Map<*, *>.getDoubles(key: KProperty1<*, *>): Collection<Double>? =
    get(key)

fun Map<*, *>.getBooleans(key: KProperty1<*, *>): Collection<Boolean>? =
    get(key)

fun Map<*, *>.getStrings(key: KProperty1<*, *>): Collection<String>? =
    get(key)

fun Map<*, *>.getLists(key: KProperty1<*, *>): Collection<List<*>>? =
    get(key)

fun Map<*, *>.getMaps(key: KProperty1<*, *>): Collection<Map<String, *>>? =
    get(key)

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
    get(key)
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
