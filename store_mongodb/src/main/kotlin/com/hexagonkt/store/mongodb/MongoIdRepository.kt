/*
package com.hexagonkt.vertx.store.mongodb

import com.hexagonkt.helpers.Logger
import com.hexagonkt.helpers.error
import com.hexagonkt.helpers.filterEmpty
import com.hexagonkt.helpers.logger
import com.hexagonkt.vertx.store.Mapper
import com.hexagonkt.vertx.store.Store
import com.hexagonkt.vertx.toJsonObject
import io.vertx.core.Future
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.*
import io.vertx.ext.mongo.BulkOperation.*
import io.vertx.kotlin.core.json.JsonObject

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties

class MongoDbStore<T : Any, K : Any>(
    @Suppress("MemberVisibilityCanBePrivate")
    private val mongoDbClient: MongoClient,
    override val type: KClass<T>,
    override val key: KProperty1<T, K>,
    override val name: String = type.simpleName ?: error("Type name cannot be 'null'"),
    override val mapper: Mapper<T> = MongoDbMapper(type, key)
): Store<T, K> {

    private val logger: Logger = logger()

    override val fields: List<String> by lazy {
        logger.time ("REFLECT") { type.declaredMemberProperties } // TODO This is *VERY* slow
            .map { it.name }
    }

    override fun insertOne(instance: T): Future<K> {
        val future = Future.future<String>()
        mongoDbClient.insert(name, JsonObject(mapper.toStore(instance)), future)
        return future.map {
            logger.info { "Record inserted in '$name' with key: $it" }
            // TODO This works only if key is not generated
            @Suppress("UNCHECKED_CAST")
            if (it == null) key.get(instance)
            else it as K
        }
    }

    override fun insertMany(instances: List<T>): Future<Long> =
        executeBulkOperation(instances) {
            createInsert(JsonObject(mapper.toStore(it)))
        }
        .map { it.insertedCount }

    override fun saveOne(instance: T): Future<K> {
        val future = Future.future<String>()
        mongoDbClient.save(name, JsonObject(mapper.toStore(instance)), future)
        return future.map {
            // TODO This works only if key is not generated
            key.get(instance)
        }
    }

    override fun saveMany(instances: List<T>): Future<Long> =
        executeBulkOperation(instances) {
            createInsert(JsonObject(mapper.toStore(it))).setUpsert(true)
        }
        .map { it.insertedCount + it.modifiedCount }

    override fun replaceOne(instance: T): Future<Boolean> {
        val future = Future.future<MongoClientUpdateResult>()
        val document = JsonObject(mapper.toStore(instance))
        val query = JsonObject("_id" to key.get(instance))
        mongoDbClient.replaceDocuments(name, query, document, future)
        return future.map { it.docMatched == 1L && it.docMatched == it.docModified }
    }

    override fun replaceMany(instances: List<T>): Future<Long> =
        executeBulkOperation(instances) {
            val query = JsonObject("_id" to key.get(it))
            createReplace(query, JsonObject(mapper.toStore(it)))
        }
        .map { it.modifiedCount }

    override fun updateOne(key: K, updates: Map<String, *>): Future<Boolean> {
        val future = Future.future<MongoClientUpdateResult>()
        val query = JsonObject("_id" to key)
        val document = createUpdate(updates)
        mongoDbClient.updateCollection(name, query, document, future)
        return future.map { it.docModified == 1L }
    }

    override fun updateMany(filter: Map<String, List<*>>, updates: Map<String, *>): Future<Long> {
        val future = Future.future<MongoClientUpdateResult>()
        mongoDbClient.updateCollection(name, JsonObject(), JsonObject(), future)
        return future.map { it.docModified }
    }

    override fun deleteOne(id: K): Future<Boolean> {
        val future = Future.future<MongoClientDeleteResult>()
        val query = JsonObject("_id" to id)
        mongoDbClient.removeDocument(name, query, future)
        return future.map { it.removedCount == 1L }
    }

    override fun deleteMany(filter: Map<String, List<*>>): Future<Long> {
        val future = Future.future<MongoClientDeleteResult>()
        mongoDbClient.removeDocuments(name, createFilter(filter), future)
        return future.map { it.removedCount }
    }

    override fun findOne(key: K): Future<T?> {
        val future = Future.future<JsonObject>()
        val query = JsonObject("_id" to key)
        mongoDbClient.findOne(name, query, JsonObject(), future)
        return future.map { if (it == null) null else mapper.fromStore(remap(it)) }
    }

    override fun findOne(key: K, fields: List<String>): Future<Map<String, *>?> {
        val future = Future.future<JsonObject>()
        val query = JsonObject("_id" to key)
        mongoDbClient.findOne(name, query, createProjection(fields), future)
        return future.map { it.map }
    }

    override fun findMany(
        filter: Map<String, List<*>>,
        limit: Int?,
        skip: Int?,
        sort: Map<String, Boolean>): Future<List<T>> =
            find(filter, emptyList(), limit, skip, sort)
                .map { rows ->
                    rows.map { row ->
                        val rowMap = remap(row)
                        mapper.fromStore(rowMap)
                    }
                }

    override fun findMany(
        filter: Map<String, List<*>>,
        fields: List<String>,
        limit: Int?,
        skip: Int?,
        sort: Map<String, Boolean>): Future<List<Map<String, *>>> =
            find(filter, fields, limit, skip, sort)
                .map { rows ->
                    rows.map { row ->
                        // TODO Move this to mapper
                        @Suppress("UNCHECKED_CAST")
                        remap(row) + (key.name to (row.map["_id"] as? K ?: error)) - "_id"
                    }
                }

    override fun count(filter: Map<String, List<*>>): Future<Long> {
        val future = Future.future<Long>()
        mongoDbClient.count(name, createFilter(filter), future)
        return future
    }

    override fun drop(): Future<Void> {
        val future = Future.future<Void>()
        mongoDbClient.dropCollection(name, future)
        return future
    }

    private fun find(
        filter: Map<String, List<*>>,
        fields: List<String>,
        limit: Int?,
        skip: Int?,
        sort: Map<String, Boolean>): Future<List<JsonObject>> {

        val future = Future.future<List<JsonObject>>()
        val query = createFilter(filter)
        val findOptions = FindOptions()
        if (fields.isNotEmpty()) findOptions.fields = createProjection(fields)
        if (limit != null) findOptions.limit = limit
        if (skip != null) findOptions.skip = skip
        if (sort.isNotEmpty()) findOptions.sort = createSort(sort)

        mongoDbClient.findWithOptions(name, query, findOptions, future)
        return future
    }

    private fun executeBulkOperation(instances: List<T>, operation: (T) -> BulkOperation):
        Future<MongoClientBulkWriteResult> {

        val map = instances.map(operation)
        val future = Future.future<MongoClientBulkWriteResult>()
        mongoDbClient.bulkWrite(name, map, future)
        return future
    }
}
 */
