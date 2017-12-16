package com.hexagonkt.store

import com.hexagonkt.settings.SettingsManager.settings
import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Indexes.*
import org.bson.Document
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import kotlin.reflect.KProperty1

val mongodbUrl = settings["mongodbUrl"] as? String? ?: "mongodb://localhost/test"

fun mongoDatabase (uri: String = mongodbUrl): MongoDatabase =
    MongoClient(MongoClientURI(uri)).getDatabase(MongoClientURI(uri).database) ?:
        error ("Error connecting to MongoDB at: $uri")

fun mongoCollection (
    name: String, database: MongoDatabase = mongoDatabase()) : MongoCollection<Document> =
        database.getCollection(name) ?: error ("Error getting '$name' collection")

fun mongoId(): String = ObjectId().toHexString()

inline fun <reified T : Any> mongoRepository(database: MongoDatabase = mongoDatabase()) =
    MongoRepository(
        T::class,
        mongoCollection(T::class.simpleName ?: error("Error fetching class name"), database)
    )

inline fun <reified T : Any> mongoRepository(
    database: MongoDatabase = mongoDatabase(),
    setup: MongoRepository<T>.() -> Unit): MongoRepository<T> =
        mongoRepository<T>(database).let {
            it.setup()
            it
        }

inline fun <reified T : Any, reified K : Any> mongoIdRepository(
    key: KProperty1<T, K>,
    database: MongoDatabase = mongoDatabase(),
    indexOrder: Int? = 1) =
        MongoIdRepository (
            T::class,
            mongoCollection(T::class.simpleName ?: error("Error getting type name"), database),
            key,
            indexOrder
        )

inline fun <reified T : Any, reified K : Any> mongoIdRepository(
    key: KProperty1<T, K>,
    database: MongoDatabase = mongoDatabase(),
    indexOrder: Int? = 1,
    setup: MongoIdRepository<T, K>.() -> Unit) =
        mongoIdRepository (key, database, indexOrder).let {
            it.setup()
            it
        }

// TODO Check that parameter is simple type... Ie: fails with LocalDate
infix fun <T> String.eq(value: T): Bson = Filters.eq(this, value)
infix fun <T> String.isIn(value: Collection<T>): Bson = Filters.`in`(this, value)

infix fun <T> String.gte(value: T): Bson = Filters.gte(this, value)
infix fun <T> String.gt(value: T): Bson = Filters.gt(this, value)
infix fun <T> String.lte(value: T): Bson = Filters.lte(this, value)
infix fun <T> String.lt(value: T): Bson = Filters.lt(this, value)

infix fun <T> KProperty1<*, *>.eq(value: T): Bson = this.name eq value
infix fun <T> KProperty1<*, *>.isIn(value: Collection<T>): Bson = this.name isIn value

infix fun <T> KProperty1<*, *>.gte(value: T): Bson = this.name gte value
infix fun <T> KProperty1<*, *>.gt(value: T): Bson = this.name gt value
infix fun <T> KProperty1<*, *>.lte(value: T): Bson = this.name lte value
infix fun <T> KProperty1<*, *>.lt(value: T): Bson = this.name lt value

fun ascending(vararg fields: KProperty1<*, *>): Bson = ascending(fields.map { it.name })
fun descending(vararg fields: KProperty1<*, *>): Bson = descending(fields.map { it.name })
