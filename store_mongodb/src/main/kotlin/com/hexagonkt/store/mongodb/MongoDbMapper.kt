package com.hexagonkt.store.mongodb

import com.hexagonkt.helpers.error
import com.hexagonkt.serialization.convertToMap
import com.hexagonkt.serialization.convertToObject
import com.hexagonkt.store.Mapper
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class MongoDbMapper<T : Any, K : Any>(
    private val type: KClass<T>,
    private val key: KProperty1<T, K>
): Mapper<T> {

//    private val fieldTypes: Map<String, Class<*>> = type.declaredMemberProperties
//        .map { it.name to it.returnType.jvmErasure.java }
//        .toMap()

    override fun toStore(instance: T): Map<String, Any?> =
        (instance.convertToMap() + ("_id" to key.get(instance)) - (key.name))
            .mapKeys { it.key.toString() }

    @Suppress("UNCHECKED_CAST")
    override fun fromStore(map: Map<String, Any?>): T =
        (map + (key.name to (map["_id"] ?: error))).convertToObject(type)

//    override fun fromStore(property: String, value: Any?): Any? {
//        return mapper.convertValue(value, fieldTypes[property])
//    }
}
