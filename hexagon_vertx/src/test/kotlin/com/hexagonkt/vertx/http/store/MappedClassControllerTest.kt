package com.hexagonkt.vertx.http.store

import com.hexagonkt.logger
import com.hexagonkt.sync
import com.hexagonkt.vertx.serialization.SerializationManager.formats
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

            val records = getEntities(contentType, "")
            assert(testEntities.containsAll(records))

//            val modifiedEntities = records.map { modifyEntity(it) }
//            replaceEntities(modifiedEntities, contentType)
//            val modifiedRecords = getEntities(contentType, "")
//            assert(modifiedEntities.containsAll(modifiedRecords))
//            assert(modifiedRecords.containsAll(modifiedEntities))
//
//            val recordsMaps = getEntitiesMaps(contentType, "include=$keyName")
//            val recordIds = recordsMaps
//                .map { it.values.first() ?: error("Key field not found") }
//                .joinToString (separator = ",")
//            deleteEntities("$keyName=$recordIds")
        }
    }
}
