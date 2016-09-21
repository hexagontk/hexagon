package co.there4.hexagon.repository

import co.there4.hexagon.events.EventManager
import co.there4.hexagon.repository.RepositoryEventAction.DELETED
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Indexes.*
import com.mongodb.client.model.UpdateOptions
import kotlinx.html.dom.document
import org.bson.Document
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.jvm.javaType

open class MongoIdRepository<T : Any, K : Any> (
    type: KClass<T>,
    collection: MongoCollection<Document>,
    protected val key: KProperty1<T, K>,
    publishEvents: Boolean = false,
    indexOrder: Int? = 1,
    onStore: (Document) -> Document = { it },
    onLoad: (Document) -> Document = { it }) :
        MongoRepository<T> (type, collection, publishEvents, onStore, onLoad) {

    @Suppress("UNCHECKED_CAST")
    val keyType: KClass<K> = key.returnType.kclass() as KClass<K>

    constructor (
        type: KClass<T>,
        database: MongoDatabase,
        key: KProperty1<T, K>,
        publishEvents: Boolean = false,
        indexOrder: Int? = 1,
        onStore: (Document) -> Document = { it },
        onLoad: (Document) -> Document = { it }) :
            this (
                type,
                mongoCollection(type.simpleName ?: error("Error getting type name"), database),
                key,
                publishEvents,
                indexOrder,
                onStore,
                onLoad
            )

    constructor (
        type: KClass<T>,
        key: KProperty1<T, K>,
        publishEvents: Boolean = false,
        indexOrder: Int? = 1,
        onStore: (Document) -> Document = { it },
        onLoad: (Document) -> Document = { it }) :
            this (
                type,
                mongoDatabase(),
                key,
                publishEvents,
                indexOrder,
                onStore,
                onLoad
            )

    init {
        if (indexOrder != null)
            createIndex (if(indexOrder == 1) ascending(key.name) else descending(key.name), true)
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
        deleteOne (convertKeyName(key.name) eq convertId(documentId))
        publishKey(documentId, DELETED)
    }

    fun deleteIds (vararg documentId: K) {
        deleteIds (documentId.toList ())
    }

    fun deleteIds (documentIds: List<K>) {
        deleteMany (convertKeyName(key.name) isIn documentIds.map { convertId(it) })
        publishKey(documentIds, DELETED)
    }

    fun deleteObject (documentId: T) {
        deleteOne (convertKeyName(key.name) eq convertId((key.getter)(documentId)))
        publish(documentId, DELETED)
    }

    fun deleteObjects (vararg documentId: T) {
        deleteObjects (documentId.toList())
    }

    fun deleteObjects (documentIds: List<T>) {
        val ids = documentIds.map { convertId((key.getter)(it)) }
        deleteMany (convertKeyName(key.name) isIn ids)
        publish(documentIds, DELETED)
    }

    fun replaceObject (document: T, upsert: Boolean = false) =
        replaceOneObject (
            convertKeyName(key.name) eq convertId((key.getter)(document)),
            document,
            if (upsert) UpdateOptions().upsert(true) else UpdateOptions()
        )

    fun replaceObjects (vararg document: T, upsert: Boolean = false) {
        replaceObjects (document.toList(), upsert)
    }

    fun replaceObjects (document: List<T>, upsert: Boolean = false) {
        document.forEach { replaceObject(it, upsert) }
    }

    fun find (vararg documentId: K): List<T> = find (documentId.toList ())

    fun find (documentId: List<K>): List<T> =
        findObjects (convertKeyName(key.name) isIn documentId.map { convertId(it) }).toList()

    fun find (documentId: K): T? =
        findObjects (convertKeyName(key.name) eq convertId(documentId)).first ()

    fun isEmpty() = count() == 0L

    fun getKey (obj: T): K = (key.getter)(obj)

    private fun KType.kclass(): KClass<*> = when (this.javaType.typeName) {
        "boolean" -> Boolean::class
        "int" -> Int::class
        "long" -> Long::class
        "short" -> Short::class
        "double" -> Double::class
        "float" -> Float::class
        else -> Class.forName(this.javaType.typeName).kotlin
    }
}
