package com.hexagonkt.vertx.store;

import io.vertx.core.Vertx
import org.junit.Test

abstract class StoreTest<T : Any, K : Any> {

    abstract fun store(vertx: Vertx): Store<T, K>
    abstract val testObjects: List<T>

    @Test fun `Entities are stored`() {}
}
