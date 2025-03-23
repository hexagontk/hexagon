package com.hexagontk.store.mongodb

import com.hexagontk.core.fail
import com.hexagontk.core.filterNotEmpty
import com.hexagontk.core.toLocalDateTime
import com.hexagontk.store.Store
import com.mongodb.ConnectionString
import com.mongodb.client.FindIterable
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.client.model.Updates
import org.bson.BsonBinary
import org.bson.BsonString
import org.bson.Document
import org.bson.conversions.Bson
import org.bson.types.Binary
import java.net.URL
import java.nio.ByteBuffer
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class MongoDbStore<T : Any, K : Any>(
    override val type: KClass<T>,
    override val key: KProperty1<T, K>,
    private val database: MongoDatabase,
    override val name: String = type.java.simpleName,
    override val encoder: (T) -> Map<String, *>,
    override val decoder: (Map<String, *>) -> T,
) : Store<T, K> {

    companion object {
        fun database(url: String): MongoDatabase = ConnectionString(url).let {
            MongoClients.create(it).getDatabase(it.database ?: fail)
        }
    }

    val collection: MongoCollection<Document> = this.database.getCollection(name)

    constructor(
        type: KClass<T>,
        key: KProperty1<T, K>,
        url: String,
        name: String = type.java.simpleName,
        encoder: (T) -> Map<String, *>,
        decoder: (Map<String, *>) -> T,
    ) :
        this(type, key, database(url), name, encoder, decoder)

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
        val options = ReplaceOptions().upsert(true)
        val result = collection.replaceOne(filter, map(instance), options)
        val upsertedId = result.upsertedId

        @Suppress("UNCHECKED_CAST")
        return if (upsertedId == null) null
            else fromStore(upsertedId as Any) as K
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
        require(updates.isNotEmpty())
        val filter = createKeyFilter(key)
        val update = createUpdate(updates)
        val result = collection.updateOne(filter, update)
        // *NOTE* that 'modifiedCount' returns 0 for matched records with unchanged update values
        return result.matchedCount == 1L
    }

    override fun updateMany(filter: Map<String, *>, updates: Map<String, *>): Long {
        require(updates.isNotEmpty())
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
        val result = collection.find(createKeyFilter(key)).first()?.filterNotEmpty()
        return if (result == null) null else fromStore(result)
    }

    override fun findOne(key: K, fields: List<String>): Map<String, *>? {
        val filter = createKeyFilter(key)
        val result = collection
            .find(filter)
            .projection(createProjection(fields))
            .first()?.filterNotEmpty()

        return result?.mapValues { fromStore(it.value) }
    }

    override fun findMany(
        filter: Map<String, *>,
        limit: Int?,
        skip: Int?,
        sort: Map<String, Boolean>
    ): List<T> {

        val findFilter = createFilter(filter)
        val findSort = createSort(sort)
        val query = collection.find(findFilter).sort(findSort)

        pageQuery(limit, query, skip)

        val result = query.into(ArrayList())
        return result.map { fromStore(it.filterNotEmpty()) }
    }

    override fun findMany(
        filter: Map<String, *>,
        fields: List<String>,
        limit: Int?,
        skip: Int?,
        sort: Map<String, Boolean>
    ): List<Map<String, *>> {

        val findFilter = createFilter(filter)
        val projection = createProjection(fields)
        val findSort = createSort(sort)
        val query = collection.find(findFilter).projection(projection).sort(findSort)

        pageQuery(limit, query, skip)

        val result = query.into(ArrayList())

        return result.map { resultMap ->
            resultMap
                .map { pair -> pair.key to fromStore(pair.value) }
                .toMap()
                .filterNotEmpty()
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

    private fun map(instance: T): Document = Document(toStore(instance))

    private fun createKeyFilter(key: K) = Filters.eq("_id", key)

    private fun createFilter(filter: Map<String, *>): Bson = filter
        .filterNotEmpty()
        .map {
            val keyFields = it.key.split(":")
            val key = keyFields.firstOrNull() ?: fail
            val collectionKey = if (key == this.key.name) "_id" else key
            val operator = keyFields.getOrNull(1)
            val value = it.value

            when {
                value is List<*> ->
                    if (value.size > 1) Filters.`in`(collectionKey, value)
                    else Filters.eq(collectionKey, value.first())
                operator != null ->
                    when (operator) {
                        "gt" -> Filters.gt(collectionKey, value)
                        "gte" -> Filters.gte(collectionKey, value)
                        "lt" -> Filters.lt(collectionKey, value)
                        "lte" -> Filters.lte(collectionKey, value)
                        "re" -> Filters.regex(collectionKey, value.toString())
                        else -> Filters.eq(collectionKey, value)
                    }
                else ->
                    Filters.eq(collectionKey, value)
            }
        }
        .let {
            if (it.isEmpty()) Document()
            else Filters.and(it)
        }

    private fun createUpdate(update: Map<String, *>): Bson =
        Updates.combine(
            update
                .filter { it.value != null }
                .mapValues { toStore(it.value as Any) }
                .map { Updates.set(it.key, it.value) }
        )

    private fun createProjection(fields: List<String>): Bson =
        if (fields.isEmpty()) Document()
        else
            fields
                .asSequence()
                .filter { fields.contains(it) }
                .map { it to 1 }
                .toMap()
                .toDocument()
                .append("_id", 0)

    private fun createSort(fields: Map<String, Boolean>): Bson =
        fields
            .filter { fields.contains(it.key) }
            .mapValues { if (it.value) -1 else 1 }
            .toDocument()

    private fun Map<String, *>.toDocument() = Document(this)

    private fun toStore(instance: T): Map<String, Any> =
        (encoder(instance) + ("_id" to key.get(instance)) - key.name)
            .filterNotEmpty()
            .mapKeys { it.key }
            .mapValues { toStore(it.value) }

    private fun fromStore(map: Map<String, Any>): T =
        (map + (key.name to map["_id"]))
            .filterNotEmpty()
            .mapValues { fromStore(it.value) }
            .let(decoder)

    private fun fromStore(value: Any): Any = when (value) {
        is Binary -> value.data
        is BsonBinary -> value.data
        is BsonString -> value.value
        is Date -> value.toLocalDateTime()
        is Iterable<*> -> value.map { i -> i?.let { fromStore(it) } }
        is Map<*, *> -> value.mapValues { v -> v.value?.let { fromStore(it) } }
        else -> value
    }

    private fun toStore(value: Any): Any = when (value) {
        is Enum<*> -> value.name
        is ByteArray -> BsonBinary(value)
        is ByteBuffer -> BsonBinary(value.array())
        is URL -> value.toString()
        is LocalDateTime -> value
            .atZone(ZoneId.systemDefault())
            .withZoneSameInstant(ZoneOffset.UTC)
            .toLocalDateTime()
        is Iterable<*> -> value.map { i -> i?.let { toStore(it) } }
        is Map<*, *> -> value.mapValues { v -> v.value?.let { toStore(it) } }
        else -> value
    }
}
