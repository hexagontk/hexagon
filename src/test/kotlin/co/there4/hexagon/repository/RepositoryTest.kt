package co.there4.hexagon.repository

import co.there4.hexagon.repository.RepositoryEventAction.INSERTED
import co.there4.hexagon.serialization.SerializationTest
import co.there4.hexagon.util.CompanionLogger
import com.mongodb.MongoBulkWriteException
import com.mongodb.client.model.FindOneAndReplaceOptions
import com.mongodb.client.model.InsertManyOptions
import com.mongodb.client.model.InsertOneOptions
import com.mongodb.client.model.UpdateOptions
import java.lang.System.*
import kotlin.reflect.KClass
import com.mongodb.client.model.Filters.*
import com.mongodb.client.result.DeleteResult
import org.bson.Document

/**
 * TODO Check events
 */
abstract class RepositoryTest<T : Any, K : Any> (type: KClass<T>, val idField: String) :
    SerializationTest<T> (type) {

    companion object : CompanionLogger (RepositoryTest::class)

    protected val collection: MongoRepository<T> = createCollection(type)

    fun <T : Any> createCollection (type: KClass<T>): MongoRepository<T> {
        val repository = MongoRepository (type, mongoDatabase (), true)
        setupCollection(repository)
        return repository
    }

    protected open fun <T : Any> setupCollection (collection: MongoRepository<T>) {}

    protected abstract fun setObjectKey (obj: T, id: Int): T
    protected abstract fun getObjectKey (obj: T): K
    protected abstract fun createObject(): T
    protected abstract fun changeObject(obj: T): T

    protected open fun createObjects() = (0..9).map { setObjectKey (createObject(), it) }

    protected fun deleteAll (): DeleteResult = collection.deleteMany (Document ())

    fun one_object_is_stored_and_loaded_without_error() {
        testObjects.forEach {
            var eventCount = 0
            on (it.javaClass.kotlin, INSERTED) {
                eventCount++
            }

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
//            assert(eventCount > 0) // TODO Check event count
        }
    }

    fun many_objects_are_stored_and_loaded_without_error() {
        testObjects.forEach {
            deleteAll()
            val objects = createObjects ()

            try {
                collection.insertManyObjects(objects)
            } catch (e: MongoBulkWriteException) {
                err("Repository error", e)
            }

            assert(collection.count() == 10L)
            val firstObject = objects[0]
            val obj = collection.findObjects(eq<K>(idField, getObjectKey(firstObject))).first()
            assert(obj == firstObject)

            deleteAll()

            collection.insertManyObjects(objects, InsertManyOptions().ordered(false))
            assert(collection.count() == 10L)

            deleteAll()
        }
    }

    fun replace_object_stores_modified_data_in_db() {
        testObjects.forEach {
            deleteAll()
            val entity = createObject()
            val replacement = changeObject(entity)
            val query = eq<K>(idField, getObjectKey(entity))

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

    fun find_and_replace_object_stores_modified_data_in_db() {
        testObjects.forEach {
            deleteAll()
            var t = nanoTime()
            val entity = createObject()
            val replacement = changeObject(entity)
            val query = eq<K>(idField, getObjectKey(entity))
            trace("Test setup: " + (nanoTime() - t))

            t = nanoTime()
            collection.insertOneObject(entity)
            trace("Insert: " + (nanoTime() - t))
            t = nanoTime()
            var result = collection.findOneObjectAndReplace(query, replacement)
            trace("Find and replace: " + (nanoTime() - t))

            assert(entity == result)

            t = nanoTime()
            val options = FindOneAndReplaceOptions().upsert(false)
            result = collection.findOneObjectAndReplace(query, entity, options)
            trace("Find and replace (params): " + (nanoTime() - t))
            t = nanoTime()

            assert(replacement == result)

            deleteAll()
            trace("Delete all records: " + (nanoTime() - t))
        }
    }
}
