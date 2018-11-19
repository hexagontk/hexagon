package com.hexagonkt.store.mongodb

import com.hexagonkt.helpers.error
import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import org.bson.Document
import org.bson.conversions.Bson
import kotlin.reflect.KProperty1

fun mongoDatabase (uri: String): MongoDatabase =
    MongoClient(MongoClientURI(uri)).getDatabase(MongoClientURI(uri).database ?: error) ?:
        error ("Error connecting to MongoDB at: $uri")

fun mongoCollection (
    name: String, database: MongoDatabase) : MongoCollection<Document> =
        database.getCollection(name) ?: error ("Error getting '$name' collection")

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
