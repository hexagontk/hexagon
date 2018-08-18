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
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.produce
import kotlinx.coroutines.experimental.runBlocking
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistries.fromProviders
import org.bson.codecs.configuration.CodecRegistries.fromRegistries
import org.bson.types.ObjectId
import kotlin.coroutines.experimental.suspendCoroutine
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
    useUnderscoreId: Boolean = true,
    indexOrder: Int = 1) : Store<T, K> {

    private val database = database.withCodecRegistry(
        fromRegistries(getDefaultCodecRegistry(),
        fromProviders(JacksonCodecProvider(key, useObjectId, useUnderscoreId)))
    )

    private val collection: MongoCollection<Document> = this.database.getCollection(name)
    private val typedCollection: MongoCollection<T> =
        this.database.getCollection<T>(name, type.java)

    init {
        if (useObjectId)
            database.createCollection(
                this.name,
                CreateCollectionOptions().autoIndex(false),
                { _, t -> t } // TODO Make it sync
            )

        typedCollection.createIndex(
            if (indexOrder == 1) Indexes.ascending(key.name) else Indexes.descending(key.name),
            IndexOptions().unique(true).background(true),
            { _, _ -> logger.info("Index created for: $name with field: ${key.name}") } // TODO Log
        )
    }

    suspend override fun insertOne(instance: T): K = suspendCoroutine {
        typedCollection.insertOne(instance) { _, error ->
            if (error == null) it.resume(key.get(instance))
            else it.resumeWithException(error)
        }
    }

    suspend override fun insertMany(instances: List<T>): ReceiveChannel<K> = produce {
        if (!instances.isEmpty())
            suspendCoroutine<Unit> {
                typedCollection.insertMany(instances) { _, error ->
                    if (error == null) {
                        runBlocking {
                            instances.forEach { send(key.get(it))}
                        }
                        it.resume(Unit)
                    }
                    else it.resumeWithException(error)
                }
            }
    }

    suspend override fun replaceOne(instance: T): Boolean = suspendCoroutine {
        typedCollection.replaceOne(eq(key.name, getKey(instance)), instance) { result, error ->
            if (error == null) it.resume(result.modifiedCount == 1L)
            else it.resumeWithException(error)
        }
    }

    suspend override fun replaceMany(instances: List<T>): ReceiveChannel<T> = produce {
        instances.map { if (replaceOne(it)) send(it) }
    }

    suspend override fun updateOne(key: K, updates: Map<String, *>): Boolean = suspendCoroutine {
 //        return updateOne(key, createUpdate(updates))
        TODO("not implemented")
    }

    suspend override fun updateMany(filter: Map<String, List<*>>, updates: Map<String, *>): Long {
        TODO("not implemented")
    }

    suspend override fun deleteOne(id: K): Boolean {
        TODO("not implemented")
    }

    suspend override fun deleteMany(filter: Map<String, List<*>>): Long {
        TODO("not implemented")
    }

    suspend override fun findOne(key: K): T? = suspendCoroutine {
        typedCollection.find (eq (this.key.name, convertKey (key))).first { result, error ->
            if (error == null) it.resume(result)
            else it.resumeWithException(error)
        }
    }

    suspend override fun findOne(key: K, fields: List<String>): Map<String, *> {
        TODO("not implemented")
    }

    suspend override fun findMany(
        filter: Map<String, List<*>>,
        limit: Int?,
        skip: Int?,
        sort: Map<String, Boolean>): Channel<T> {

        TODO("not implemented")
    }

    suspend override fun findMany(
        filter: Map<String, List<*>>,
        fields: List<String>,
        limit: Int?,
        skip: Int?,
        sort: Map<String, Boolean>): Channel<Map<String, *>> {

        TODO("not implemented")
    }

    suspend override fun count(filter: Map<String, List<*>>): Long {
        TODO("not implemented")
    }

    private fun convertKey(instance: K): Any =
        if (useObjectId) ObjectId(instance.toString()) else instance

    private fun getKey(instance: T): Any = key.get(instance).let {
        if (useObjectId) ObjectId(it.toString()) else it
    }
}
