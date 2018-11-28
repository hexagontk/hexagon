package com.hexagonkt.store

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

interface Store<T : Any, K : Any> {
    val type: KClass<T>
    val key: KProperty1<T, K>
    val name: String
    val mapper: Mapper<T>

    fun createIndex(unique: Boolean, fields: List<Pair<String, IndexOrder>>): String

    fun createIndex(unique: Boolean, vararg fields: Pair<String, IndexOrder>): String =
        createIndex(unique, fields.toList())

    fun insertOne(instance: T): K

    fun insertMany(instances: List<T>): List<K>

    fun insertMany(vararg instances: T): List<K> =
        insertMany(instances.toList())

    fun saveOne(instance: T): K? // returns key if created, null if updated

    fun saveMany(instances: List<T>): List<K?>

    fun replaceOne(instance: T): Boolean

    fun replaceMany(instances: List<T>): List<T>

    fun replaceMany(vararg instances: T): List<T> =
        replaceMany(instances.toList())

    fun updateOne(key: K, updates: Map<String, *>): Boolean

    fun updateOne(key: K, vararg updates: Pair<String, *>): Boolean =
        updateOne(key, updates.toMap())

    fun updateOne_(key: K, vararg updates: Pair<KProperty1<T, *>, *>): Boolean =
        updateOne(key, updates.map { it.first.name to it.second }.toMap())

    fun updateMany(filter: Map<String, List<*>>, updates: Map<String, *>): Long

    fun deleteOne(id: K): Boolean

    fun deleteMany(filter: Map<String, List<*>>): Long

    fun findOne(key: K): T?

    fun findOne(key: K, fields: List<String>): Map<String, *>

    fun findMany(
        filter: Map<String, List<*>>,
        limit: Int? = null,
        skip: Int? = null,
        sort: Map<String, Boolean> =
            emptyMap()): List<T>

    fun findMany(
        filter: Map<String, List<*>>,
        fields: List<String>,
        limit: Int? = null,
        skip: Int? = null,
        sort: Map<String, Boolean> = emptyMap()): List<Map<String, *>>

    fun findAll(
        limit: Int? = null,
        skip: Int? = null,
        sort: Map<String, Boolean> = emptyMap()): List<T> =
            findMany(emptyMap(), limit, skip, sort)

    fun findAll(
        fields: List<String>,
        limit: Int? = null,
        skip: Int? = null,
        sort: Map<String, Boolean> = emptyMap()): List<Map<String, *>> =
            findMany(emptyMap(), fields, limit, skip, sort)

    fun count(filter: Map<String, List<*>> = emptyMap()): Long

    fun drop()
}