package com.hexagonkt.store.mongodb

import com.mongodb.client.FindIterable
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.*
import com.mongodb.client.model.Indexes.ascending
import com.mongodb.client.model.Indexes.descending
import org.bson.Document
import org.bson.conversions.Bson
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.jvm.javaType

open class MongoIdRepository<T : Any, K : Any> (
    type: KClass<T>,
    collection: MongoCollection<Document>,
    private val key: KProperty1<T, K>,
    indexOrder: Int? = 1,
    onStore: (Document) -> Document = { it },
    onLoad: (Document) -> Document = { it }) :
        MongoRepository<T> (type, collection, onStore, onLoad) {

    @Suppress("UNCHECKED_CAST")
    val keyType: KClass<K> = key.returnType.kclass() as KClass<K>

    constructor (
        type: KClass<T>,
        database: MongoDatabase,
        key: KProperty1<T, K>,
        indexOrder: Int? = 1,
        onStore: (Document) -> Document = { it },
        onLoad: (Document) -> Document = { it }) :
            this (
                type,
                database.getCollection(type.simpleName ?: error("Error getting type name")),
                key,
                indexOrder,
                onStore,
                onLoad
            )

    init {
        if (indexOrder != null)
            createIndex (if(indexOrder == 1) ascending(key.name) else descending(key.name), true)
    }

    private fun createIndex(keys: Bson, unique: Boolean = false, background: Boolean = true): String =
        createIndex(keys, IndexOptions().unique(unique).background(background))

    protected open fun convertKeyName(keyName: String): String = keyName
    protected open fun convertId(id: K): Any = id

    fun deleteId (documentId: K) {
        deleteOne (convertKeyName(key.name) eq convertId(documentId))
    }

    fun deleteIds (vararg documentId: K) {
        deleteIds (documentId.toList ())
    }

    fun deleteIds (documentIds: List<K>) {
        deleteMany (convertKeyName(key.name) isIn documentIds.map { convertId(it) })
    }

    fun deleteObject (documentId: T) {
        deleteOne (convertKeyName(key.name) eq convertId((key.getter)(documentId)))
    }

    fun deleteObjects (vararg documentId: T) {
        deleteObjects (documentId.toList())
    }

    fun deleteObjects (documentIds: List<T>) {
        val ids = documentIds.map { convertId((key.getter)(it)) }
        deleteMany (convertKeyName(key.name) isIn ids)
    }

    fun replaceObject (document: T, upsert: Boolean = false) =
        replaceOneObject (
            convertKeyName(key.name) eq convertId((key.getter)(document)),
            document,
            if (upsert) ReplaceOptions().upsert(true) else ReplaceOptions()
        )

    fun replaceObjects (vararg document: T, upsert: Boolean = false, bulk: Boolean = false) {
        replaceObjects (document.toList(), upsert, bulk)
    }

    fun replaceObjects (document: List<T>, upsert: Boolean = false, bulk: Boolean = false) {
        if (bulk) {
            val keyName = convertKeyName(key.name)
            bulkWrite(
                document.map { ReplaceOneModel(keyName eq convertId((key.getter)(it)), map(it)) },
                BulkWriteOptions().ordered(false)
            )
        }
        else {
            document.forEach { replaceObject(it, upsert) }
        }
    }

    fun find (vararg documentId: K): List<T> = find (documentId.toList ())

    fun find (documentId: List<K>, setup: FindIterable<*>.() -> Unit = {}): List<T> =
        findObjects (convertKeyName(key.name) isIn documentId.map { convertId(it) }) {
            setup()
        }.toList()

    fun find (documentId: K): T? =
        findObjects (convertKeyName(key.name) eq convertId(documentId)).first ()

    fun isEmpty() = countDocuments() == 0L

    fun getKey (obj: T): K = (key.getter)(obj)

    private fun KType.kclass(): KClass<*> = when (this.javaType.typeName) {
        "boolean" -> Boolean::class
        "int" -> Int::class
        "long" -> Long::class
        "short" -> Short::class
        "double" -> Double::class
        "float" -> Float::class
        else -> Class.forName(this.javaType.typeName).kotlin
    }

    // TODO Check that parameter is simple type... Ie: fails with LocalDate
    infix fun <T> String.eq(value: T): Bson = Filters.eq(this, value)
    infix fun <T> String.isIn(value: Collection<T>): Bson = Filters.`in`(this, value)
}
