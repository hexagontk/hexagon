package com.hexagonkt.store.mongodb

import com.hexagonkt.helpers.logger
import com.hexagonkt.store.Mapper
import com.hexagonkt.store.Store
import com.mongodb.async.client.MongoDatabase
import com.mongodb.async.client.MongoCollection
import com.mongodb.client.model.CreateCollectionOptions
import com.mongodb.client.model.Filters.eq
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.runBlocking
import org.bson.Document
import org.bson.types.ObjectId
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class MongoDbStore <T : Any, K : Any>(
    override val type: KClass<T>,
    override val key: KProperty1<T, K>,
    override val name: String = type.simpleName ?: error("Invalid name"),
    private val database: MongoDatabase,
    private val useObjectId: Boolean = false,
    indexOrder: Int = 1,
    override val mapper: Mapper<T> = MongoDbMapper(type, key)) : Store<T, K> {

    private val typedCollection: MongoCollection<Document> = this.database.getCollection(name)

    override suspend fun saveOne(instance: T): K {
        TODO("not implemented")
    }

    override suspend fun saveMany(instances: List<T>): Long {
        TODO("not implemented")
    }

    override suspend fun drop(): Unit = suspendCoroutine {
        typedCollection.drop { _, error ->
            if (error == null) it.resume(Unit)
            else it.resumeWithException(error)
        }
    }

    init {
        if (useObjectId)
            // TODO Make it sync
            database.createCollection(name, CreateCollectionOptions()) { _, _ ->
               logger.info { "Collection without auto index created for: $name" }
            }

//        val index =
//            if (indexOrder == 1) Indexes.ascending(key.name)
//            else Indexes.descending(key.name)

//        typedCollection.createIndex(index, IndexOptions().unique(true).background(true)) { _, _ ->
//            logger.info { "Index created for: $name with field: ${key.name}" }
//        }
    }

    override suspend fun insertOne(instance: T): K = suspendCoroutine {
        typedCollection.insertOne(Document(mapper.toStore(instance))) { _, error ->
            if (error == null) it.resume(key.get(instance))
            else it.resumeWithException(error)
        }
    }

    override suspend fun insertMany(instances: List<T>): ReceiveChannel<K> = GlobalScope.produce {

        if (!instances.isEmpty())
            suspendCoroutine<Unit> {
                val map = instances.map { instance -> Document(mapper.toStore(instance)) }
                typedCollection.insertMany(map) { _, error ->
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
        val document = Document(mapper.toStore(instance))
//        typedCollection.replaceOne(eq(key.name, getKey(instance)), document) { result, error ->
        typedCollection.replaceOne(eq("_id", getKey(instance)), document) { result, error ->
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
//        typedCollection.find (eq (this.key.name, convertKey (key))).first { result, error ->
        // TODO Handle null in mapper.fromStore
        typedCollection.find (eq ("_id", convertKey (key))).first { result, error ->
            if (error == null) it.resume(mapper.fromStore(result))
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
