package com.hexagonkt.store.mongodb

import com.hexagonkt.store.Mapper
import com.hexagonkt.store.Store
import com.mongodb.client.MongoDatabase
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.CreateCollectionOptions
import com.mongodb.client.model.Filters.eq
import org.bson.Document
import org.bson.types.ObjectId
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

    private val collection: MongoCollection<Document> = this.database.getCollection(name)

    init {
        if (useObjectId)
            database.createCollection(name, CreateCollectionOptions())

//        val index =
//            if (indexOrder == 1) Indexes.ascending(key.name)
//            else Indexes.descending(key.name)

//        typedCollection.createIndex(index, IndexOptions().unique(true).background(true)) { _, _ ->
//            logger.info { "Index created for: $name with field: ${key.name}" }
//        }
    }

    override fun insertOne(instance: T): K {
        collection.insertOne(map(instance))
        return key.get(instance)
    }

    override fun insertMany(instances: List<T>): List<K> {

        return if (!instances.isEmpty()) {
            val map = instances.map { instance -> map(instance) }
            collection.insertMany(map)
            return instances.map { key.get(it) }
        }
        else {
            emptyList()
        }
    }

    override fun saveOne(instance: T): K {
        TODO("not implemented")
    }

    override fun saveMany(instances: List<T>): Long {
        TODO("not implemented")
    }

    override fun replaceOne(instance: T): Boolean {
        val document = Document(mapper.toStore(instance))
//        typedCollection.replaceOne(eq(key.name, getKey(instance)), document) { result, error ->
        val result = collection.replaceOne(eq("_id", getKey(instance)), document)
        return result.modifiedCount == 1L
    }

    override fun replaceMany(instances: List<T>): List<T> =
        instances.mapNotNull { if (replaceOne(it)) it else null }

    override fun updateOne(key: K, updates: Map<String, *>): Boolean {
        TODO("not implemented")
    }

    override fun updateMany(filter: Map<String, List<*>>, updates: Map<String, *>): Long {
        TODO("not implemented")
    }

    override fun deleteOne(id: K): Boolean {
        TODO("not implemented")
    }

    override fun deleteMany(filter: Map<String, List<*>>): Long {
        TODO("not implemented")
    }

    override fun findOne(key: K): T? {
        // TODO Handle null in mapper.fromStore
        val result = collection.find (eq ("_id", convertKey (key))).first()
        return mapper.fromStore(result as Map<String, Any?>)
    }

    override fun findOne(key: K, fields: List<String>): Map<String, *> {
        TODO("not implemented")
    }

    override fun findMany(
        filter: Map<String, List<*>>,
        limit: Int?,
        skip: Int?,
        sort: Map<String, Boolean>): List<T> {

        TODO("not implemented")
    }

    override fun findMany(
        filter: Map<String, List<*>>,
        fields: List<String>,
        limit: Int?,
        skip: Int?,
        sort: Map<String, Boolean>): List<Map<String, *>> {

        TODO("not implemented")
    }

    override fun count(filter: Map<String, List<*>>): Long {
        TODO("not implemented")
    }

    override fun drop() {
        collection.drop()
    }

    private fun convertKey(instance: K): Any =
        if (useObjectId) ObjectId(instance.toString()) else instance

    private fun getKey(instance: T): Any = key.get(instance).let {
        if (useObjectId) ObjectId(it.toString()) else it
    }

    private fun map(instance: T): Document = Document(mapper.toStore(instance))
}
