package com.hexagonkt.vertx.store.mongodb

import com.hexagonkt.helpers.sync
import com.hexagonkt.vertx.createVertx
import com.hexagonkt.vertx.store.mongodb.MongoDbMapperTest.MappedClass
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import io.vertx.kotlin.core.json.JsonObject
import io.vertx.kotlin.coroutines.await
import org.junit.Test

class MongoDbStoreTest {
    private val vertx: Vertx = createVertx()
    private val jsonObject: JsonObject = JsonObject("connection_string" to "mongodb://localhost/db")
    private val client: MongoClient = MongoClient.createShared(vertx, jsonObject)

    @Test fun `Insert one record returns the proper key`() = sync {
        val mappedClass = MappedClass()
        val store = MongoDbStore(client, MappedClass::class, MappedClass::oneString, "mapped")
        store.drop().await()
        val await = store.insertOne(mappedClass).await()
        val storedClass = store.findOne(await).await()
        assert(await.isNotBlank())
        assert(mappedClass == storedClass)
    }
}
