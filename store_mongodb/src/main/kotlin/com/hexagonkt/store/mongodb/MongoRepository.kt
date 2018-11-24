package com.hexagonkt.store.mongodb

import com.hexagonkt.helpers.Logger
import com.hexagonkt.helpers.Resource
import com.hexagonkt.helpers.error
import com.hexagonkt.serialization.convertToMap
import com.hexagonkt.serialization.convertToObject
import com.hexagonkt.serialization.parseList
import com.hexagonkt.serialization.SerializationManager.formatOf
import com.mongodb.client.*
import com.mongodb.client.model.*
import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.UpdateResult
import org.bson.Document
import org.bson.conversions.Bson
import java.io.File
import kotlin.reflect.KClass

@Suppress("MemberVisibilityCanBePrivate") // This class has public methods for library clients use
open class MongoRepository <T : Any> (
    private val type: KClass<T>,
    collection: MongoCollection<Document>,
    protected val onStore: (Document) -> Document = { it },
    protected val onLoad: (Document) -> Document = { it }
) : MongoCollection<Document> by collection {

    private val log: Logger = Logger(MongoRepository::class)

    constructor (type: KClass<T>, database: MongoDatabase) :
        this(type, database.getCollection(type.simpleName ?: error("Error getting type name")))

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

    fun replaceOneObject (filter: Bson, replacement: T): UpdateResult =
        replaceOne (filter, map (replacement))

    fun replaceOneObject (filter: Bson, replacement: T, options: ReplaceOptions): UpdateResult =
        replaceOne (filter, map (replacement), options)

    fun findOneObjectAndReplace (filter: Bson, replacement: T): T =
        unmap (findOneAndReplace (filter, map (replacement)) ?: error)

    fun findOneObjectAndReplace (
        filter: Bson, replacement: T, options: FindOneAndReplaceOptions): T =
            unmap (findOneAndReplace (filter, map (replacement), options) ?: error)

    fun findObjects (setup: FindIterable<*>.() -> Unit = {}) = fo(null, setup)
    fun findObjects (filter: Bson, setup: FindIterable<*>.() -> Unit = {}) = fo(filter, setup)

    private fun fo (filter: Bson?, setup: FindIterable<*>.() -> Unit = {}): MongoIterable<T> =
        (if (filter == null) find() else find (filter)).let { findIterable ->
            findIterable.setup()
            findIterable.map { unmap(it) }
        }

    fun importFile(input: File) { insertManyObjects(input.parseList(type)) }
    fun importResource(input: String) { insertManyObjects(Resource(input).requireUrl().parseList(type)) }

    fun delete(): DeleteResult = deleteMany(Document())

    /**
     * Load a file with DB data serialized to a store.
     * @param file .
     */
    fun loadData(file: String) {
        val resourceAsStream = Resource(file).stream()
        val extension = file.substringAfterLast('.')
        val objects = resourceAsStream?.parseList(
            type, formatOf("application/$extension")) ?: listOf()
        objects.forEach {
            try {
                this.insertOneObject(it)
            }
            catch (e: Exception) {
                log.warn { "$it already inserted" }
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
