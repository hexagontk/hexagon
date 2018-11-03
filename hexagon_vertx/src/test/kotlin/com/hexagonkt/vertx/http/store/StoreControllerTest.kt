package com.hexagonkt.vertx.http.store

import com.hexagonkt.helpers.Logger
import com.hexagonkt.helpers.logger
import com.hexagonkt.helpers.sync
import com.hexagonkt.serialization.SerializationFormat
import com.hexagonkt.serialization.SerializationManager.formats
import com.hexagonkt.serialization.parse
import com.hexagonkt.serialization.parseList
import com.hexagonkt.serialization.serialize
import com.hexagonkt.vertx.VertxApplication
import com.hexagonkt.vertx.createVertx
import com.hexagonkt.vertx.http.HttpVerticle
import com.hexagonkt.vertx.http.client.createWebClient
import com.hexagonkt.vertx.http.client.send
import com.hexagonkt.vertx.http.client.sendBuffer
import com.hexagonkt.vertx.store.Store
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.coroutines.await
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.fail

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
    private val keyName by lazy { controller.store.key.name }

    abstract fun store(vertx: Vertx, config: JsonObject): Store<T, K>
    abstract fun createEntity(index: Int): T
    abstract fun modifyEntity(entity: T): T
    open fun createTestEntities(): List<T> = (0..9).map { createEntity(it) }

    @Before fun startVerticle () {
        application.start()
    }

    @After fun stopVerticle () {
        application.stop()
    }

    /**
     * TODO Sorts, Filters, Pagination
     */
    @Test fun `Entities are stored and retrieved without error` () = sync {
        dropStore()
        assert(countRecords() == 0L)

        val testEntities = createTestEntities()

        formats.forEach { contentType ->
            logger.info { "Content Type: ${contentType.contentType}" }

            val createdEntities = createEntities(testEntities, contentType)
            assert(createdEntities == testEntities.size.toLong())
            val countRecords = countRecords()
            assert(countRecords == testEntities.size.toLong())

            val records = getEntities(contentType, "")
            assert(testEntities.containsAll(records))
            assert(records.containsAll(testEntities))

            val modifiedEntities = records.map { modifyEntity(it) }
            replaceEntities(modifiedEntities, contentType)
            val modifiedRecords = getEntities(contentType, "")
            assert(modifiedEntities.containsAll(modifiedRecords))
            assert(modifiedRecords.containsAll(modifiedEntities))

            val recordsMaps = getEntitiesMaps(contentType, "include=$keyName")
            val recordIds = recordsMaps
                .asSequence()
                .map { it.values.first() ?: error("Key field not found") }
                .joinToString (separator = ",")
            deleteEntities("$keyName=$recordIds")
        }
    }

    /**
     * TODO Patch
     */
    @Test fun `Entity is stored and retrieved without error` () = sync {
        dropStore()
        assert(countRecords() == 0L)

        val response = client.post("/$endpoint").send().await()
        assert(response.statusCode() == 400)
        assert(response.body().toString() == "Entity expected")

        val putResponse = client.put("/$endpoint").send().await()
        assert(putResponse.statusCode() == 400)
        assert(putResponse.body().toString() == "Entity expected")

        formats.forEach { contentType ->
            logger.info { "Content Type: ${contentType.contentType}" }

            createTestEntities().forEach {
                val entityKey = createEntity(it, contentType)

                val entity = getEntity(entityKey, contentType) ?: fail("Entity expected")
                assert(entity == it)

                val modifiedEntity = modifyEntity(entity)
                replaceEntity(modifiedEntity, contentType)
                assert(modifiedEntity == getEntity(entityKey, contentType))

                deleteEntity(entityKey)

                getEntity(entityKey, contentType, 404)
                deleteEntity(entityKey, 404)
            }
        }
    }

    private suspend fun deleteEntity(entityKey: String, expectedStatus: Int = 200) {
        logger.info { "Deleting: $entityKey" }

        val response = client.delete("/$endpoint/$entityKey").send().await()
        assert(response.statusCode() == expectedStatus)
    }

    private suspend fun deleteEntities(entityKey: String, expectedStatus: Int = 200) {
        logger.info { "Deleting: $entityKey" }

        val response = client.delete("/$endpoint?$entityKey").send().await()
        val statusCode = response.statusCode()
        assert(statusCode == expectedStatus)
    }

    private suspend fun getEntity(
        entityKey: String = "", contentType: SerializationFormat, expectedStatus: Int = 200): T? {

        logger.info { "Getting: $entityKey" }

        val response = client.get("/$endpoint/$entityKey")
            .putHeader("Accept", contentType.contentType)
            .send().await()

        assert(response.statusCode() == expectedStatus)
        val body = response.body()?.toString()
        return body?.parse(controller.store.type, contentType)
    }

    private suspend fun getEntitiesMaps(
        contentType: SerializationFormat, queryString: String = ""): List<Map<*, *>> {

        logger.info { "Getting: $queryString" }

        val path = if (queryString.isBlank()) "" else "?$queryString"
        val response = client.get("/$endpoint$path")
            .putHeader("Accept", contentType.contentType)
            .send().await()

        assert(response.statusCode() == 200)
        val body = response.body()?.toString()
        return body?.parseList(contentType) ?: emptyList()
    }

    protected suspend fun getEntities(
        contentType: SerializationFormat, queryString: String = ""): List<T> {

        logger.info { "Getting: $queryString" }

        val path = if (queryString.isBlank()) "" else "?$queryString"
        val response = client.get("/$endpoint$path")
            .putHeader("Accept", contentType.contentType)
            .send().await()

        assert(response.statusCode() == 200)
        val body = response.body()?.toString()
        return body?.parseList(controller.store.type, contentType) ?: emptyList()
    }

    private suspend fun replaceEntity(entity: T, contentType: SerializationFormat): Boolean {
        logger.info { "Create: $entity" }

        val response = client
            .put("/$endpoint")
            .putHeader("Content-Type", contentType.contentType)
            .sendBuffer(Buffer.factory.buffer(entity.serialize(contentType))).await()

        assert(response.statusCode() == 200)
        return response.body().toString().toBoolean()
    }

    private suspend fun replaceEntities(entity: List<T>, contentType: SerializationFormat): Boolean {
        logger.info { "Create: $entity" }

        val response = client
            .put("/$endpoint")
            .putHeader("Content-Type", contentType.contentType)
            .sendBuffer(Buffer.factory.buffer(entity.serialize(contentType))).await()

        assert(response.statusCode() == 200)
        return response.body().toString().toBoolean()
    }

    protected suspend fun createEntities(entity: List<T>, contentType: SerializationFormat): Long {
        logger.info { "Create: $entity" }

        val response = client
            .post("/$endpoint")
            .putHeader("Content-Type", contentType.contentType)
            .sendBuffer(Buffer.factory.buffer(entity.serialize(contentType))).await()

        assert(response.statusCode() == 200)
        return response.body().toString().trim().toLong()
    }

    private suspend fun createEntity(entity: T, contentType: SerializationFormat): String {
        logger.info { "Create: $entity" }

        val response = client
            .post("/$endpoint")
            .putHeader("Content-Type", contentType.contentType)
            .sendBuffer(Buffer.factory.buffer(entity.serialize(contentType))).await()

        assert(response.statusCode() == 200)
        return response.body().toString()
    }

    private suspend fun countRecords(): Long {
        val response = client.get("/$endpoint:count").send().await()
        val body = response.body().toString()
        assert(response.statusCode() == 200)
        return body.toLong()
    }

    protected suspend fun dropStore() {
        val response = client.delete("/$endpoint:drop").send().await()
        assert(response.statusCode() == 200)
        assert(response.body().toString() == "Store deleted")
    }
}
