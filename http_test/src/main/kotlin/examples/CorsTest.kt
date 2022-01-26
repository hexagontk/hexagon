package com.hexagonkt.http.test.examples

import com.hexagonkt.core.multiMapOf
import com.hexagonkt.core.multiMapOfLists
import com.hexagonkt.http.client.HttpClientPort
import com.hexagonkt.http.model.ClientErrorStatus.FORBIDDEN
import com.hexagonkt.http.model.HttpMethod.POST
import com.hexagonkt.http.model.SuccessStatus.NO_CONTENT
import com.hexagonkt.http.model.SuccessStatus.OK
import com.hexagonkt.http.server.*
import com.hexagonkt.http.server.callbacks.CorsCallback
import com.hexagonkt.http.server.handlers.ServerBuilder
import com.hexagonkt.http.server.handlers.PathHandler
import com.hexagonkt.http.server.handlers.ServerHandler
import com.hexagonkt.http.server.handlers.path
import com.hexagonkt.http.test.BaseTest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@Suppress("FunctionName") // This class's functions are intended to be used only in tests
abstract class CorsTest(
    final override val clientAdapter: () -> HttpClientPort,
    final override val serverAdapter: () -> HttpServerPort,
    final override val serverSettings: HttpServerSettings = HttpServerSettings(),
) : BaseTest() {

    // cors
    val path: PathHandler = path {
        corsPath("/default", CorsCallback())
        corsPath("/example/org", CorsCallback("example.org"))
        corsPath("/no/credentials", CorsCallback(supportCredentials = false))
        corsPath("/only/post", CorsCallback(allowedMethods = setOf(POST)))
        corsPath("/cache", CorsCallback(preFlightMaxAge = 10))
        corsPath("/exposed/headers", CorsCallback(exposedHeaders = setOf("head")))
        corsPath("/allowed/headers", CorsCallback(allowedHeaders = setOf("head")))
    }

    private fun ServerBuilder.corsPath(path: String, cors: CorsCallback) {
        path(path) {
            // CORS settings can change for different routes
            filter(pattern = "*", callback = cors)

            get("/path") { ok(method.toString()) }
            post("/path") { ok(method.toString()) }
            put("/path") { ok(method.toString()) }
            delete("/path") { ok(method.toString()) }
            get { ok(method.toString()) }
            post { ok(method.toString()) }
            put { ok(method.toString()) }
            delete { ok(method.toString()) }
        }
    }
    // cors

    override val handler: ServerHandler = path

    @Test fun `Request without origin continues as non CORS`() {
        listOf(
            client.get("/default"),
            client.get("/default/path")
        ).forEach {
            assertEquals(OK, it.status)
            assertEquals("GET", it.body)
        }
    }

    @Test fun `Request with not allowed origin is forbidden`() {
        listOf(
            client.get("/example/org", multiMapOf("origin" to "other.com")),
            client.get("/example/org/path", multiMapOf("origin" to "other.com"))
        ).forEach {
            assertEquals(FORBIDDEN, it.status)
            assertEquals("Not allowed origin: other.com", it.body)
        }
    }

    @Test fun `Allowed origin is returned properly`() {
        listOf(
            client.get("/no/credentials", multiMapOf("origin" to "other.com")),
            client.get("/no/credentials/path", multiMapOf("origin" to "other.com"))
        ).forEach {
            assertEquals(OK, it.status)
            assertEquals("GET", it.body)
            assertEquals("*", it.headers["access-control-allow-origin"])
            assert(it.headers["vary"]?.contains("Origin")?.not() ?: true)
        }
    }

    @Test fun `Simple CORS request`() {
        val result = client.get("/default", multiMapOf("origin" to "example.org"))
        assertEquals(OK, result.status)
        assertEquals("example.org", result.headers["access-control-allow-origin"])
        assert(result.headers["vary"]?.contains("Origin") ?: false)
        assertEquals("true", result.headers["access-control-allow-credentials"])
    }

    @Test fun `Simple CORS request with not allowed method`() {
        val result = client.get("/only/post", multiMapOf("origin" to "example.org"))
        assertEquals(FORBIDDEN, result.status)
        assertEquals("Not allowed method: GET", result.body)
    }

    @Test fun `Simple CORS request with exposed headers`() {
        val result = client.get("/exposed/headers", multiMapOf(
            "origin" to "example.org",
            "head" to "exposed header"
        ))
        assertEquals(OK, result.status)
        assertEquals("example.org", result.headers["access-control-allow-origin"])
        assert(result.headers["vary"]?.contains("Origin") ?: false)
        assertEquals("true", result.headers["access-control-allow-credentials"])
        assertEquals("head", result.headers["access-control-expose-headers"])
    }

    @Test fun `CORS pre flight with empty request method`() {
        val result = client.options("/default", headers = multiMapOfLists(
            "origin" to listOf("example.org"),
            "access-control-request-method" to emptyList()
        ))
        assertEquals(FORBIDDEN, result.status)
        assertEquals("access-control-request-method required header not found", result.body)
    }

    @Test fun `CORS pre flight without request method`() {
        val result = client.options("/default", headers = multiMapOf("origin" to "example.org"))
        assertEquals(FORBIDDEN, result.status)
        assertEquals("access-control-request-method required header not found", result.body)
    }

    @Test fun `CORS pre flight`() {
        val result = client.options("/default", headers = multiMapOf(
            "origin" to "example.org",
            "access-control-request-method" to "GET"
        ))
        assertEquals(NO_CONTENT, result.status)
        assert(result.bodyString().isEmpty())
    }

    @Test fun `CORS full pre flight`() = runBlocking<Unit> {
        client.options("/default", headers = multiMapOf(
            "origin" to ("example.org"),
            "access-control-request-method" to "GET",
            "access-control-request-headers" to "header1,header2"
        )).apply {
            assertEquals(NO_CONTENT, status)
            assert(bodyString().isEmpty())
        }
        client.options("/cache", headers = multiMapOf(
            "origin" to "example.org",
            "access-control-request-method" to "GET",
            "access-control-request-headers" to "header1,header2"
        )).apply {
            assertEquals(NO_CONTENT, status)
            assert(bodyString().isEmpty())
            assertEquals("10", headers["access-control-max-age"])
        }
    }

    @Test fun `CORS pre flight with not allowed method`() {
        val result = client.options("/only/post", headers = multiMapOf(
            "origin" to "example.org",
            "access-control-request-method" to "GET"
        ))
        assertEquals(FORBIDDEN, result.status)
        assertEquals("Not allowed method: GET", result.body)
    }

    @Test fun `CORS pre flight with not allowed headers`() {
        val result = client.options("/allowed/headers", headers = multiMapOf(
            "origin" to "example.org",
            "access-control-request-method" to "GET",
            "access-control-request-headers" to "header1,header2"
        ))
        assertEquals(FORBIDDEN, result.status)
        assertEquals("Not allowed headers", result.body)
    }

    @Test fun `CORS pre flight with allowed headers`() {
        val result = client.options("/allowed/headers", headers = multiMapOf(
            "origin" to "example.org",
            "access-control-request-method" to "GET",
            "access-control-request-headers" to "head"
        ))
        assertEquals(NO_CONTENT, result.status)
        assert(result.bodyString().isEmpty())
    }
}
