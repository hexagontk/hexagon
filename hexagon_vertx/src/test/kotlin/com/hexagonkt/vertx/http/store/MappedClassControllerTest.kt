package com.hexagonkt.vertx.http.store

import com.hexagonkt.flare
import com.hexagonkt.logger
import com.hexagonkt.sync
import com.hexagonkt.vertx.serialization.SerializationManager.formats
import com.hexagonkt.vertx.serialization.serialize
import com.hexagonkt.vertx.store.Store
import com.hexagonkt.vertx.store.mongodb.MongoDbMapperTest.MappedClass
import com.hexagonkt.vertx.store.mongodb.MongoDbStore
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import org.junit.Test
import org.slf4j.Logger

class MappedClassControllerTest : StoreControllerTest<MappedClass, String>() {
    private val logger: Logger = logger()

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

    @Test fun `Entities are sorted, paginated and filtered without error` () = sync {
        val testEntities = createTestEntities()

        formats.forEach { contentType ->
            dropStore()
            logger.info("Content Type: ${contentType.contentType}")

            val createdEntities = createEntities(testEntities, contentType)
            assert(createdEntities == testEntities.size.toLong())

            val page1 = getEntities(contentType, "sort=-anInt&otherData=even&max=2&offset=2")
            logger.flare(page1.serialize(contentType))
            assert(testEntities.containsAll(page1))
            assert(page1.size == 2)

            val page2 = getEntities(contentType, "sort=-anInt&otherData=even&max=2&offset=4")
            logger.flare(page2.serialize(contentType))
            assert(testEntities.containsAll(page2))
            assert(page2.size == 1)
        }
    }
}
