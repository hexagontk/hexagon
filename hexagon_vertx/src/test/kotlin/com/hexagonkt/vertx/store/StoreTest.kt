package com.hexagonkt.vertx.store;

import com.hexagonkt.sync
import com.hexagonkt.vertx.createVertx
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.await
import org.junit.Test

abstract class StoreTest<T : Any, K : Any> {

    abstract fun store(vertx: Vertx): Store<T, K>
    abstract fun createEntity(index: Int): T
    abstract fun modifyEntity(entity: T): T
    open fun createTestEntities(): List<T> = (0..9).map { createEntity(it) }

    @Test fun `Entities are stored`() = sync {
        val testEntities = createTestEntities()
        val store = store(createVertx())
        store.drop()

        store.saveMany(testEntities).await()

        testEntities.forEach {
            store.saveOne(it).await()
        }

        // TODO Fix this!
//        store.saveMany(testEntities).await()
    }
}
