package com.hexagonkt.serialization

import kotlin.collections.Map.Entry

interface MutableData : Map<String, Any?> {
    fun data(): Map<String, *>
    fun with(data: Map<String, *>)

    override val entries: Set<Entry<String, *>>
        get() = data().entries

    override val keys: Set<String>
        get() = data().keys

    override val size: Int
        get() = data().size

    override val values: Collection<*>
        get() = data().values

    override fun isEmpty(): Boolean =
        data().isEmpty()

    override fun get(key: String): Any? =
        data()[key]

    override fun containsValue(value: Any?): Boolean =
        data().containsValue(value)

    override fun containsKey(key: String): Boolean =
        data().containsKey(key)
}
