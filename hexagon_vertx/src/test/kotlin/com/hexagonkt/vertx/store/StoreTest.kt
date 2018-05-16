package com.hexagonkt.vertx.store;

import io.vertx.core.Vertx
import org.junit.Test

abstract class StoreTest<T : Any, K : Any> {

    abstract fun store(vertx: Vertx): Store<T, K>
    abstract fun createEntity(index: Int): T
    abstract fun modifyEntity(entity: T): T
    open fun createTestEntities(): List<T> = (0..9).map { createEntity(it) }

    @Test fun `Entities are stored`() {

    }
}
