package com.hexagonkt.store.mongodb

import com.hexagonkt.helpers.logger
import com.hexagonkt.store.Store
import com.mongodb.async.client.MongoClients.getDefaultCodecRegistry
import com.mongodb.async.client.MongoCollection
import com.mongodb.async.client.MongoDatabase
import com.mongodb.client.model.CreateCollectionOptions
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.runBlocking
import org.bson.codecs.configuration.CodecRegistries.fromProviders
import org.bson.codecs.configuration.CodecRegistries.fromRegistries
import org.bson.types.ObjectId
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

/**
 * TODO .
 */
class MongoDbStore <T : Any, K : Any>(
    override val type: KClass<T>,
    override val key: KProperty1<T, K>,
    override val name: String = type.simpleName ?: error("Invalid name"),
    database: MongoDatabase,
    private val useObjectId: Boolean = false,
    indexOrder: Int = 1) : Store<T, K> {

    private val database = database.withCodecRegistry(
        fromRegistries(getDefaultCodecRegistry(),
        fromProviders(JacksonCodecProvider(key, useObjectId)))
    )

    private val typedCollection: MongoCollection<T> =
        this.database.getCollection<T>(name, type.java)

    init {
        if (useObjectId)
            // TODO Make it sync
            database.createCollection(name, CreateCollectionOptions().autoIndex(false)) { _, _ ->
               logger.info { "Collection without auto index created for: $name" }
            }

        val index =
            if (indexOrder == 1) Indexes.ascending(key.name)
            else Indexes.descending(key.name)

        typedCollection.createIndex(index, IndexOptions().unique(true).background(true)) { _, _ ->
            logger.info { "Index created for: $name with field: ${key.name}" }
        }
    }

    override suspend fun insertOne(instance: T): K = suspendCoroutine {
        typedCollection.insertOne(instance) { _, error ->
            if (error == null) it.resume(key.get(instance))
            else it.resumeWithException(error)
        }
    }

    override suspend fun insertMany(instances: List<T>): ReceiveChannel<K> = GlobalScope.produce {
        if (!instances.isEmpty())
            suspendCoroutine<Unit> {
                typedCollection.insertMany(instances) { _, error ->
                    if (error == null) {
                        runBlocking {
                            instances.forEach { instance -> send(key.get(instance))}
                        }
                        it.resume(Unit)
                    }
                    else it.resumeWithException(error)
                }
            }
    }

    override suspend fun replaceOne(instance: T): Boolean = suspendCoroutine {
        typedCollection.replaceOne(eq(key.name, getKey(instance)), instance) { result, error ->
            if (error == null) it.resume(result.modifiedCount == 1L)
            else it.resumeWithException(error)
        }
    }

    override suspend fun replaceMany(instances: List<T>): ReceiveChannel<T> = GlobalScope.produce {
        instances.map { if (replaceOne(it)) send(it) }
    }

    override suspend fun updateOne(key: K, updates: Map<String, *>): Boolean = suspendCoroutine {
 //        return updateOne(key, createUpdate(updates))
        TODO("not implemented")
    }

    override suspend fun updateMany(filter: Map<String, List<*>>, updates: Map<String, *>): Long {
        TODO("not implemented")
    }

    override suspend fun deleteOne(id: K): Boolean {
        TODO("not implemented")
    }

    override suspend fun deleteMany(filter: Map<String, List<*>>): Long {
        TODO("not implemented")
    }

    override suspend fun findOne(key: K): T? = suspendCoroutine {
        typedCollection.find (eq (this.key.name, convertKey (key))).first { result, error ->
            if (error == null) it.resume(result)
            else it.resumeWithException(error)
        }
    }

    override suspend fun findOne(key: K, fields: List<String>): Map<String, *> {
        TODO("not implemented")
    }

    override suspend fun findMany(
        filter: Map<String, List<*>>,
        limit: Int?,
        skip: Int?,
        sort: Map<String, Boolean>): Channel<T> {

        TODO("not implemented")
    }

    override suspend fun findMany(
        filter: Map<String, List<*>>,
        fields: List<String>,
        limit: Int?,
        skip: Int?,
        sort: Map<String, Boolean>): Channel<Map<String, *>> {

        TODO("not implemented")
    }

    override suspend fun count(filter: Map<String, List<*>>): Long {
        TODO("not implemented")
    }

    private fun convertKey(instance: K): Any =
        if (useObjectId) ObjectId(instance.toString()) else instance

    private fun getKey(instance: T): Any = key.get(instance).let {
        if (useObjectId) ObjectId(it.toString()) else it
    }
}
