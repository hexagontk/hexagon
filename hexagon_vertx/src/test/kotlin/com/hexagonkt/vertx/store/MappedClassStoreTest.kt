package com.hexagonkt.vertx.store

import com.hexagonkt.vertx.store.mongodb.MongoDbMapperTest.MappedClass
import com.hexagonkt.vertx.store.mongodb.MongoDbStore
import io.vertx.core.Vertx
import io.vertx.ext.mongo.MongoClient
import io.vertx.kotlin.core.json.JsonObject

class MappedClassStoreTest : StoreTest<MappedClass, String>() {
    override fun store(vertx: Vertx): Store<MappedClass, String> =
        MongoDbStore(
            MongoClient.createShared(
                vertx,
                JsonObject("connection_string" to "mongodb://localhost/devices")
            ),
            MappedClass::class,
            MappedClass::oneString
        )

    override fun createEntity(index: Int) = MappedClass(
        oneString = "key_${System.nanoTime()}",
        anInt = index,
        otherData = if (index % 2 == 0) "even" else "odd"
    )

    override fun modifyEntity(entity: MappedClass): MappedClass = entity.copy(
        oneBoolean = !entity.oneBoolean
    )
}
