package co.there4.hexagon.store

import com.mongodb.client.FindIterable
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.BulkWriteOptions
import com.mongodb.client.model.Indexes.ascending
import com.mongodb.client.model.Indexes.descending
import com.mongodb.client.model.ReplaceOneModel
import com.mongodb.client.model.UpdateOptions
import org.bson.Document
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.jvm.javaType

open class MongoIdRepository<T : Any, K : Any> (
    type: KClass<T>,
    collection: MongoCollection<Document>,
    val key: KProperty1<T, K>,
    indexOrder: Int? = 1,
    onStore: (Document) -> Document = { it },
    onLoad: (Document) -> Document = { it }) :
        MongoRepository<T> (type, collection, onStore, onLoad) {

    @Suppress("UNCHECKED_CAST")
    val keyType: KClass<K> = key.returnType.kclass() as KClass<K>

    constructor (
        type: KClass<T>,
        database: MongoDatabase,
        key: KProperty1<T, K>,
        indexOrder: Int? = 1,
        onStore: (Document) -> Document = { it },
        onLoad: (Document) -> Document = { it }) :
            this (
                type,
                mongoCollection(type.simpleName ?: error("Error getting type name"), database),
                key,
                indexOrder,
                onStore,
                onLoad
            )

    constructor (
        type: KClass<T>,
        key: KProperty1<T, K>,
        indexOrder: Int? = 1,
        onStore: (Document) -> Document = { it },
        onLoad: (Document) -> Document = { it }) :
            this (
                type,
                mongoDatabase(),
                key,
                indexOrder,
                onStore,
                onLoad
            )

    init {
        if (indexOrder != null)
            createIndex (if(indexOrder == 1) ascending(key.name) else descending(key.name), true)
    }

    protected open fun convertKeyName(keyName: String): String = keyName
    protected open fun convertId(id: K): Any = id

    fun deleteId (documentId: K) {
        deleteOne (convertKeyName(key.name) eq convertId(documentId))
    }

    fun deleteIds (vararg documentId: K) {
        deleteIds (documentId.toList ())
    }

    fun deleteIds (documentIds: List<K>) {
        deleteMany (convertKeyName(key.name) isIn documentIds.map { convertId(it) })
    }

    fun deleteObject (documentId: T) {
        deleteOne (convertKeyName(key.name) eq convertId((key.getter)(documentId)))
    }

    fun deleteObjects (vararg documentId: T) {
        deleteObjects (documentId.toList())
    }

    fun deleteObjects (documentIds: List<T>) {
        val ids = documentIds.map { convertId((key.getter)(it)) }
        deleteMany (convertKeyName(key.name) isIn ids)
    }

    fun replaceObject (document: T, upsert: Boolean = false) =
        replaceOneObject (
            convertKeyName(key.name) eq convertId((key.getter)(document)),
            document,
            if (upsert) UpdateOptions().upsert(true) else UpdateOptions()
        )

    fun replaceObjects (vararg document: T, upsert: Boolean = false, bulk: Boolean = false) {
        replaceObjects (document.toList(), upsert, bulk)
    }

    fun replaceObjects (document: List<T>, upsert: Boolean = false, bulk: Boolean = false) {
        if (bulk) {
            val keyName = convertKeyName(key.name)
            bulkWrite(
                document.map { ReplaceOneModel(keyName eq convertId((key.getter)(it)), map(it)) },
                BulkWriteOptions().ordered(false)
            )
        }
        else {
            document.forEach { replaceObject(it, upsert) }
        }
    }

    fun find (vararg documentId: K): List<T> = find (documentId.toList ())

    fun find (documentId: List<K>, setup: FindIterable<*>.() -> Unit = {}): List<T> =
        findObjects (convertKeyName(key.name) isIn documentId.map { convertId(it) }) {
            setup()
        }.toList()

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
