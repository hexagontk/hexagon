package com.hexagonkt.store.mongodb

import com.hexagonkt.helpers.Logger
import com.hexagonkt.serialization.convertToMap
import com.hexagonkt.settings.SettingsManager
import com.mongodb.client.model.*
import com.mongodb.client.result.DeleteResult
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

abstract class IdRepositoryTest<T : Any, K : Any>(
    type: KClass<T>, private val key: KProperty1<T, K>) {

    private val mongodbUrl =
        SettingsManager.settings["mongodbUrl"] as? String? ?: "mongodb://localhost/test"

    private val log: Logger = Logger(IdRepositoryTest::class)

    private val collection: MongoRepository<T> = createCollection(type)

    private val idCollection: MongoIdRepository<T, K> =
        MongoIdRepository(type, mongoDatabase(mongodbUrl), key)

    private fun getObjectKey(obj: T) = idCollection.getKey(obj)

    @Suppress("unused")
    fun performing_crud_operations_with_lists_of_objects_behaves_as_expected () {
        val objects = createObjects ()
        @Suppress("UNCHECKED_CAST") // It seems the only way to convert to generic array
        val objectsArray = createObjects ().toTypedArray<Any>() as Array<T>
        val changedObjects = objects.map { this.changeObject(it) }
        @Suppress("UNCHECKED_CAST") // It seems the only way to convert to generic array
        val changedObjectsArray = changedObjects.toTypedArray<Any>() as Array<T>
        val ids = objects.map { idCollection.getKey(it) }
        @Suppress("UNCHECKED_CAST") // It seems the only way to convert to generic array
        val idsArray: Array<K> = ids.toTypedArray<Any>() as Array<K>

        assert (ids.all { it.javaClass == idCollection.keyType.java })

        idCollection.insertManyObjects(objects)
        assert(ids.map { idCollection.find(it) } == objects)
        assert(idCollection.find(*idsArray) == objects)
        ids.forEach { idCollection.deleteId(it) }
        assert(idCollection.find(*idsArray).isEmpty())

        idCollection.insertManyObjects(objects)
        assert(idCollection.find(*idsArray) == objects)
        objects.forEach { idCollection.deleteObject(it) }
        assert(idCollection.find(*idsArray).isEmpty())

        idCollection.insertManyObjects(objects)
        assert(idCollection.find(*idsArray) == objects)
        idCollection.deleteIds(*idsArray)
        assert(idCollection.find(*idsArray).isEmpty())

        idCollection.insertManyObjects(objects)
        assert(idCollection.find(*idsArray) == objects)
        idCollection.deleteObjects(*objectsArray)
        assert(idCollection.find(*idsArray).isEmpty())

        idCollection.insertManyObjects(objects)
        assert(idCollection.find(*idsArray).size == objects.size)
        idCollection.replaceObjects(*changedObjectsArray)
        assert(idCollection.find(*idsArray) == changedObjects)

        idCollection.delete()
        assert(idCollection.isEmpty())
    }

    private fun <T : Any> createCollection (type: KClass<T>): MongoRepository<T> {
        val repository = MongoRepository(type, mongoDatabase(mongodbUrl))
        setupCollection(repository)
        return repository
    }

    protected open fun <T : Any> setupCollection (collection: MongoRepository<T>) {}

    protected abstract val testObjects: List<T>
    protected abstract fun setObjectKey (obj: T, id: Int): T
    protected abstract fun createObject(): T
    protected abstract fun changeObject(obj: T): T

    protected open fun createObjects() = (0..9).map { setObjectKey (createObject(), it) }

    private fun deleteAll (): DeleteResult = collection.delete ()

    @Suppress("unused")
    fun one_object_is_stored_and_loaded_without_error() {
        testObjects.forEach {
            deleteAll()
            collection.insertOneObject(it)
            var result: T = collection.findObjects().first() ?: com.hexagonkt.helpers.error

            assert(result == it)

            deleteAll()
            val object2 = changeObject(it)
            collection.insertOneObject(object2, InsertOneOptions())
            assert(collection.countDocuments() == 1L)
            result = collection.findObjects().first() ?: com.hexagonkt.helpers.error
            assert(result == object2)

            deleteAll()
            collection.insertOne(it.convertToMap().mapKeys { entry -> entry.key as String })
            result = collection.findObjects().first() ?: com.hexagonkt.helpers.error

            assert(result == it)

            deleteAll()
        }
    }

    @Suppress("unused")
    fun many_objects_are_stored_and_loaded_without_error() {
        deleteAll()
        val objects = createObjects ()

        collection.insertManyObjects(objects)

        assert(collection.countDocuments() == 10L)
        val firstObject = objects[0]
        val obj = collection.findObjects(Filters.eq(key.name, getObjectKey(firstObject))).first()
        assert(obj == firstObject)

        deleteAll()

        collection.insertManyObjects(objects, InsertManyOptions().ordered(false))
        assert(collection.countDocuments() == 10L)

        deleteAll()
    }

    @Suppress("unused")
    fun replace_object_stores_modified_data_in_db() {
        deleteAll()
        val entity = createObject()
        val replacement = changeObject(entity)
        val query = Filters.eq(key.name, getObjectKey(entity))

        collection.insertOneObject(entity)
        var result = collection.replaceOneObject(query, replacement)
        assert(result.matchedCount == 1L)

        assert(collection.findObjects(query).first() == replacement)

        result = collection.replaceOneObject(query, entity, ReplaceOptions().upsert(false))
        assert(result.matchedCount == 1L)

        assert(collection.findObjects(query).first() == entity)

        deleteAll()
    }

    @Suppress("unused")
    fun find_and_replace_object_stores_modified_data_in_db() {
        deleteAll()
        var t = System.nanoTime()
        val entity = createObject()
        val replacement = changeObject(entity)
        val query = Filters.eq(key.name, getObjectKey(entity))
        log.trace { "Test setup: " + (System.nanoTime() - t) }

        t = System.nanoTime()
        collection.insertOneObject(entity)
        log.trace { "Insert: " + (System.nanoTime() - t) }
        t = System.nanoTime()
        var result = collection.findOneObjectAndReplace(query, replacement)
        log.trace { "Find and replace: " + (System.nanoTime() - t) }

        assert(entity == result)

        t = System.nanoTime()
        val options = FindOneAndReplaceOptions().upsert(false)
        result = collection.findOneObjectAndReplace(query, entity, options)
        log.trace { "Find and replace (params): " + (System.nanoTime() - t) }
        t = System.nanoTime()

        assert(replacement == result)

        deleteAll()
        log.trace { "Delete all records: " + (System.nanoTime() - t) }
    }
}
