package com.hexagonkt.vertx.http.store

import com.hexagonkt.logger
import com.hexagonkt.sync
import com.hexagonkt.time
import com.hexagonkt.vertx.VertxApplication
import com.hexagonkt.vertx.createVertx
import com.hexagonkt.vertx.http.HttpVerticle
import com.hexagonkt.vertx.http.client.createWebClient
import com.hexagonkt.vertx.http.client.send
import com.hexagonkt.vertx.http.client.sendBuffer
import com.hexagonkt.vertx.serialization.SerializationFormat
import com.hexagonkt.vertx.serialization.SerializationManager.formats
import com.hexagonkt.vertx.serialization.parse
import com.hexagonkt.vertx.serialization.serialize
import com.hexagonkt.vertx.store.Store
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.coroutines.await
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.slf4j.Logger

abstract class StoreControllerTest<T : Any, K : Any> {
    private val logger: Logger = logger()
    private lateinit var verticle: HttpVerticle
    private lateinit var controller: StoreController<T, K>

    private val application : VertxApplication by lazy {
        verticle = object : HttpVerticle() {
            override fun router() = router {
                val store = logger.time ("STORE") { store(vertx, config()) }
                controller = logger.time ("CONTROLLER") { StoreController(store) }
                logger.time("ROUTER") { storeRouter(controller) }
            }
        }

        VertxApplication { verticle }
    }

    protected val client: WebClient by lazy {
        createVertx().createWebClient(this.verticle.actualPort())
    }

    protected val endpoint by lazy { controller.store.name }

    abstract fun store(vertx: Vertx, config: JsonObject): Store<T, K>
    abstract fun createEntity(): T
    abstract fun modifyEntity(entity: T): T
    open val testEntities: List<T> get() = listOf(createEntity())

    @Before fun startVerticle () {
        application.start()
    }

    @After fun stopVerticle () {
        application.stop()
    }

    /**
     * TODO Sorts, Filters, Projections, Pagination
     */
    @Test fun `Entities are stored and retrieved without error` () = sync {
        dropStore()
        assert(countRecords() == 0)

        formats.forEach { contentType ->
            logger.info("Content Type: ${contentType.contentType}")

            testEntities.forEach {
                val entityKey = createEntity(it, contentType)

                val entity = getEntity(entityKey, contentType)
                assert(entity == it)

                val modifiedEntity = modifyEntity(entity)
                replaceEntity(modifiedEntity, contentType)
                assert(modifiedEntity == getEntity(entityKey, contentType))

                deleteEntity(entityKey)
            }
        }
    }

    @Test fun `Entity is stored and retrieved without error` () = sync {
        dropStore()
        assert(countRecords() == 0)

        val response = client.post("/$endpoint").send().await()
        assert(response.statusCode() == 400)
        assert(response.body().toString() == "Entity expected")

        val putResponse = client.put("/$endpoint").send().await()
        assert(putResponse.statusCode() == 400)
        assert(putResponse.body().toString() == "Entity expected")

        formats.forEach { contentType ->
            logger.info("Content Type: ${contentType.contentType}")

            testEntities.forEach {
                val entityKey = createEntity(it, contentType)

                val entity = getEntity(entityKey, contentType)
                assert(entity == it)

                val modifiedEntity = modifyEntity(entity)
                replaceEntity(modifiedEntity, contentType)
                assert(modifiedEntity == getEntity(entityKey, contentType))

                deleteEntity(entityKey)
            }
        }
    }

    private suspend fun deleteEntity(entityKey: String) {
        logger.info("Deleting: $entityKey")

        val response = client.delete("/$endpoint/$entityKey").send().await()
        assert(response.statusCode() == 200)
    }

    private suspend fun getEntity(entityKey: String, contentType: SerializationFormat): T {
        logger.info("Getting: $entityKey")

        val response = client.get("/$endpoint/$entityKey")
            .putHeader("Accept", contentType.contentType)
            .send().await()

        val body = response.body().toString()
        assert(response.statusCode() == 200)
        return body.parse(controller.store.type, contentType)
    }

    private suspend fun replaceEntity(entity: T, contentType: SerializationFormat): Boolean {
        logger.info("Create: $entity")

        val response = client
            .put("/$endpoint")
            .putHeader("Content-Type", contentType.contentType)
            .sendBuffer(Buffer.factory.buffer(entity.serialize(contentType))).await()

        assert(response.statusCode() == 200)
        return response.body().toString().toBoolean()
    }

    private suspend fun createEntity(entity: T, contentType: SerializationFormat): String {
        logger.info("Create: $entity")

        val response = client
            .post("/$endpoint")
            .putHeader("Content-Type", contentType.contentType)
            .sendBuffer(Buffer.factory.buffer(entity.serialize(contentType))).await()

        assert(response.statusCode() == 200)
        return response.body().toString()
    }

    private suspend fun countRecords(): Int {
        val response = client.get("/$endpoint:count").send().await()
        val body = response.body().toString()
        assert(response.statusCode() == 200)
        return body.toInt()
    }

    private suspend fun dropStore() {
        val response = client.delete("/$endpoint:drop").send().await()
        assert(response.statusCode() == 200)
        assert(response.body().toString() == "Store deleted")
    }
}
