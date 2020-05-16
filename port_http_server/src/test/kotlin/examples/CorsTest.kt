package com.hexagonkt.http.server.examples

import com.hexagonkt.http.Method.POST
import com.hexagonkt.http.client.Client
import com.hexagonkt.http.client.ahc.AhcAdapter
import com.hexagonkt.http.server.CorsSettings
import com.hexagonkt.http.server.Router
import com.hexagonkt.http.server.Server
import com.hexagonkt.http.server.ServerPort
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

@Test abstract class CorsTest(adapter: ServerPort) {

    // cors
    val server: Server = Server(adapter) {
        corsPath("/default", CorsSettings())
        corsPath("/example/org", CorsSettings("example.org"))
        corsPath("/no/credentials", CorsSettings(supportCredentials = false))
        corsPath("/only/post", CorsSettings(allowedMethods = setOf(POST)))
        corsPath("/cache", CorsSettings(preFlightMaxAge = 10))
        corsPath("/exposed/headers", CorsSettings(exposedHeaders = setOf("head")))
        corsPath("/allowed/headers", CorsSettings(allowedHeaders = setOf("head")))
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

    private val client: Client by lazy {
        Client(AhcAdapter(), "http://localhost:${server.runtimePort}")
    }

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
            assert(it.status == 200)
            assert(it.body == "GET")
        }
    }

    @Test fun `Request with not allowed origin is forbidden`() {
        listOf(
            client.get("/example/org", mapOf("Origin" to listOf("other.com"))),
            client.get("/example/org/path", mapOf("Origin" to listOf("other.com")))
        ).forEach {
            assert(it.status == 403)
            assert(it.body == "Not allowed origin: other.com")
        }
    }

    @Test fun `Allowed origin is returned properly`() {
        listOf(
            client.get("/no/credentials", mapOf("Origin" to listOf("other.com"))),
            client.get("/no/credentials/path", mapOf("Origin" to listOf("other.com")))
        ).forEach {
            assert(it.status == 200)
            assert(it.body == "GET")
            assert(it.headers["Access-Control-Allow-Origin"]?.first() == "*")
            assert(it.headers["Vary"] == null)
        }
    }

    @Test fun `Simple CORS request`() {
        val result = client.get("/default", mapOf("Origin" to listOf("example.org")))
        assert(result.status == 200)
        assert(result.headers["Access-Control-Allow-Origin"]?.first() == "example.org")
        assert(result.headers["Vary"]?.first() == "Origin")
        assert(result.headers["Access-Control-Allow-Credentials"]?.first() == "true")
    }

    @Test fun `Simple CORS request with not allowed method`() {
        val result = client.get("/only/post", mapOf("Origin" to listOf("example.org")))
        assert(result.status == 403)
        assert(result.body == "Not allowed method: GET")
    }

    @Test fun `Simple CORS request with exposed headers`() {
        val result = client.get("/exposed/headers", mapOf(
            "Origin" to listOf("example.org"),
            "head" to listOf("exposed header")
        ))
        assert(result.status == 200)
        assert(result.headers["Access-Control-Allow-Origin"]?.first() == "example.org")
        assert(result.headers["Vary"]?.first() == "Origin")
        assert(result.headers["Access-Control-Allow-Credentials"]?.first() == "true")
        assert(result.headers["Access-Control-Expose-Headers"]?.first() == "head")
    }

    @Test fun `CORS pre flight with empty request method`() {
        val result = client.options("/default", callHeaders = mapOf(
            "Origin" to listOf("example.org"),
            "Access-Control-Request-Method" to emptyList()
        ))
        assert(result.status == 403)
        assert(result.body == "Access-Control-Request-Method required header not found")
    }

    @Test fun `CORS pre flight without request method`() {
        val result = client.options("/default", callHeaders = mapOf(
            "Origin" to listOf("example.org")
        ))
        assert(result.status == 403)
        assert(result.body == "Access-Control-Request-Method required header not found")
    }

    @Test fun `CORS pre flight`() {
        val result = client.options("/default", callHeaders = mapOf(
            "Origin" to listOf("example.org"),
            "Access-Control-Request-Method" to listOf("GET")
        ))
        assert(result.status == 204)
        assert(result.body?.isEmpty() ?: false)
    }

    @Test fun `CORS full pre flight`() {
        client.options("/default", callHeaders = mapOf(
            "Origin" to listOf("example.org"),
            "Access-Control-Request-Method" to listOf("GET"),
            "Access-Control-Request-Headers" to listOf("header1,header2")
        )).apply {
            assert(status == 204)
            assert(body?.isEmpty() ?: false)
        }
        client.options("/cache", callHeaders = mapOf(
            "Origin" to listOf("example.org"),
            "Access-Control-Request-Method" to listOf("GET"),
            "Access-Control-Request-Headers" to listOf("header1,header2")
        )).apply {
            assert(status == 204)
            assert(body?.isEmpty() ?: false)
            assert(headers["Access-Control-Max-Age"]?.first() == "10")
        }
    }

    @Test fun `CORS pre flight with not allowed method`() {
        val result = client.options("/only/post", callHeaders = mapOf(
            "Origin" to listOf("example.org"),
            "Access-Control-Request-Method" to listOf("GET")
        ))
        assert(result.status == 403)
        assert(result.body == "Not allowed method: GET")
    }

    @Test fun `CORS pre flight with not allowed headers`() {
        val result = client.options("/allowed/headers", callHeaders = mapOf(
            "Origin" to listOf("example.org"),
            "Access-Control-Request-Method" to listOf("GET"),
            "Access-Control-Request-Headers" to listOf("header1,header2")
        ))
        assert(result.status == 403)
        assert(result.body == "Not allowed headers")
    }

    @Test fun `CORS pre flight with allowed headers`() {
        val result = client.options("/allowed/headers", callHeaders = mapOf(
            "Origin" to listOf("example.org"),
            "Access-Control-Request-Method" to listOf("GET"),
            "Access-Control-Request-Headers" to listOf("head")
        ))
        assert(result.status == 204)
        assert(result.body?.isEmpty() ?: false)
    }
}
