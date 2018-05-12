package com.hexagonkt.vertx.store.mongodb

import com.hexagonkt.error
import com.hexagonkt.filterEmpty
import com.hexagonkt.logger
import com.hexagonkt.time
import com.hexagonkt.vertx.store.Mapper
import com.hexagonkt.vertx.store.Store
import com.hexagonkt.vertx.toJsonObject
import io.vertx.core.Future
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.*
import io.vertx.ext.mongo.BulkOperation.*
import io.vertx.kotlin.core.json.JsonObject
import org.slf4j.Logger
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties

class MongoDbStore<T : Any, K : Any>(
    @Suppress("MemberVisibilityCanBePrivate")
    val mongoDbClient: MongoClient,
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
            logger.info("Record inserted in '$name' with key: $it")
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

    private fun createFilter(filter: Map<String, List<*>>): JsonObject =
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
            .toJsonObject()

    // TODO Transform values with mapper
    private fun createUpdate (update: Map<String, *>): JsonObject =
        update
            .filterEmpty()
            .map { "\$set" to it }
            .toMap()
            .toJsonObject()

    // TODO Remove '_id', add "_id" to 0
    private fun createProjection (fields: List<String>): JsonObject =
        if(fields.isEmpty ()) JsonObject()
        else
            fields
                .filter { fields.contains(it) }
                .map { it to 1 }
                .toMap()
                .toJsonObject()

    // TODO Make recursive
    private fun remap(json: JsonObject): Map<String, *> =
        json.map.mapValues {
            val value = it.value
            when (value) {
                is JsonObject -> value.map
                is JsonArray -> value.list
                else -> value
            }
        }

    private fun createSort(fields : Map<String, Boolean>): JsonObject =
        fields
            .filter { fields.contains (it.key) }
            .mapValues { if (it.value) -1 else 1 }
            .toJsonObject()

    private fun executeBulkOperation(instances: List<T>, operation: (T) -> BulkOperation):
        Future<MongoClientBulkWriteResult> {

        val map = instances.map(operation)
        val future = Future.future<MongoClientBulkWriteResult>()
        mongoDbClient.bulkWrite(name, map, future)
        return future
    }
}
