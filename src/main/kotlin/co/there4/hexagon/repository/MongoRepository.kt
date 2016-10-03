package co.there4.hexagon.repository

import co.there4.hexagon.events.EventManager
import co.there4.hexagon.repository.RepositoryEventAction.*
import co.there4.hexagon.serialization.*
import co.there4.hexagon.util.*
import com.mongodb.client.*
import com.mongodb.client.model.*
import com.mongodb.client.result.UpdateResult
import org.bson.Document
import org.bson.conversions.Bson
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

open class MongoRepository <T : Any> (
    val type: KClass<T>,
    collection: MongoCollection<Document>,
    protected val publishEvents: Boolean = false,
    protected val onStore: (Document) -> Document = { it },
    protected val onLoad: (Document) -> Document = { it }) :
        MongoCollection<Document> by collection {

    companion object : CompanionLogger (MongoRepository::class)

    constructor (
        type: KClass<T>,
        database: MongoDatabase = mongoDatabase(),
        publishEvents: Boolean = false) :
            this(
                type,
                mongoCollection(type.simpleName ?: error("Error getting type name"), database),
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

    // TODO Apply to the other MongoCollection's methods to support maps directly
    fun insertOne (document: Map<String, *>) { insertOne(Document(document)) }

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

    fun findObjects (filter: Bson, setup: FindIterable<*>.() -> Unit = {}): MongoIterable<T> =
        find (filter).let {
            it.setup()
            it.map { unmap(it) }
        }

    fun findOneObject (filter: Bson, setup: FindIterable<*>.() -> Unit = {}): T? =
        findObjects(filter, setup).firstOrNull()

    fun exists (filter: Bson): Boolean = findOneObject(filter) != null

    fun createIndex(keys: Bson, unique: Boolean = false, background: Boolean = true): String =
        createIndex(keys, IndexOptions().unique(unique).background(background))

    fun createUniqueIndex(keys: Bson, background: Boolean = true): String =
        createIndex(keys, true, background)

    fun createIndex(vararg fields: KProperty1<*, *>): String = createIndex(ascending(*fields))
    fun createUniqueIndex(vararg fields: KProperty1<*, *>) = createUniqueIndex(ascending(*fields))

    fun aggregate(vararg bson: Map<String, *>): AggregateIterable<Document>? =
        aggregate(bson.map(::Document))

    // TODO Test this!
    fun importFile(input: File) { insertManyObjects(input.parseList(type)) }
    fun importResource(input: String) { insertManyObjects(requireResource(input).parseList(type)) }

    /**
     * Load a file with DB data serialized to a repository.
     * @param file .
     */
    fun loadData(file: String) {
        val resourceAsStream = resourceAsStream(file)
        val extension = file.substringAfterLast('.')
        val objects = resourceAsStream?.parseList(type, "application/$extension") ?: listOf()
        objects.forEach {
            try {
                this.insertOneObject(it)
            }
            catch (e: Exception) {
                warn("$it already inserted")
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

    protected open fun map (documents: List<T>): List<Document> = documents.map { map(it) }

    protected open fun unmap (document: Document): T = onLoad(document).convertToObject(type)
}
