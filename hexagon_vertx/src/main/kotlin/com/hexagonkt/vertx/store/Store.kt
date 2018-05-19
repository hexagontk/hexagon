package com.hexagonkt.vertx.store

import io.vertx.core.Future
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

/**
 * Common operations for stores, provides info about store type, name and key.
 */
interface Store<T : Any, K : Any> {
    val type: KClass<T>
    val key: KProperty1<T, K>
    val name: String
    val mapper: Mapper<T>
    val fields: List<String>

    fun insertOne(instance: T): Future<K>

    fun insertMany(instances: List<T>): Future<Long>

    fun insertMany(vararg instances: T): Future<Long> = insertMany(instances.toList())

    fun saveOne(instance: T): Future<K>

    fun saveMany(instances: List<T>): Future<Long> // Returns modified ones inserted + modified

    fun replaceOne(instance: T): Future<Boolean>

    fun replaceMany(instances: List<T>): Future<Long>

    fun replaceMany(vararg instances: T): Future<Long> =
        replaceMany(instances.toList())

    fun updateOne(key: K, updates: Map<String, *>): Future<Boolean>

    fun updateOne(key: K, vararg updates: Pair<String, *>): Future<Boolean> =
        updateOne(key, updates.toMap())

    fun updateOne_(key: K, updates: Map<KProperty1<T, *>, *>): Future<Boolean> =
        updateOne(key, updates.mapKeys { it.key.name })

    fun updateOne_(key: K, vararg updates: Pair<KProperty1<T, *>, *>): Future<Boolean> =
        updateOne(key, updates.map { it.first.name to it.second }.toMap())

    fun updateMany(filter: Map<String, List<*>>, updates: Map<String, *>): Future<Long>

    fun deleteOne(id: K): Future<Boolean>

    fun deleteMany(filter: Map<String, List<*>>): Future<Long>

    fun findOne(key: K): Future<T?>

    fun findOne(key: K, fields: List<String>): Future<Map<String, *>?>

    fun findMany(
        filter: Map<String, List<*>>,
        limit: Int? = null,
        skip: Int? = null,
        sort: Map<String, Boolean> = emptyMap()): Future<List<T>>

    fun findMany(
        filter: Map<String, List<*>>,
        fields: List<String>,
        limit: Int? = null,
        skip: Int? = null,
        sort: Map<String, Boolean> = emptyMap()): Future<List<Map<String, *>>>

    fun findAll(
        limit: Int? = null,
        skip: Int? = null,
        sort: Map<String, Boolean> = emptyMap()): Future<List<T>> =
            findMany(emptyMap(), limit, skip, sort)

    fun findAll(
        fields: List<String>,
        limit: Int? = null,
        skip: Int? = null,
        sort: Map<String, Boolean> = emptyMap()): Future<List<Map<String, *>>> =
            findMany(emptyMap(), fields, limit, skip, sort)

    fun count(filter: Map<String, List<*>> = emptyMap()): Future<Long>

    fun drop(): Future<Void>
}
