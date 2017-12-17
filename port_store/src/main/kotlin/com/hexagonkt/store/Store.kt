package com.hexagonkt.store

import kotlinx.coroutines.experimental.channels.Channel
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

interface Store<T : Any, K : Any> {
    fun getName(): String

    fun getType(): KClass<T>

    fun getKey(): KProperty1<T, K>

    suspend fun insertOne(`object`: T): K

    suspend fun insertMany(objects: List<T>): Channel<K>

    suspend fun replaceOne(`object`: T): Boolean

    suspend fun replaceMany(objects: List<T>): Channel<T>

    suspend fun updateOne(key: K, updates: Map<String, *>): Boolean

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
