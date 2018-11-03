package com.hexagonkt.store.mongodb

import com.hexagonkt.helpers.Logger
import com.hexagonkt.helpers.logger
import com.hexagonkt.serialization.convertToMap
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.FindOneAndReplaceOptions
import com.mongodb.client.model.InsertManyOptions
import com.mongodb.client.model.InsertOneOptions
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.result.DeleteResult
import java.lang.System.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

/**
 * TODO Check events
 */
abstract class RepositoryTest<T : Any, out K : Any> (
    type: KClass<T>, private val key: KProperty1<T, K>) {

    private val log: Logger = logger()

    private val collection: MongoRepository<T> = createCollection(type)

    private fun <T : Any> createCollection (type: KClass<T>): MongoRepository<T> {
        val repository = MongoRepository(type, mongoDatabase())
        setupCollection(repository)
        return repository
    }

    protected open fun <T : Any> setupCollection (collection: MongoRepository<T>) {}

    protected abstract val testObjects: List<T>
    protected abstract fun setObjectKey (obj: T, id: Int): T
    protected abstract fun getObjectKey (obj: T): K
    protected abstract fun createObject(): T
    protected abstract fun changeObject(obj: T): T

    protected open fun createObjects() = (0..9).map { setObjectKey (createObject(), it) }

    private fun deleteAll (): DeleteResult = collection.delete ()

    @Suppress("unused")
    fun one_object_is_stored_and_loaded_without_error() {
        testObjects.forEach {
            deleteAll()
            collection.insertOneObject(it)
            var result: T = collection.findObjects().first()

            assert(result == it)

            deleteAll()
            val object2 = changeObject(it)
            collection.insertOneObject(object2, InsertOneOptions())
            assert(collection.count() == 1L)
            result = collection.findObjects().first()
            assert(result == object2)

            deleteAll()
            collection.insertOne(it.convertToMap().mapKeys { entry -> entry.key as String })
            result = collection.findObjects().first()

            assert(result == it)

            deleteAll()
        }
    }

    @Suppress("unused")
    fun many_objects_are_stored_and_loaded_without_error() {
        testObjects.forEach { _ ->
            deleteAll()
            val objects = createObjects ()

            collection.insertManyObjects(objects)

            assert(collection.count() == 10L)
            val firstObject = objects[0]
            val obj = collection.findObjects(eq<K>(key.name, getObjectKey(firstObject))).first()
            assert(obj == firstObject)

            deleteAll()

            collection.insertManyObjects(objects, InsertManyOptions().ordered(false))
            assert(collection.count() == 10L)

            deleteAll()
        }
    }

    @Suppress("unused")
    fun replace_object_stores_modified_data_in_db() {
        testObjects.forEach {
            deleteAll()
            val entity = createObject()
            val replacement = changeObject(entity)
            val query = eq<K>(key.name, getObjectKey(entity))

            collection.insertOneObject(entity)
            var result = collection.replaceOneObject(query, replacement)
            assert(result.matchedCount == 1L)

            assert(collection.findObjects(query).first() == replacement)

            result = collection.replaceOneObject(query, entity, UpdateOptions().upsert(false))
            assert(result.matchedCount == 1L)

            assert(collection.findObjects(query).first() == entity)

            deleteAll()
        }
    }

    @Suppress("unused")
    fun find_and_replace_object_stores_modified_data_in_db() {
        testObjects.forEach {
            deleteAll()
            var t = nanoTime()
            val entity = createObject()
            val replacement = changeObject(entity)
            val query = eq<K>(key.name, getObjectKey(entity))
            log.trace { "Test setup: " + (nanoTime() - t) }

            t = nanoTime()
            collection.insertOneObject(entity)
            log.trace { "Insert: " + (nanoTime() - t) }
            t = nanoTime()
            var result = collection.findOneObjectAndReplace(query, replacement)
            log.trace { "Find and replace: " + (nanoTime() - t) }

            assert(entity == result)

            t = nanoTime()
            val options = FindOneAndReplaceOptions().upsert(false)
            result = collection.findOneObjectAndReplace(query, entity, options)
            log.trace { "Find and replace (params): " + (nanoTime() - t) }
            t = nanoTime()

            assert(replacement == result)

            deleteAll()
            log.trace { "Delete all records: " + (nanoTime() - t) }
        }
    }
}
