package com.hexagonkt.store

import kotlinx.coroutines.experimental.channels.Channel
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

interface Store<T : Any, K : Any> {
    val type: KClass<T>
    val key: KProperty1<T, K>
    val name: String

    suspend fun insertOne(instance: T): K

    suspend fun insertMany(instances: List<T>): Channel<K>

    suspend fun insertMany(vararg instances: T): Channel<K> = insertMany(instances.toList())

    suspend fun replaceOne(instance: T): Boolean

    suspend fun replaceMany(instances: List<T>): Channel<T>

    suspend fun replaceMany(vararg instances: T): Channel<T> = replaceMany(instances.toList())

    suspend fun updateOne(key: K, updates: Map<String, *>): Boolean

    suspend fun updateOne(key: K, vararg updates: Pair<String, *>): Boolean =
        updateOne(key, updates.toMap())

    suspend fun updateMany(filter: Map<String, List<*>>, updates: Map<String, *>): Long

    suspend fun deleteOne(id: K): Boolean

    suspend fun deleteMany(filter: Map<String, List<*>>): Long

    suspend fun findOne(key: K): T

    suspend fun findOne(key: K, fields: List<String>): Map<String, *>

    suspend fun findMany(
        filter: Map<String, List<*>>,
        limit: Int? = null,
        skip: Int? = null,
        sort: Map<String, Boolean> = emptyMap()): Channel<T>

    suspend fun findMany(
        filter: Map<String, List<*>>,
        fields: List<String>,
        limit: Int? = null,
        skip: Int? = null,
        sort: Map<String, Boolean> = emptyMap()): Channel<Map<String, *>>

    suspend fun count(filter: Map<String, List<*>> = emptyMap()): Long
}
