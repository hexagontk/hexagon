package com.hexagonkt.store.mongodb

import com.hexagonkt.helpers.error
import com.hexagonkt.helpers.filterEmpty
import com.hexagonkt.helpers.logger
import com.hexagonkt.store.IndexOrder
import com.hexagonkt.store.IndexOrder.ASCENDING
import com.hexagonkt.store.Mapper
import com.hexagonkt.store.Store
import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import com.mongodb.client.FindIterable
import com.mongodb.client.MongoDatabase
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.*
import org.bson.Document
import org.bson.conversions.Bson
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties

class MongoDbStore <T : Any, K : Any>(
    override val type: KClass<T>,
    override val key: KProperty1<T, K>,
    private val database: MongoDatabase,
    override val name: String = type.java.simpleName,
    override val mapper: Mapper<T> = MongoDbMapper(type, key)) : Store<T, K> {

    companion object {
        fun database(url: String): MongoDatabase = MongoClientURI(url).let {
            MongoClient(it).getDatabase(it.database ?: error())
        }
    }

    private val fields: List<String> by lazy {
        logger.time ("REFLECT") { type.declaredMemberProperties } // TODO This is *VERY* slow
            .map { it.name }
    }

    private val collection: MongoCollection<Document> = this.database.getCollection(name)

    init {
        if (key.name != "_id")
            createIndex(true, key.name to ASCENDING)
    }

    constructor(
        type: KClass<T>, key: KProperty1<T, K>, url: String, name: String = type.java.simpleName) :
            this(type, key, database(url), name)

    override fun createIndex(unique: Boolean, fields: Map<String, IndexOrder>): String {
        val indexes = fields.entries.map {
            if (it.value == ASCENDING) Indexes.ascending(it.key)
            else Indexes.descending(it.key)
        }

        val name = fields.entries.joinToString("_") {
            it.key + "_" + it.value.toString().toLowerCase()
        }

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
        val filter = createKeyFilter(key.get(instance))
        val options = UpdateOptions().upsert(true)
        val replaceOptions = ReplaceOptions.createReplaceOptions(options)
        val result = collection.replaceOne(filter, map(instance), replaceOptions)
        val upsertedId = result.upsertedId

        @Suppress("UNCHECKED_CAST")
        return if (upsertedId == null) null
            else mapper.fromStore(key.name, upsertedId as Any) as K
    }

    override fun saveMany(instances: List<T>): List<K?> =
        instances.map(this::saveOne)

    override fun replaceOne(instance: T): Boolean {
        val document = map(instance)
        val filter = createKeyFilter(key.get(instance))
        val result = collection.replaceOne(filter, document)
        // *NOTE* that 'modifiedCount' returns 0 for matched records with unchanged update values
        return result.matchedCount == 1L
    }

    override fun replaceMany(instances: List<T>): List<T> =
        instances.mapNotNull { if (replaceOne(it)) it else null }

    override fun updateOne(key: K, updates: Map<String, *>): Boolean {
        val filter = createKeyFilter(key)
        val update = createUpdate(updates)
        val result = collection.updateOne(filter, update)
        // *NOTE* that 'modifiedCount' returns 0 for matched records with unchanged update values
        return result.matchedCount == 1L
    }

    override fun updateMany(filter: Map<String, *>, updates: Map<String, *>): Long {
        val updateFilter = createFilter(filter)
        val update = createUpdate(updates)
        val result = collection.updateMany(updateFilter, update)
        // *NOTE* that 'modifiedCount' returns 0 for matched records with unchanged update values
        return result.matchedCount
    }

    override fun deleteOne(id: K): Boolean {
        val filter = createKeyFilter(id)
        val result = collection.deleteOne(filter)
        return result.deletedCount == 1L
    }

    override fun deleteMany(filter: Map<String, *>): Long {
        val deleteFilter = createFilter(filter)
        val result = collection.deleteMany(deleteFilter)
        return result.deletedCount
    }

    override fun findOne(key: K): T? {
        val result = collection.find(createKeyFilter(key)).first()?.filterEmpty()
        return mapper.fromStore(result as Map<String, Any>)
    }

    override fun findOne(key: K, fields: List<String>): Map<String, *>? {
        val filter = createKeyFilter(key)
        val result = collection
            .find(filter)
            .projection(createProjection(fields))
            .first()?.filterEmpty() ?: error()

        return result.mapValues { mapper.toStore(it.key, it.value as Any) }
    }

    override fun findMany(
        filter: Map<String, *>,
        limit: Int?,
        skip: Int?,
        sort: Map<String, Boolean>): List<T> {

        val findFilter = createFilter(filter)
        val findSort = createSort(sort)
        val query = collection.find(findFilter).sort(findSort)

        pageQuery(limit, query, skip)

        val result = query.into(ArrayList())
        return result.map { mapper.fromStore(it.filterEmpty()) }
    }

    override fun findMany(
        filter: Map<String, *>,
        fields: List<String>,
        limit: Int?,
        skip: Int?,
        sort: Map<String, Boolean>): List<Map<String, *>> {

        val findFilter = createFilter(filter)
        val projection = createProjection(fields)
        val findSort = createSort(sort)
        val query = collection.find(findFilter).projection(projection).sort(findSort)

        pageQuery(limit, query, skip)

        val result = query.into(ArrayList())

        return result.map { resultMap ->
            resultMap.map { pair ->
                pair.key to mapper.fromStore(pair.key, pair.value)
            }
            .toMap()
        }
    }

    override fun count(filter: Map<String, *>): Long {
        val countFilter = createFilter(filter)
        return collection.countDocuments(countFilter)
    }

    override fun drop() {
        collection.drop()
    }

    private fun pageQuery(limit: Int?, query: FindIterable<Document>, skip: Int?) {
        if (limit != null)
            query.limit(limit)

        if (skip != null)
            query.skip(skip)
    }

    private fun map(instance: T): Document = Document(mapper.toStore(instance))

    private fun createKeyFilter(key: K) = Filters.eq("_id", key)

    private fun createFilter(filter: Map<String, *>): Bson = filter
        .filterEmpty()
        .filter {
            val firstKeySegment = it.key.split ("\\.")[0]
            fields.contains (firstKeySegment)
        }
        .map {
            val key = it.key

            when (val value = it.value) {
                is List<*> ->
                    if (value.size > 1) Filters.`in`(key, value)
                    else Filters.eq(key, value.first())
                else ->
                    Filters.eq(key, value)
            }
        }
        .let {
            if (it.isEmpty()) Document()
            else Filters.and(it)
        }

    private fun createUpdate (update: Map<String, *>): Bson =
        Updates.combine(
            update
                .filterEmpty()
                .mapValues { mapper.toStore(it.key, it.value as Any) }
                .map { Updates.set(it.key, it.value) }
        )

    private fun createProjection (fields: List<String>): Bson =
        if(fields.isEmpty ()) Document()
        else
            fields
                .asSequence()
                .filter { fields.contains(it) }
                .map { it to 1 }
                .toMap()
                .toDocument()
                .append("_id", 0)

    private fun createSort(fields : Map<String, Boolean>): Bson =
        fields
            .filter { fields.contains (it.key) }
            .mapValues { if (it.value) -1 else 1 }
            .toDocument()

    private fun Map<String, *>.toDocument() = Document(this)
}
