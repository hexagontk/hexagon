package co.there4.hexagon.repository

import co.there4.hexagon.events.EventManager
import co.there4.hexagon.repository.RepositoryEventAction.DELETED
import com.mongodb.client.model.Filters.*
import com.mongodb.client.model.Filters.`in` as _in
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.IndexOptions
import org.bson.Document
import kotlin.reflect.KClass

class MongoIdRepository<T : Any, K : Any> (
    type: KClass<T>,
    collection: MongoCollection<Document>,
    private val keyName: String,
    val keyType: KClass<K>,
    private val keySupplier: (T) -> K,
    publishEvents: Boolean = false,
    indexOrder: Int = 1) :
    MongoRepository<T> (type, collection, publishEvents) {

    constructor (
        type: KClass<T>,
        database: MongoDatabase,
        keyName: String,
        keyType: KClass<K>,
        keySupplier: (T) -> K,
        publishEvents: Boolean = false,
        indexOrder: Int = 1) :
        this (
            type,
            mongoCollection(type.simpleName ?: throw IllegalArgumentException (), database),
            keyName,
            keyType,
            keySupplier,
            publishEvents,
            indexOrder
        )

    init {
        val indexOptions = IndexOptions ().unique (true).background (true)
        createIndex (Document (keyName, indexOrder), indexOptions)
    }

    protected fun publishKey (source: K, action: RepositoryEventAction) {
        if (publishEvents)
            EventManager.publish(RepositoryIdEvent (type, source, action))
    }

    protected fun publishKey (sources: List<K>, action: RepositoryEventAction) {
        if (publishEvents)
            sources.forEach { publishKey(it, action) }
    }

    fun deleteId (documentId: K) {
        deleteOne (eq (keyName, documentId))
        publishKey(documentId, DELETED)
    }

    fun deleteIds (vararg documentId: K) {
        deleteIds (documentId.toList ())
    }

    fun deleteIds (documentIds: List<K>) {
        deleteMany (_in (keyName, documentIds))
        publishKey(documentIds, DELETED)
    }

    fun deleteObject (documentId: T) {
        deleteOne (eq (keyName, keySupplier(documentId)))
        publish(documentId, DELETED)
    }

    fun deleteObjects (vararg documentId: T) {
        deleteObjects (documentId.toList())
    }

    fun deleteObjects (documentIds: List<T>) {
        val ids = documentIds.map { keySupplier(it) }
        deleteMany (_in (keyName, ids))
        publish(documentIds, DELETED)
    }

    fun replaceObject (document: T) {
        replaceOneObject (eq (keyName, keySupplier (document)), document)
    }

    fun replaceObjects (vararg document: T) {
        replaceObjects (document.toList())
    }

    fun replaceObjects (document: List<T>) {
        document.forEach { replaceObject(it) }
    }

    fun find (vararg documentId: K) = find (documentId.toList ())

    fun find (documentId: List<K>) =
        findObjects (_in (keyName, documentId)).toList()

    fun find (documentId: K) = findObjects (eq (keyName, documentId)).first ()

    fun getKey (obj: T): K = keySupplier(obj)
}
