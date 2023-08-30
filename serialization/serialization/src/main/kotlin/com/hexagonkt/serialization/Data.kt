package com.hexagonkt.serialization

import kotlin.collections.Map.Entry
import kotlin.reflect.KProperty1

interface Data<T> : Map<String, Any?> {
    val data: Map<String, *>
    fun copy(data: Map<String, *>): T

    override val entries: Set<Entry<String, *>>
        get() = data.entries

    override val keys: Set<String>
        get() = data.keys

    override val size: Int
        get() = data.size

    override val values: Collection<*>
        get() = data.values

    override fun isEmpty(): Boolean =
        data.isEmpty()

    override fun get(key: String): Any? =
        data[key]

    override fun containsValue(value: Any?): Boolean =
        data.containsValue(value)

    override fun containsKey(key: String): Boolean =
        data.containsKey(key)

    operator fun get(key: KProperty1<T, *>): Any? =
        data[key.name]
}
