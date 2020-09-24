package com.hexagonkt.store.mongodb

import com.hexagonkt.helpers.*
import com.hexagonkt.serialization.convertToMap
import com.hexagonkt.serialization.convertToObject
import com.hexagonkt.store.Mapper
import org.bson.BsonBinary
import org.bson.BsonString
import java.net.URL
import java.time.*
import java.time.ZoneOffset.UTC
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.javaType

class MongoDbMapper<T : Any, K : Any>(
    private val type: KClass<T>,
    private val key: KProperty1<T, K>
) : Mapper<T> {

    override val fields: Map<String, KProperty1<T, *>> by lazy {
        logger.time("REFLECT") { type.declaredMemberProperties }
            .map { it.name to it }
            .toMap()
    }

    override fun toStore(instance: T): Map<String, Any> =
        (instance.convertToMap() + ("_id" to key.get(instance)) - key.name)
            .filterEmpty()
            .mapKeys { it.key.toString() }
            .mapValues { toStore(it.key, it.value) }

    @Suppress("UNCHECKED_CAST")
    override fun fromStore(map: Map<String, Any>): T =
        (map + (key.name to map["_id"]))
            .filterEmpty()
            .mapValues { fromStore(it.key, it.value) }
            .convertToObject(type)

    override fun fromStore(property: String, value: Any): Any {
        val fieldType = fields[property]?.returnType?.javaType
        return when {
            value is BsonBinary && fieldType == UUID::class.java ->
                if (value is UUID) value
                else UUID.nameUUIDFromBytes(value.data)
            value is BsonString -> value.value
            value is Date -> when (fields[property]?.returnType?.javaType) {
                LocalDate::class.java -> value.toLocalDate()
                LocalDateTime::class.java -> value.toLocalDateTime()
                else -> fail
            }
            else -> value
        }
    }

    override fun toStore(property: String, value: Any): Any {
        val fieldType = fields[property]?.returnType?.javaType
        return when {
            value is String && fieldType == UUID::class.java -> UUID.fromString(value)
            value is URL -> value.toString()
            value is String && fieldType == LocalDate::class.java -> LocalDate.parse(value)
            value is String && fieldType == LocalDateTime::class.java ->
                toStore(property, LocalDateTime.parse(value))
            value is LocalDateTime && fieldType == LocalDateTime::class.java -> value
                .atZone(ZoneId.systemDefault())
                .withZoneSameInstant(UTC)
                .toLocalDateTime()
            else -> value
        }
    }
}
