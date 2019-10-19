package com.hexagonkt.http.server.examples

import com.hexagonkt.http.Method
import com.hexagonkt.http.Method.POST
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
            corsPath("/example/org", CorsSettings("example.org"))
            corsPath("/no/credentials", CorsSettings(supportCredentials = false))
            corsPath("/only/post", CorsSettings(allowedMethods = setOf(POST)))
            corsPath("/cache", CorsSettings(preFlightMaxAge = 10))
        }
    }

    private fun Router.corsPath(path: String, settings: CorsSettings) {
        path(path) {
            // CORS settings can change for different routes
            cors(settings)

            get("/path") { ok(request.method) }
            post("/path") { ok(request.method) }
            put("/path") { ok(request.method) }
            delete("/path") { ok(request.method) }
            get { ok(request.method) }
            post { ok(request.method) }
            put { ok(request.method) }
            delete { ok(request.method) }
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

    @Test fun `Request without origin continues as non CORS`() {
        listOf(
            client.get("/default"),
            client.get("/default/path")
        ).forEach {
            assert(it.statusCode == 200)
            assert(it.responseBody == "GET")
        }
    }

    @Test fun `Request with not allowed origin is forbidden`() {
        listOf(
            client.get("/example/org", mapOf("Origin" to listOf("other.com"))),
            client.get("/example/org/path", mapOf("Origin" to listOf("other.com")))
        ).forEach {
            assert(it.statusCode == 403)
            assert(it.responseBody == "Not allowed origin: other.com")
        }
    }

    @Test fun `Allowed origin is returned properly`() {
        listOf(
            client.get("/no/credentials", mapOf("Origin" to listOf("other.com"))),
            client.get("/no/credentials/path", mapOf("Origin" to listOf("other.com")))
        ).forEach {
            assert(it.statusCode == 200)
            assert(it.responseBody == "GET")
            assert(it.headers["Access-Control-Allow-Origin"] == "*")
            assert(it.headers["Vary"] == null)
        }
    }

    @Test fun `Simple CORS request`() {
        val result = client.get("/default", mapOf("Origin" to listOf("example.org")))
        assert(result.statusCode == 200)
        assert(result.headers["Access-Control-Allow-Origin"] == "example.org")
        assert(result.headers["Vary"] == "Origin")
        assert(result.headers["Access-Control-Allow-Credentials"] == "true")
    }

    @Test fun `Simple CORS request with not allowed method`() {
        val result = client.get("/only/post", mapOf("Origin" to listOf("example.org")))
        assert(result.statusCode == 403)
        assert(result.responseBody == "Not allowed method: GET")
    }

    @Test fun `CORS pre flight without request method`() {
        val result = client.options("/default", callHeaders = mapOf(
            "Origin" to listOf("example.org")
        ))
        assert(result.statusCode == 403)
        assert(result.responseBody == "Access-Control-Request-Method required header not found")
    }

    @Test fun `CORS pre flight`() {
        val result = client.options("/default", callHeaders = mapOf(
            "Origin" to listOf("example.org"),
            "Access-Control-Request-Method" to listOf("GET")
        ))
        assert(result.statusCode == 204)
        assert(result.responseBody.isEmpty())
    }

    @Test fun `CORS full pre flight`() {
        client.options("/default", callHeaders = mapOf(
            "Origin" to listOf("example.org"),
            "Access-Control-Request-Method" to listOf("GET"),
            "Access-Control-Request-Headers" to listOf("header1", "header2")
        )).apply {
            assert(statusCode == 204)
            assert(responseBody.isEmpty())
        }
        client.options("/cache", callHeaders = mapOf(
            "Origin" to listOf("example.org"),
            "Access-Control-Request-Method" to listOf("GET"),
            "Access-Control-Request-Headers" to listOf("header1", "header2")
        )).apply {
            assert(statusCode == 204)
            assert(responseBody.isEmpty())
            assert(headers["Access-Control-Max-Age"] == "10")
        }
    }

    @Test fun `CORS pre flight with not allowed method`() {
        val result = client.options("/only/post", callHeaders = mapOf(
            "Origin" to listOf("example.org"),
            "Access-Control-Request-Method" to listOf("GET")
        ))
        assert(result.statusCode == 403)
        assert(result.responseBody == "Not allowed method: GET")
    }
}
