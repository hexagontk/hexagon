package com.hexagonkt.store.hashmap

import com.hexagonkt.helpers.filterEmpty
import com.hexagonkt.helpers.logger
import com.hexagonkt.helpers.error
import com.hexagonkt.helpers.toLocalDate
import com.hexagonkt.helpers.toLocalDateTime
import com.hexagonkt.serialization.convertToMap
import com.hexagonkt.serialization.convertToObject
import com.hexagonkt.store.Mapper
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.javaType

class HashMapMapper <T: Any, K: Any>(
    private val type: KClass<T>,
    private val key: KProperty1<T, K>
): Mapper<T> {
    override val fields: Map<String, KProperty1<*, *>> by lazy {
        logger.time ("REFLECT") { type.declaredMemberProperties }
            .map { it.name to it }
            .toMap()
    }

    override fun toStore(instance: T): Map<String, Any>  =
        instance.convertToMap()
            .filterEmpty()
            .mapKeys { it.key.toString() }
            .mapValues { it.value as Any }

    @Suppress("UNCHECKED_CAST")
    override fun fromStore(map: Map<String, Any>): T  = map.filterEmpty().convertToObject(type)

    override fun fromStore(property: String, value: Any): Any = when (value) {
        is Date -> when (fields[property]?.returnType?.javaType) {
            LocalDate::class.java -> value.toLocalDate()
            LocalDateTime::class.java -> value.toLocalDateTime()
            else -> error
        }
        else -> value
    }
}
