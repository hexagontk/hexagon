package co.there4.hexagon.repository

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.bson.Document
import org.bson.types.ObjectId
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class MongoObjectIdRepository<T : Any>(
    type: KClass<T>,
    collection: MongoCollection<Document>,
    key: KProperty1<T, String>,
    onStore: (Document) -> Document = { it },
    onLoad: (Document) -> Document = { it }) :
    MongoIdRepository<T, String> (
        type,
        collection,
        key,
        null,
        onStore,
        onLoad) {

    constructor (
        type: KClass<T>,
        database: MongoDatabase,
        key: KProperty1<T, String>,
        onStore: (Document) -> Document = { it },
        onLoad: (Document) -> Document = { it }) :
        this (
            type,
            mongoCollection(type.simpleName ?: error("Error getting type name"), database),
            key,
            onStore,
            onLoad
        )

    constructor (
        type: KClass<T>,
        key: KProperty1<T, String>,
        onStore: (Document) -> Document = { it },
        onLoad: (Document) -> Document = { it }) :
        this (
            type,
            mongoDatabase(),
            key,
            onStore,
            onLoad
        )

    override fun map (document: T): Document {
        val doc = super.map(document)
        doc["_id"] = ObjectId(doc[key.name].toString())
        doc.remove(key.name)
        return doc
    }

    override fun unmap (document: Document): T {
        document[key.name] = (document["_id"] as ObjectId).toHexString()
        document.remove("_id")
        return super.unmap(document)
    }

    override fun convertId(id: String): Any = ObjectId(id)
    override fun convertKeyName(keyName: String): String = "_id"
}
