package com.hexagonkt.store

import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

interface Store<T : Any, K : Any> {
    val type: KClass<T>
    val key: KProperty1<T, K>
    val name: String

    suspend fun insertOne(instance: T): K

    fun insertMany(instances: List<T>): ReceiveChannel<K>

    fun insertMany(vararg instances: T): ReceiveChannel<K> = insertMany(instances.toList())

    suspend fun replaceOne(instance: T): Boolean

    fun replaceMany(instances: List<T>): ReceiveChannel<T>

    fun replaceMany(vararg instances: T): ReceiveChannel<T> = replaceMany(instances.toList())

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

    suspend fun findOne(key: K): T

    suspend fun findOne(key: K, fields: List<String>): Map<String, *>

    fun findMany(
        filter: Map<String, List<*>>,
        limit: Int? = null,
        skip: Int? = null,
        sort: Map<String, Boolean> = emptyMap()): ReceiveChannel<T>

    fun findMany(
        filter: Map<String, List<*>>,
        fields: List<String>,
        limit: Int? = null,
        skip: Int? = null,
        sort: Map<String, Boolean> = emptyMap()): ReceiveChannel<Map<String, *>>

    suspend fun count(filter: Map<String, List<*>> = emptyMap()): Long
}
