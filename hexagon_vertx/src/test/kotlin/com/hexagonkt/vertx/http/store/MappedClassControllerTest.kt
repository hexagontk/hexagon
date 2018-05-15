package com.hexagonkt.vertx.http.store

import com.hexagonkt.vertx.store.Store
import com.hexagonkt.vertx.store.mongodb.MongoDbMapperTest.MappedClass
import com.hexagonkt.vertx.store.mongodb.MongoDbStore
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient

class MappedClassControllerTest : StoreControllerTest<MappedClass, String>() {
    override fun patchEntity(entity: MappedClass): Pair<String, *> {
        TODO("not implemented")
    }

    override fun createTestEntities(): List<MappedClass> = listOf(
        createEntity(),
        createEntity(),
        createEntity()
    )

    override fun store(vertx: Vertx, config: JsonObject): Store<MappedClass, String> =
        MongoDbStore(
            MongoClient.createShared(vertx, config),
            MappedClass::class,
            MappedClass::oneString
        )

    override fun createEntity() = MappedClass(oneString = "key_${System.nanoTime()}")

    override fun modifyEntity(entity: MappedClass): MappedClass = entity.copy(
        oneBoolean = !entity.oneBoolean
    )
}
