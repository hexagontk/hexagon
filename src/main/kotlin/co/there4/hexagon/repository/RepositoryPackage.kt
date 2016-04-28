package co.there4.hexagon.repository

import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.bson.Document

fun mongoDatabase (uri: String = "mongodb://localhost", database: String): MongoDatabase =
    MongoClient(MongoClientURI(uri)).getDatabase(database) ?:
        error ("Error connecting to MongoDB at: $uri")

fun mongoCollection (
    name: String, database: MongoDatabase) : MongoCollection<Document> =
        database.getCollection(name) ?: error ("Error getting '$name' collection")
