package com.hexagonkt.store

import com.hexagonkt.serialization.convertToMap
import com.hexagonkt.serialization.convertToObject
import com.hexagonkt.serialization.parseList
import com.hexagonkt.helpers.CachedLogger
import com.hexagonkt.helpers.requireResource
import com.hexagonkt.helpers.resourceAsStream
import com.hexagonkt.serialization.SerializationManager.getContentTypeFormat
import com.mongodb.client.*
import com.mongodb.client.model.*
import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.UpdateResult
import org.bson.Document
import org.bson.conversions.Bson
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

open class MongoRepository <T : Any> (
    val type: KClass<T>,
    collection: MongoCollection<Document>,
    protected val onStore: (Document) -> Document = { it },
    protected val onLoad: (Document) -> Document = { it }) :
        MongoCollection<Document> by collection {

    companion object : CachedLogger(MongoRepository::class)

    constructor (type: KClass<T>, database: MongoDatabase = mongoDatabase()) :
        this(type, mongoCollection(type.simpleName ?: error("Error getting type name"), database))

    // TODO Apply to the other MongoCollection's methods to support maps directly
    fun insertOne (document: Map<String, *>) { insertOne(Document(document)) }

    /**
     * TODO Publish error event (try-catch with throw)
     */
    fun insertOneObject (document: T) {
        insertOne (map (document))
    }

    fun insertOneObject (document: T, options: InsertOneOptions) {
        insertOne (map (document), options)
    }

    fun insertManyObjects (documents: List<T>) {
        insertMany (map (documents))
    }

    fun insertManyObjects (documents: List<T>, options: InsertManyOptions) {
        insertMany (map (documents), options)
    }

    fun replaceOneObject (filter: Bson, replacement: T): UpdateResult {
        val result = replaceOne (filter, map (replacement))
        return result
    }

    fun replaceOneObject (filter: Bson, replacement: T, options: UpdateOptions): UpdateResult {
        val result = replaceOne (filter, map (replacement), options)
        return result
    }

    fun findOneObjectAndReplace (filter: Bson, replacement: T): T {
        val result = unmap (findOneAndReplace (filter, map (replacement)))
        return result
    }

    fun findOneObjectAndReplace (
        filter: Bson, replacement: T, options: FindOneAndReplaceOptions): T {

        val result = unmap (findOneAndReplace (filter, map (replacement), options))
        return result
    }

    fun findObjects (setup: FindIterable<*>.() -> Unit = {}) = fo(null, setup)
    fun findObjects (filter: Bson, setup: FindIterable<*>.() -> Unit = {}) = fo(filter, setup)

    private fun fo (filter: Bson?, setup: FindIterable<*>.() -> Unit = {}): MongoIterable<T> =
        (if (filter == null) find() else find (filter)).let {
            it.setup()
            it.map { unmap(it) }
        }

    fun findOneObject (filter: Bson, setup: FindIterable<*>.() -> Unit = {}): T? =
        findObjects(filter, setup).firstOrNull()

    fun exists (filter: Bson): Boolean = findOneObject(filter) != null

    fun createIndex(keys: Bson, unique: Boolean = false, background: Boolean = true): String =
        createIndex(keys, IndexOptions().unique(unique).background(background))

    fun createUniqueIndex(keys: Bson, background: Boolean = true): String =
        createIndex(keys, true, background)

    fun createIndex(vararg fields: KProperty1<*, *>): String = createIndex(ascending(*fields))
    fun createUniqueIndex(vararg fields: KProperty1<*, *>) = createUniqueIndex(ascending(*fields))

    fun aggregate(vararg bson: Map<String, *>): AggregateIterable<Document>? =
        aggregate(bson.map(::Document))

    fun importFile(input: File) { insertManyObjects(input.parseList(type)) }
    fun importResource(input: String) { insertManyObjects(requireResource(input).parseList(type)) }

    fun delete(): DeleteResult = deleteMany(Document())

    /**
     * Load a file with DB data serialized to a store.
     * @param file .
     */
    fun loadData(file: String) {
        val resourceAsStream = resourceAsStream(file)
        val extension = file.substringAfterLast('.')
        val objects = resourceAsStream?.parseList(
            type, getContentTypeFormat("application/$extension")) ?: listOf()
        objects.forEach {
            try {
                this.insertOneObject(it)
            }
            catch (e: Exception) {
                warn("$it already inserted")
            }
        }
    }

    protected open fun map (document: T): Document {
        return onStore (
            Document (document.convertToMap ().mapKeys {
                val key = it.key ?: error("Key can not be 'null'")
                key as? String ?: error("Key must be 'String' not '${key.javaClass.name}'")
            })
        )
    }

    protected open fun map (documents: List<T>): List<Document> = documents.map(this::map)

    protected open fun unmap (document: Document): T = onLoad(document).convertToObject(type)
}
