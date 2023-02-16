package com.hexagonkt.core

import kotlin.reflect.KProperty1

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
                is Collection<*> -> value.mapIndexed { ii, item -> ii to item }.toMap()
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
 * @param pairs .
 * @return .
 */
fun <K : Any> mapOfNotNull(vararg pairs: Pair<K, *>): Map<K, *> =
    mapOf(*pairs).filterValues { it != null }

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @receiver .
 * @param name .
 * @return .
 */
fun <K, V> Map<K, V>.require(name: K): V =
    this[name] ?: error("$name required key not found")
