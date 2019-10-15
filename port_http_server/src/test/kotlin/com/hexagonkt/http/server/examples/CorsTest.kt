package com.hexagonkt.http.server.examples

import com.hexagonkt.http.client.Client
import com.hexagonkt.http.server.CorsSettings
import com.hexagonkt.http.server.Router
import com.hexagonkt.http.server.Server
import com.hexagonkt.http.server.ServerPort
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

@Test abstract class CorsTest(adapter: ServerPort) {

    // cors
    val server: Server by lazy {
        Server(adapter) {
            corsPath("/default", CorsSettings())
        }
    }

    private fun Router.corsPath(path: String, settings: CorsSettings) {
        path(path) {
            cors(settings)
            post("/books") { ok() }
            get("/books/{id}") { ok() }
            put("/books/{id}") { ok() }
            delete("/books/{id}") { ok() }
            get { ok() }
        }
    }
    // cors

    private val client: Client by lazy { Client("http://localhost:${server.runtimePort}") }

    @BeforeClass fun initialize() {
        server.start()
    }

    @AfterClass fun shutdown() {
        server.stop()
    }

    @Test fun `Simple CORS request`() {
        val result = client.get("/default", mapOf("Origin" to listOf("example.org")))
        assert(200 == result.statusCode)
    }

    @Test fun `CORS Pre flight`() {
        val result = client.options("/default", callHeaders = mapOf(
            "Origin" to listOf("example.org"),
            "Access-Control-Request-Method" to listOf("GET")
        ))
        assert(204 == result.statusCode)
    }
}
