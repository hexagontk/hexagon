package com.hexagonkt.store.hashmap

import com.hexagonkt.store.Mapper
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class HashMapMapper <T: Any, K: Any>(
    private val type: KClass<T>,
    private val key: KProperty1<T, K>
): Mapper<T> {
    override val fields: Map<String, KProperty1<*, *>>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun toStore(instance: T): Map<String, Any> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun fromStore(map: Map<String, Any>): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
