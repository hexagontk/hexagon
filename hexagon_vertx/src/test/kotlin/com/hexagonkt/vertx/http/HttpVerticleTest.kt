package com.hexagonkt.vertx.http

import com.hexagonkt.helpers.logger
import com.hexagonkt.helpers.flare
import com.hexagonkt.helpers.sync
import com.hexagonkt.vertx.VertxApplication
import com.hexagonkt.vertx.createVertx
import com.hexagonkt.vertx.http.client.createWebClient
import com.hexagonkt.vertx.http.client.send
import com.hexagonkt.vertx.http.client.sendBuffer
import com.hexagonkt.serialization.JsonFormat
import com.hexagonkt.vertx.serialization.serialize
import gherkin.deps.com.google.gson.JsonObject
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.coroutines.await
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.slf4j.Logger

class HttpVerticleTest {
    private data class Player(val name: String, val number: Int)

    private val logger: Logger = logger()
    private lateinit var verticle: HttpVerticle

    private val application : VertxApplication by lazy {
        verticle = object : HttpVerticle(HttpServerOptions().setPort(2020)) {
            override fun router() = router {
                get("/not_implemented", ::notImplemented)
                get("/boolean") { handle { true } }
                get("/long") { handle { 1L } }
                get("/exception") { error("Fail") }
                get("/content_type") {
                    response.contentType = "text/rtf"
                    end(200, "end")
                }

                put { end(200, "put") }
                patch { end(200, "patch") }
                post(Player::class) { it?.name ?: "John Doe" }
                get("/handler") {
                    val handler: (AsyncResult<JsonObject>) -> Unit =
                        handler { end(200, it?.toString() ?: "") }
                    handler(Future.succeededFuture(JsonObject()))
                }
            }
        }

        VertxApplication { verticle }
    }

    private val client: WebClient by lazy {
        createVertx().createWebClient(this.verticle.actualPort())
    }

    @Before fun startVerticle () {
        application.start()
    }

    @After fun stopVerticle () {
        application.stop()
    }

    @Test fun `Test handlers` () = sync {
        val responseBoolean = client.get("/boolean").send().await()
        assert(responseBoolean.statusCode() == 200)
        assert(responseBoolean.body().toString() == "true")

        val responseLong = client.get("/long").send().await()
        assert(responseLong.statusCode() == 200)
        logger.flare("/long response body ${responseLong.body()}")
        // TODO Fix in Travis
//        assert(responseLong.body().toString() == "1")
//        logger.flare("/long OK")

        val responseException = client.get("/exception").send().await()
        assert(responseException.statusCode() == 500)
        assert(responseException.body().toString() == "Fail")

        val responseContentType = client.get("/content_type").send().await()
        assert(responseContentType.headers()["Content-Type"] == "text/rtf")
        assert(responseContentType.statusCode() == 200)
        assert(responseContentType.body().toString() == "end")

        val responseNotImplemented = client.get("/not_implemented").send().await()
        assert(responseNotImplemented.statusCode() == 501)
        assert(responseNotImplemented.body().toString() == "Not implemented")

        val responsePut = client.put("/").send().await()
        assert(responsePut.statusCode() == 200)
        assert(responsePut.body().toString() == "put")

        val responsePatch = client.patch("/").send().await()
        assert(responsePatch.statusCode() == 200)
        assert(responsePatch.body().toString() == "patch")

        val serializedPlayer = Player("Michael", 23).serialize(JsonFormat)
        val responsePost = client.post("/")
            .putHeader("Content-Type", JsonFormat.contentType)
            .sendBuffer(Buffer.factory.buffer(serializedPlayer)).await()
        assert(responsePost.statusCode() == 200)
        assert(responsePost.body().toString() == "Michael")

        val responseHandler = client.get("/handler").send().await()
        assert(responseHandler.statusCode() == 200)
        assert(responseHandler.body().toString().contains("{}"))
    }
}
