package com.hexagonkt.store.mongodb

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.*
import com.mongodb.client.model.Indexes.ascending
import com.mongodb.client.model.Indexes.descending
import org.bson.Document
import org.bson.conversions.Bson
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

open class MongoIdRepository<T : Any, K : Any>(
    type: KClass<T>,
    collection: MongoCollection<Document>,
    private val key: KProperty1<T, K>,
    indexOrder: Int? = 1,
    onStore: (Document) -> Document = { it },
    onLoad: (Document) -> Document = { it }) :
        MongoRepository<T>(type, collection, onStore, onLoad) {

    constructor(
        type: KClass<T>,
        database: MongoDatabase,
        key: KProperty1<T, K>,
        indexOrder: Int? = 1,
        onStore: (Document) -> Document = { it },
        onLoad: (Document) -> Document = { it }) :
            this(
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

    private fun createIndex(
        keys: Bson, unique: Boolean = false, background: Boolean = true): String =
            createIndex(keys, IndexOptions().unique(unique).background(background))

    fun getKey(obj: T): K = (key.getter)(obj)
}
