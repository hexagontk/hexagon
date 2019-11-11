package com.hexagonkt.store.mongodb

import com.hexagonkt.helpers.Logger
import com.hexagonkt.settings.SettingsManager
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
}
