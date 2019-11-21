package com.hexagonkt.store

import com.hexagonkt.helpers.Resource
import com.hexagonkt.helpers.ensureSize
import com.hexagonkt.serialization.parseObjects
import com.hexagonkt.store.IndexOrder.ASCENDING
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

/**
 * TODO All methods accepting maps rely on `mapOf` returning an insertion ordered map. Take care of
 *   this in the future to avoid possible bugs
 */
interface Store<T : Any, K : Any> {
    val type: KClass<T>
    val key: KProperty1<T, K>
    val name: String
    val mapper: Mapper<T>

    fun createIndex(unique: Boolean, fields: Map<String, IndexOrder>): String

    fun createIndex(unique: Boolean, vararg fields: Pair<KProperty1<T, *>, IndexOrder>): String =
        createIndex(unique, fields.map { it.first.name to it.second }.toMap())

    fun createIndex(unique: Boolean, vararg fields: KProperty1<T, *>): String =
        createIndex(unique, *fields.map { it to ASCENDING }.toTypedArray())

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

    fun updateOne(key: K, vararg updates: Pair<KProperty1<T, *>, *>): Boolean =
        updateOne(key, fields(*updates))

    fun updateMany(filter: Map<String, *>, updates: Map<String, *>): Long

    fun deleteOne(id: K): Boolean

    fun deleteMany(filter: Map<String, *>): Long

    fun findOne(key: K): T?

    fun findOne(key: K, fields: List<String>): Map<String, *>?

    fun findOne(filter: Map<String, *>): T? =
        findMany(filter).ensureSize(0..1).firstOrNull()

    fun findOne(filter: Map<String, *>, fields: List<String>): Map<String, *>? =
        findMany(filter, fields).ensureSize(0..1).firstOrNull()

    fun findMany(
        filter: Map<String, *>,
        limit: Int? = null,
        skip: Int? = null,
        sort: Map<String, Boolean> = emptyMap()): List<T>

    fun findMany(
        filter: Map<String, *>,
        fields: List<String>,
        limit: Int? = null,
        skip: Int? = null,
        sort: Map<String, Boolean> = emptyMap()): List<Map<String, *>>

    fun findAll(
        limit: Int? = null,
        skip: Int? = null,
        sort: Map<String, Boolean> = emptyMap()): List<T> =
            findMany(emptyMap<String, Any>(), limit, skip, sort)

    fun findAll(
        fields: List<String>,
        limit: Int? = null,
        skip: Int? = null,
        sort: Map<String, Boolean> = emptyMap()): List<Map<String, *>> =
            findMany(emptyMap<String, Any>(), fields, limit, skip, sort)

    fun count(filter: Map<String, *> = emptyMap<String, Any>()): Long

    fun drop()

    fun fields(updates: Map<KProperty1<T, *>, *>): Map<String, *> =
        updates.mapKeys { it.key.name }

    fun fields(vararg updates: Pair<KProperty1<T, *>, *>): Map<String, *> =
        fields(updates.toMap())

    fun import(input: File) {
        insertMany(input.parseObjects(type))
    }

    fun import(input: Resource) {
        insertMany(input.parseObjects(type))
    }
}
