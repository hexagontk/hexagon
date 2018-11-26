package com.hexagonkt.store.mongodb

import com.hexagonkt.helpers.filterEmpty
import com.hexagonkt.helpers.logger
import com.hexagonkt.store.IndexOrder
import com.hexagonkt.store.IndexOrder.ASCENDING
import com.hexagonkt.store.Mapper
import com.hexagonkt.store.Store
import com.mongodb.client.MongoDatabase
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.*
import com.mongodb.client.model.Filters.eq
import org.bson.Document
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties

class MongoDbStore <T : Any, K : Any>(
    override val type: KClass<T>,
    override val key: KProperty1<T, K>,
    override val name: String = type.java.simpleName,
    private val database: MongoDatabase,
    override val mapper: Mapper<T> = MongoDbMapper(type, key)) : Store<T, K> {

    private val collection: MongoCollection<Document> = this.database.getCollection(name)

    private val fields: List<String> by lazy {
        logger.time ("REFLECT") { type.declaredMemberProperties } // TODO This is *VERY* slow
            .map { it.name }
    }

    init {
        createIndex(true, key.name to ASCENDING)
    }

    override fun createIndex(unique: Boolean, fields: List<Pair<String, IndexOrder>>): String {
        val indexes = fields.map {
            if (it.second == ASCENDING) Indexes.ascending(it.first)
            else Indexes.descending(it.first)
        }

        val name = fields.joinToString("_") { it.first + "_" + it.second.toString().toLowerCase() }
        val compoundIndex = Indexes.compoundIndex(indexes)
        val indexOptions = IndexOptions().unique(unique).background(true).name(name)

        return collection.createIndex(compoundIndex, indexOptions)
    }

    override fun insertOne(instance: T): K {
        collection.insertOne(map(instance))
        return key.get(instance)
    }

    override fun insertMany(instances: List<T>): List<K> {
        collection.insertMany(instances.map { instance -> map(instance) })
        return instances.map { key.get(it) }
    }

    override fun saveOne(instance: T): K? {
        val filter = eq("_id", key.get(instance))
        val options = UpdateOptions().upsert(true)
        val replaceOptions = ReplaceOptions.createReplaceOptions(options)
        val result = collection.replaceOne(filter, map(instance), replaceOptions)
        // TODO
        return result.upsertedId as? K
    }

    override fun saveMany(instances: List<T>): List<K?> =
        instances.map(this::saveOne)

    override fun replaceOne(instance: T): Boolean {
        val document = map(instance)
        val filter = eq("_id", key.get(instance))
        val result = collection.replaceOne(filter, document)
        return result.modifiedCount == 1L
    }

    override fun replaceMany(instances: List<T>): List<T> =
        instances.mapNotNull { if (replaceOne(it)) it else null }

    override fun updateOne(key: K, updates: Map<String, *>): Boolean {
        val filter = eq("_id", key)

        val u = updates
            .filterEmpty()
            .mapValues { mapper.toStore(it.key, it.value as Any) }

        val result = collection.updateOne(filter, Document(u))
        return result.modifiedCount == 1L
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
        val result = collection.find (eq ("_id", key)).first()
        return mapper.fromStore(result as Map<String, Any>)
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

    private fun map(instance: T): Document = Document(mapper.toStore(instance))

    private fun createFilter(filter: Map<String, List<*>>): Document =
        filter
            .filterEmpty()
            .filter {
                val key = it.key
                val firstKeySegment = key.split ("\\.")[0]
                fields.contains (firstKeySegment)
            }
            .map {
                val key = it.key
                val value = it.value

                if (value.size > 1) key to mapOf("\$in" to value)
                else key to value[0]
            }
            .map {
                if (it.first == key.name) "_id" to it.second
                else it
            }
            .toMap()
            .toDocument()

    // TODO Transform values with mapper
    private fun createUpdate (update: Map<String, *>): Document =
        update
            .filterEmpty()
            .map { "\$set" to it }
            .toMap()
            .toDocument()

    // TODO Remove '_id', add "_id" to 0
    private fun createProjection (fields: List<String>): Document =
        if(fields.isEmpty ()) Document()
        else
            fields
                .asSequence()
                .filter { fields.contains(it) }
                .map { it to 1 }
                .toMap()
                .toDocument()

    private fun createSort(fields : Map<String, Boolean>): Document =
        fields
            .filter { fields.contains (it.key) }
            .mapValues { if (it.value) -1 else 1 }
            .toDocument()

    private fun Map<String, *>.toDocument() = Document(this)
}
