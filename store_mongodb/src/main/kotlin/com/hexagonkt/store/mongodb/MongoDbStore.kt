package com.hexagonkt.store.mongodb

import com.hexagonkt.helpers.Log
import com.hexagonkt.store.Store
import com.mongodb.async.client.MongoClients.getDefaultCodecRegistry
import com.mongodb.async.client.MongoCollection
import com.mongodb.async.client.MongoDatabase
import com.mongodb.client.model.CreateCollectionOptions
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import kotlinx.coroutines.experimental.channels.Channel
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistries.fromProviders
import org.bson.codecs.configuration.CodecRegistries.fromRegistries
import org.bson.types.ObjectId
import java.util.Objects.requireNonNull
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
    private val useObjectId: Boolean = true,
    useUnderscoreId: Boolean = true,
    indexOrder: Int? = null) : Store<T, K> {

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
            Indexes.ascending(key.name),
            IndexOptions().unique(true).background(true),
            { _, _ -> Log.info("Index created for: {} with field: {}", name, key.name) } // TODO Log
        )
    }

    suspend override fun insertOne(instance: T): K {
        requireNonNull(instance)
//        val future = Future.future()
//        typedCollection.insertOne(instance) { result, error ->
//            if (error == null)
//                future.complete(getKey(instance) as K) // TODO Check type! this can fail!
//            else
//                future.fail(error)
//        }
//        return future
        TODO("not implemented")
    }

    suspend override fun insertMany(instances: List<T>): Channel<K> {
//        requireNonNull(objects)
//        val future = Future.future()
//        if (!objects.isEmpty())
//            typedCollection.insertMany(objects, singleResultCallback(future))
//        return future
        TODO("not implemented")
    }

    suspend override fun replaceOne(instance: T): Boolean {
//        requireNonNull(`object`)
//        val future = Future.future()
//        val filter = eq(entity.keyName, getKey(`object`))
//        typedCollection.replaceOne(filter, `object`) { result, error ->
//            if (error == null)
//                future.complete(result.modifiedCount == 1L)
//            else
//                future.fail(error)
//        }
//        return future
        TODO("not implemented")
    }

    suspend override fun replaceMany(instances: List<T>): Channel<T> {
//        requireNonNull(objects)
//        return CompositeFuture.join(
//            objects.stream().map<Any> { `object` ->
//                val future = Future.future()
//                val filter = eq(entity.keyName, getKey(`object`))
//                typedCollection.replaceOne(filter, `object`, singleResultCallback(future))
//                future
//            }
//                .collect<R, A>(toList<T>())
//        )
        TODO("not implemented")
    }

    suspend override fun updateOne(key: K, updates: Map<String, *>): Boolean {
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

    suspend override fun findOne(key: K): T {
        TODO("not implemented")
    }

    suspend override fun findOne(key: K, fields: List<String>): Map<String, *> {
        TODO("not implemented")
    }

    suspend override fun findMany(filter: Map<String, List<*>>, limit: Int?, skip: Int?, sort: Map<String, Boolean>): Channel<T> {
        TODO("not implemented")
    }

    suspend override fun findMany(filter: Map<String, List<*>>, fields: List<String>, limit: Int?, skip: Int?, sort: Map<String, Boolean>): Channel<Map<String, *>> {
        TODO("not implemented")
    }

    suspend override fun count(filter: Map<String, List<*>>): Long {
        TODO("not implemented")
    }

    private fun convertKey(instance: K): Any =
        if (useObjectId) ObjectId(instance.toString()) else instance

    private fun getKey(instance: T): Any = key.get(instance).let (::convertKey)
}
