package com.hexagonkt.store.mongodb

import com.hexagonkt.helpers.error
import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import org.bson.Document
import org.bson.conversions.Bson

fun mongoDatabase (uri: String): MongoDatabase =
    MongoClient(MongoClientURI(uri)).getDatabase(MongoClientURI(uri).database ?: error) ?:
        error ("Error connecting to MongoDB at: $uri")

fun mongoCollection (
    name: String, database: MongoDatabase) : MongoCollection<Document> =
        database.getCollection(name) ?: error ("Error getting '$name' collection")

// TODO Check that parameter is simple type... Ie: fails with LocalDate
infix fun <T> String.eq(value: T): Bson = Filters.eq(this, value)
infix fun <T> String.isIn(value: Collection<T>): Bson = Filters.`in`(this, value)
