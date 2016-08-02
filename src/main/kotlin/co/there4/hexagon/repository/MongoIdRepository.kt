package co.there4.hexagon.repository

import co.there4.hexagon.events.EventManager
import co.there4.hexagon.repository.RepositoryEventAction.DELETED
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.IndexOptions
import org.bson.Document
import kotlin.reflect.KClass

open class MongoIdRepository<T : Any, K : Any> (
    type: KClass<T>,
    collection: MongoCollection<Document>,
    private val keySupplier: (T) -> K,
    val keyType: KClass<K>,
    protected val keyName: String = "id",
    publishEvents: Boolean = false,
    indexOrder: Int = 1,
    createIndex: Boolean = true,
    onStore: (Document) -> Document = { it },
    onLoad: (Document) -> Document = { it }) :
        MongoRepository<T> (type, collection, publishEvents, onStore, onLoad) {

    constructor (
        type: KClass<T>,
        database: MongoDatabase,
        keySupplier: (T) -> K,
        keyType: KClass<K>,
        keyName: String = "id",
        publishEvents: Boolean = false,
        indexOrder: Int = 1,
        createIndex: Boolean = true,
        onStore: (Document) -> Document = { it },
        onLoad: (Document) -> Document = { it }) :
        this (
            type,
            mongoCollection(type.simpleName ?: error("Error getting type name"), database),
            keySupplier,
            keyType,
            keyName,
            publishEvents,
            indexOrder,
            createIndex,
            onStore,
            onLoad
        )

    constructor (
        type: KClass<T>,
        keySupplier: (T) -> K,
        keyType: KClass<K>,
        keyName: String = "id",
        publishEvents: Boolean = false,
        indexOrder: Int = 1,
        createIndex: Boolean = true,
        onStore: (Document) -> Document = { it },
        onLoad: (Document) -> Document = { it }
        ) :
        this (
            type,
            mongoDatabase(),
            keySupplier,
            keyType,
            keyName,
            publishEvents,
            indexOrder,
            createIndex,
            onStore,
            onLoad
        )

    init {
        if (createIndex)
            createIndex (keyName, indexOrder, IndexOptions().unique(true).background(true))
    }

    protected fun publishKey (source: K, action: RepositoryEventAction) {
        if (publishEvents)
            EventManager.publish(RepositoryIdEvent (type, source, action))
    }

    protected fun publishKey (sources: List<K>, action: RepositoryEventAction) {
        if (publishEvents)
            sources.forEach { publishKey(it, action) }
    }

    protected open fun convertKeyName(keyName: String): String = keyName
    protected open fun convertId(id: K): Any = id

    fun deleteId (documentId: K) {
        deleteOne (convertKeyName(keyName) eq convertId(documentId))
        publishKey(documentId, DELETED)
    }

    fun deleteIds (vararg documentId: K) {
        deleteIds (documentId.toList ())
    }

    fun deleteIds (documentIds: List<K>) {
        deleteMany (convertKeyName(keyName) isIn documentIds.map { convertId(it) })
        publishKey(documentIds, DELETED)
    }

    fun deleteObject (documentId: T) {
        deleteOne (convertKeyName(keyName) eq convertId(keySupplier(documentId)))
        publish(documentId, DELETED)
    }

    fun deleteObjects (vararg documentId: T) {
        deleteObjects (documentId.toList())
    }

    fun deleteObjects (documentIds: List<T>) {
        val ids = documentIds.map { convertId(keySupplier(it)) }
        deleteMany (convertKeyName(keyName) isIn ids)
        publish(documentIds, DELETED)
    }

    fun replaceObject (document: T) =
        replaceOneObject (convertKeyName(keyName) eq convertId(keySupplier (document)), document)

    fun replaceObjects (vararg document: T) {
        replaceObjects (document.toList())
    }

    fun replaceObjects (document: List<T>) {
        document.forEach { replaceObject(it) }
    }

    fun find (vararg documentId: K) = find (documentId.toList ())

    fun find (documentId: List<K>) =
        findObjects (convertKeyName(keyName) isIn documentId.map { convertId(it) }).toList()

    fun find (documentId: K) = findObjects (convertKeyName(keyName) eq convertId(documentId)).first ()

    fun getKey (obj: T): K = keySupplier(obj)
}
