package co.there4.hexagon.repository

import com.mongodb.client.model.Filters.*
import com.mongodb.client.model.Filters.`in` as _in
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.IndexOptions
import org.bson.Document
import kotlin.reflect.KClass

class MongoIdRepository<T : Any, K : Any> (
    type: KClass<T>,
    collection: MongoCollection<Document>,
    val keyName: String,
    val keyType: KClass<K>,
    val keySupplier: (T) -> K,
    indexOrder: Int) :
    MongoRepository<T> (type, collection) {

    constructor (
        type: KClass<T>,
        collection: MongoCollection<Document>,
        keyName: String,
        keyType: KClass<K>,
        keySupplier: (T) -> K) :
            this (type, collection, keyName, keyType, keySupplier, 1)

    init {
        val indexOptions = IndexOptions ().unique (true).background (true)
        createIndex (Document (keyName, indexOrder), indexOptions)
    }

    fun deleteId (documentId: K) {
        deleteOne (eq (keyName, documentId))
    }

    fun deleteIds (vararg documentId: K) {
        deleteIds (documentId.toList ())
    }

    fun deleteIds (documentIds: List<K>) {
        deleteMany (_in (keyName, documentIds))
    }

    fun deleteObject (documentId: T) {
        deleteOne (eq (keyName, keySupplier(documentId)))
    }

    fun deleteObjects (vararg documentId: T) {
        deleteObjects (documentId.toList())
    }

    fun deleteObjects (documentIds: List<T>) {
        val ids = documentIds.map { keySupplier(it) }
        deleteMany (_in (keyName, ids))
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
