package com.hexagonkt.core.helpers

import kotlin.collections.Map.Entry

/**
 * Simplify access to multivalued maps. It looks for first elements, but all values can be accessed
 * through the `allValues` field.
 */
class MultiMap<K, V>(mapData: Map<K, List<V>>) : Map<K, V> {

    constructor(mapData: List<Pair<K, V>>) : this(mapData.groupBy({ it.first }) { it.second } )

    val allValues: Map<K, List<V>> = mapData.filterValues { it.isNotEmpty() }
    val allPairs: List<Pair<K, V>> by lazy { allValues.flatMap { (k, v) -> v.map { k to it } } }

    override val entries: Set<Entry<K, V>>
        get() = allValues.mapValues { it.value.first() }.entries

    override val keys: Set<K>
        get() = allValues.keys

    override val size: Int
        get() = allValues.size

    override val values: Collection<V>
        get() = allValues.values.map { it.first() }

    override fun containsKey(key: K): Boolean =
        allValues.containsKey(key)

    override fun containsValue(value: V): Boolean =
        allValues.any { it.value.contains(value) }

    override fun get(key: K): V? =
        allValues[key]?.first()

    override fun isEmpty(): Boolean =
        allValues.isEmpty()

    override fun equals(other: Any?): Boolean =
        if (other is MultiMap<*, *>)
            allValues == other.allValues
        else
            allValues == other

    operator fun plus(element: Pair<K, V>): MultiMap<K, V> =
        MultiMap(allPairs + element)

    operator fun plus(element: Map<K, V>): MultiMap<K, V> =
        MultiMap(allPairs + element.map { (k, v) -> k to v })

    operator fun plus(element: MultiMap<K, V>): MultiMap<K, V> =
        MultiMap(allValues + element.allValues)

    operator fun minus(element: K): MultiMap<K, V> =
        MultiMap(allValues - element)

    override fun hashCode(): Int =
        allValues.hashCode()

    override fun toString(): String =
        allValues.toString()
}
