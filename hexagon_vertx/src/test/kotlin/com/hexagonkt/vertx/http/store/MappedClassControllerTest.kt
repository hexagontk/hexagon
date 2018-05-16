package com.hexagonkt.vertx.http.store

import com.hexagonkt.vertx.store.Store
import com.hexagonkt.vertx.store.mongodb.MongoDbMapperTest.MappedClass
import com.hexagonkt.vertx.store.mongodb.MongoDbStore
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient

class MappedClassControllerTest : StoreControllerTest<MappedClass, String>() {
    override fun store(vertx: Vertx, config: JsonObject): Store<MappedClass, String> =
        MongoDbStore(
            MongoClient.createShared(vertx, config),
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
