package com.hexagonkt.store.mongodb

import com.hexagonkt.helpers.Logger
import com.hexagonkt.serialization.convertToObject
import com.mongodb.client.*
import com.mongodb.client.result.DeleteResult
import org.bson.Document
import org.bson.conversions.Bson
import kotlin.reflect.KClass

open class MongoRepository <T : Any>(
    private val type: KClass<T>,
    collection: MongoCollection<Document>,
    protected val onStore: (Document) -> Document = { it },
    protected val onLoad: (Document) -> Document = { it }
) : MongoCollection<Document> by collection {

    private val log: Logger = Logger(MongoRepository::class)

    constructor(type: KClass<T>, database: MongoDatabase) :
        this(type, database.getCollection(type.simpleName ?: error("Error getting type name")))

    // TODO Apply to the other MongoCollection's methods to support maps directly
    fun insertOne(document: Map<String, *>) { insertOne(Document(document)) }

    fun findObjects(setup: FindIterable<*>.() -> Unit = {}) =
        fo(null, setup)

    fun findObjects(filter: Bson, setup: FindIterable<*>.() -> Unit = {}) =
        fo(filter, setup)

    private fun fo(filter: Bson?, setup: FindIterable<*>.() -> Unit = {}): MongoIterable<T> =
        (if (filter == null) find() else find(filter)).let { findIterable ->
            findIterable.setup()
            findIterable.map { unmap(it) }
        }

    fun delete(): DeleteResult = deleteMany(Document())

    protected open fun unmap(document: Document): T =
        onLoad(document).convertToObject(type)
}
