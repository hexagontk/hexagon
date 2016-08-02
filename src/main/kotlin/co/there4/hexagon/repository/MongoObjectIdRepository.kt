package co.there4.hexagon.repository

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.bson.Document
import org.bson.types.ObjectId
import kotlin.reflect.KClass

class MongoObjectIdRepository<T : Any>(
    type: KClass<T>,
    collection: MongoCollection<Document>,
    keySupplier: (T) -> String,
    keyName: String = "id",
    publishEvents: Boolean = false,
    onStore: (Document) -> Document = { it },
    onLoad: (Document) -> Document = { it }) :
    MongoIdRepository<T, String> (
        type,
        collection,
        keySupplier,
        String::class,
        keyName,
        publishEvents,
        1,
        false,
        onStore,
        onLoad) {

    constructor (
        type: KClass<T>,
        database: MongoDatabase,
        keySupplier: (T) -> String,
        keyName: String = "id",
        publishEvents: Boolean = false,
        onStore: (Document) -> Document = { it },
        onLoad: (Document) -> Document = { it }) :
        this (
            type,
            mongoCollection(type.simpleName ?: error("Error getting type name"), database),
            keySupplier,
            keyName,
            publishEvents,
            onStore,
            onLoad
        )

    constructor (
        type: KClass<T>,
        keySupplier: (T) -> String,
        keyName: String = "id",
        publishEvents: Boolean = false,
        onStore: (Document) -> Document = { it },
        onLoad: (Document) -> Document = { it }) :
        this (
            type,
            mongoDatabase(),
            keySupplier,
            keyName,
            publishEvents,
            onStore,
            onLoad
        )

    override fun map (document: T): Document {
        val doc = super.map(document)
        doc["_id"] = ObjectId(doc[keyName].toString())
        doc.remove(keyName)
        return doc
    }

    override fun unmap (document: Document): T {
        document[keyName] = (document["_id"] as ObjectId).toHexString()
        document.remove("_id")
        return super.unmap(document)
    }

    override fun convertId(id: String): Any = ObjectId(id)
    override fun convertKeyName(keyName: String): String = "_id"
}
