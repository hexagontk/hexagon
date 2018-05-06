package com.hexagonkt.vertx.store.mongodb

import com.hexagonkt.error
import com.hexagonkt.vertx.store.Mapper
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
        MongoDbJson.convertToMap(instance) + ("_id" to key.get(instance)) - (key.name)

    @Suppress("UNCHECKED_CAST")
    override fun fromStore(map: Map<String, Any?>): T =
        MongoDbJson.convertToObject(type, map + (key.name to (map["_id"] as? K ?: error)))

//    override fun fromStore(property: String, value: Any?): Any? {
//        return mapper.convertValue(value, fieldTypes[property])
//    }
}
