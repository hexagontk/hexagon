package com.hexagonkt.store

import com.hexagonkt.helpers.async
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

interface Store<T : Any, K : Any> {
    val type: KClass<T>
    val key: KProperty1<T, K>
    val name: String
    val mapper: Mapper<T>

    suspend fun insertOne(instance: T): K

    suspend fun insertMany(instances: List<T>): ReceiveChannel<K>

    suspend fun insertMany(vararg instances: T): ReceiveChannel<K> = insertMany(instances.toList())

    suspend fun saveOne(instance: T): K

    suspend fun saveMany(instances: List<T>): Long // Returns modified ones inserted + modified

    suspend fun replaceOne(instance: T): Boolean

    suspend fun replaceMany(instances: List<T>): ReceiveChannel<T>

    suspend fun replaceMany(vararg instances: T): ReceiveChannel<T> =
        replaceMany(instances.toList())

    suspend fun updateOne(key: K, updates: Map<String, *>): Boolean

    suspend fun updateOne(key: K, vararg updates: Pair<String, *>): Boolean =
        updateOne(key, updates.toMap())

    suspend fun updateOne_(key: K, updates: Map<KProperty1<T, *>, *>): Boolean =
        updateOne(key, updates.mapKeys { it.key.name })

    suspend fun updateOne_(key: K, vararg updates: Pair<KProperty1<T, *>, *>): Boolean =
        updateOne(key, updates.map { it.first.name to it.second }.toMap())

    suspend fun updateMany(filter: Map<String, List<*>>, updates: Map<String, *>): Long

    suspend fun deleteOne(id: K): Boolean

    suspend fun deleteMany(filter: Map<String, List<*>>): Long

    suspend fun findOne(key: K): T?

    suspend fun findOne(key: K, fields: List<String>): Map<String, *>

    suspend fun findMany(
        filter: Map<String, List<*>>,
        limit: Int? = null,
        skip: Int? = null,
        sort: Map<String, Boolean> = emptyMap()): ReceiveChannel<T>

    suspend fun findMany(
        filter: Map<String, List<*>>,
        fields: List<String>,
        limit: Int? = null,
        skip: Int? = null,
        sort: Map<String, Boolean> = emptyMap()): ReceiveChannel<Map<String, *>>

    suspend fun findAll(
        limit: Int? = null,
        skip: Int? = null,
        sort: Map<String, Boolean> = emptyMap()): ReceiveChannel<T> =
            findMany(emptyMap(), limit, skip, sort)

    suspend fun findAll(
        fields: List<String>,
        limit: Int? = null,
        skip: Int? = null,
        sort: Map<String, Boolean> = emptyMap()): ReceiveChannel<Map<String, *>> =
            findMany(emptyMap(), fields, limit, skip, sort)

    suspend fun count(filter: Map<String, List<*>> = emptyMap()): Long

    suspend fun drop()

    fun asyncInsertOne(instance: T): Deferred<K> = async { insertOne(instance) }

    fun asyncReplaceOne(instance: T): Deferred<Boolean> = async { replaceOne(instance) }

    fun asyncUpdateOne(key: K, updates: Map<String, *>): Deferred<Boolean> =
        async { updateOne(key, updates) }

//    fun asyncUpdateOne(key: K, vararg updates: Pair<String, *>): Boolean =
//        async { updateOne(key, updates) }
//
//    fun asyncUpdateOne_(key: K, updates: Map<KProperty1<T, *>, *>): Boolean =
//        async { updateOne(key, updates) }
//
//    fun asyncUpdateOne_(key: K, vararg updates: Pair<KProperty1<T, *>, *>): Deferred<Boolean> =
//        async { updateOne(key, *updates) }

    fun asyncUpdateMany(filter: Map<String, List<*>>, updates: Map<String, *>): Deferred<Long> =
        async { updateMany(filter, updates) }

    fun asyncDeleteOne(id: K): Deferred<Boolean> = async { deleteOne(id) }

    fun asyncDeleteMany(filter: Map<String, List<*>>): Deferred<Long> = async { deleteMany(filter) }

    fun asyncFindOne(key: K): Deferred<T?> = async { findOne(key) }

    fun asyncFindOne(key: K, fields: List<String>): Deferred<T?> = async { findOne(key) }

    fun asyncCount(filter: Map<String, List<*>> = emptyMap()): Deferred<Long> =
        async { count(filter) }
}
