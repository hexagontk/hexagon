package co.there4.hexagon.repository

import co.there4.hexagon.events.EventManager
import co.there4.hexagon.repository.RepositoryEventAction.*
import co.there4.hexagon.serialization.convertToMap
import co.there4.hexagon.serialization.convertToObject
import co.there4.hexagon.util.CompanionLogger
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.MongoIterable
import com.mongodb.client.model.*
import com.mongodb.client.result.UpdateResult
import org.bson.Document
import org.bson.conversions.Bson
import kotlin.reflect.KClass

open class MongoRepository <T : Any> (
    val type: KClass<T>,
    collection: MongoCollection<Document>,
    protected val publishEvents: Boolean = false) :
    MongoCollection<Document> by collection {

    companion object : CompanionLogger (MongoRepository::class)

    constructor (type: KClass<T>, database: MongoDatabase, publishEvents: Boolean = false) :
        this (
            type,
            mongoCollection(type.simpleName ?: throw IllegalArgumentException (), database),
            publishEvents
        )


    protected fun publish (source: T, action: RepositoryEventAction) {
        if (publishEvents)
            EventManager.publish(RepositoryEvent (source, action))
    }

    protected fun publish (sources: List<T>, action: RepositoryEventAction) {
        if (publishEvents)
            sources.forEach { publish(it, action) }
    }

    /**
     * TODO Publish error event (try-catch with throw)
     */
    fun insertOneObject (document: T) {
        insertOne (map (document))
        publish(document, INSERTED)
    }

    fun insertOneObject (document: T, options: InsertOneOptions) {
        insertOne (map (document), options)
        publish(document, INSERTED)
    }

    fun insertManyObjects (documents: List<T>) {
        insertMany (map (documents))
        publish(documents, INSERTED)
    }

    fun insertManyObjects (documents: List<T>, options: InsertManyOptions) {
        insertMany (map (documents), options)
        publish(documents, INSERTED)
    }

    fun replaceOneObject (filter: Bson, replacement: T): UpdateResult {
        val result = replaceOne (filter, map (replacement))
        publish(replacement, REPLACED)
        return result
    }

    fun replaceOneObject (filter: Bson, replacement: T, options: UpdateOptions): UpdateResult {
        val result = replaceOne (filter, map (replacement), options)
        publish(replacement, REPLACED)
        return result
    }

    fun findOneObjectAndReplace (filter: Bson, replacement: T): T {
        val result = unmap (findOneAndReplace (filter, map (replacement)))
        publish(replacement, REPLACED)
        return result
    }

    fun findOneObjectAndReplace (
        filter: Bson, replacement: T, options: FindOneAndReplaceOptions): T {

        val result = unmap (findOneAndReplace (filter, map (replacement), options))
        publish(replacement, REPLACED)
        return result
    }

    fun findObjects (): MongoIterable<T> = find ().map { unmap(it) }

    fun findObjects (filter: Bson): MongoIterable<T> = find (filter).map { unmap(it) }

    fun createIndex(
        name: String,
        order: Int = 1,
        options: IndexOptions = IndexOptions().background(true)): String =
            createIndex(Document(name, order), options)

    private fun map (document: T): Document {
        return Document (document.convertToMap ().mapKeys {
            val key = it.key ?: throw IllegalStateException ("Key can not be 'null'")
            if (key is String)
                key
            else
                throw IllegalStateException ("Key must be 'String' not '${key.javaClass.name}'")
        })
    }

    private fun map (documents: List<T>): List<Document> = documents.map { map(it) }

    private fun unmap (document: Document): T = document.convertToObject (type)
}
