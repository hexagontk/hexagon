package com.hexagontk.http.test.examples

import com.hexagontk.http.client.HttpClientPort
import com.hexagontk.http.model.HttpMethod.POST
import com.hexagontk.http.server.*
import com.hexagontk.http.handlers.HandlerBuilder
import com.hexagontk.http.handlers.PathHandler
import com.hexagontk.http.handlers.HttpHandler
import com.hexagontk.http.handlers.path
import com.hexagontk.http.model.*
import com.hexagontk.http.server.callbacks.CorsCallback
import com.hexagontk.http.server.handlers.CorsHandler
import com.hexagontk.http.test.BaseTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Suppress("FunctionName") // This class's functions are intended to be used only in tests
abstract class CorsTest(
    final override val clientAdapter: () -> HttpClientPort,
    final override val serverAdapter: () -> HttpServerPort,
    final override val serverSettings: HttpServerSettings = HttpServerSettings(),
) : BaseTest() {

    // cors
    val path: PathHandler = path {
        corsPath("/default", CorsHandler(CorsCallback()))
        corsPath("/example/org", CorsHandler(allowedOrigin = "example.org"))
        corsPath("/no/credentials", CorsHandler(supportCredentials = false))
        corsPath("/only/post", CorsHandler(allowedMethods = setOf(POST)))
        corsPath("/cache", CorsHandler(preFlightMaxAge = 10))
        corsPath("/exposed/headers", CorsHandler(exposedHeaders = setOf("head")))
        corsPath("/allowed/headers", CorsHandler(allowedHeaders = setOf("head")))
    }

    private fun HandlerBuilder.corsPath(path: String, cors: CorsHandler) {
        path(path) {
            // CORS settings can change for different routes
            use(cors)

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

    override val handler: HttpHandler = path

    @Test fun `Request without origin continues as non CORS`() {
        val server = server()
        val client = client(server)

        listOf(
            client.get("/default"),
            client.get("/default/path")
        ).forEach {
            assertEquals(OK_200, it.status)
            assertEquals("GET", it.body)
        }

        client.stop()
        server.stop()
    }

    @Test fun `Request with not allowed origin is forbidden`() {
        val server = server()
        val client = client(server)

        listOf(
            client.get("/example/org", Headers(Header("origin", "other.com"))),
            client.get("/example/org/path", Headers(Header("origin", "other.com")))
        ).forEach {
            assertEquals(FORBIDDEN_403, it.status)
            assertEquals("Not allowed origin: other.com", it.body)
        }

        client.stop()
        server.stop()
    }

    @Test fun `Allowed origin is returned properly`() {
        val server = server()
        val client = client(server)

        listOf(
            client.get("/no/credentials", Headers(Header("origin", "other.com"))),
            client.get("/no/credentials/path", Headers(Header("origin", "other.com")))
        ).forEach {
            assertEquals(OK_200, it.status)
            assertEquals("GET", it.body)
            assertEquals("*", it.headers["access-control-allow-origin"]?.value)
            assert(it.headers["vary"]?.text?.contains("Origin")?.not() ?: true)
        }

        client.stop()
        server.stop()
    }

    @Test fun `Simple CORS request`() {
        val server = server()
        val client = client(server)

        val result = client.get("/default", Headers(Header("origin", "example.org")))
        assertEquals(OK_200, result.status)
        assertEquals("example.org", result.headers["access-control-allow-origin"]?.value)
        assert(result.headers["vary"]?.text?.contains("Origin") ?: false)
        assertEquals("true", result.headers["access-control-allow-credentials"]?.value)

        client.stop()
        server.stop()
    }

    @Test fun `Simple CORS request with not allowed method`() {
        val server = server()
        val client = client(server)

        val result = client.get("/only/post", Headers(Header("origin", "example.org")))
        assertEquals(FORBIDDEN_403, result.status)
        assertEquals("Not allowed method: GET", result.body)

        client.stop()
        server.stop()
    }

    @Test fun `Simple CORS request with exposed headers`() {
        val server = server()
        val client = client(server)

        val result = client.get("/exposed/headers", Headers(
            Header("origin", "example.org"),
            Header("head", "exposed header"),
        ))
        assertEquals(OK_200, result.status)
        assertEquals("example.org", result.headers["access-control-allow-origin"]?.value)
        assert(result.headers["vary"]?.text?.contains("Origin") ?: false)
        assertEquals("true", result.headers["access-control-allow-credentials"]?.value)
        assertEquals("head", result.headers["access-control-expose-headers"]?.value)

        client.stop()
        server.stop()
    }

    @Test fun `CORS pre-flight with empty request method`() {
        val server = server()
        val client = client(server)

        val result = client.options("/default", headers = Headers(
            Header("origin", "example.org"),
            Header("access-control-request-method"),
        ))
        assertEquals(FORBIDDEN_403, result.status)
        assertEquals("access-control-request-method required header not found", result.body)

        client.stop()
        server.stop()
    }

    @Test fun `CORS pre-flight without request method`() {
        val server = server()
        val client = client(server)

        val headers = Headers(Header("origin", "example.org"))
        val result = client.options("/default", headers = headers)
        assertEquals(FORBIDDEN_403, result.status)
        assertEquals("access-control-request-method required header not found", result.body)

        client.stop()
        server.stop()
    }

    @Test fun `CORS pre-flight`() {
        val server = server()
        val client = client(server)

        val result = client.options("/default", headers = Headers(
            Header("origin", "example.org"),
            Header("access-control-request-method", "GET"),
        ))
        assertEquals("example.org", result.headers["access-control-allow-origin"]?.value)
        assertEquals(NO_CONTENT_204, result.status)
        assert(result.bodyString().isEmpty())

        client.stop()
        server.stop()
    }

    @Test fun `CORS pre-flight with mismatched origin`() {
        val server = server()
        val client = client(server)

        listOf(
            client.options("/example/org", headers = Headers(
                Header("origin", "other.com"),
                Header("access-control-request-method", "GET"),
            )),
            client.options("/example/org/path", headers = Headers(
                Header("origin", "other.com"),
                Header("access-control-request-method", "GET"),
            ))
        ).forEach {
            assertEquals(FORBIDDEN_403, it.status)
            assertNull(it.headers["access-control-allow-origin"])
            assertEquals("Not allowed origin: other.com", it.body)
        }

        client.stop()
        server.stop()
    }

    @Test fun `CORS pre-flight without origin`() {
        val server = server()
        val client = client(server)

        listOf(
            client.options("/example/org", headers = Headers(
                Header("access-control-request-method", "GET"),
            )),
            client.options("/example/org/path", headers = Headers(
                Header("access-control-request-method", "GET"),
            ))
        ).forEach {
            assertEquals(FORBIDDEN_403, it.status)
            assertNull(it.headers["access-control-allow-origin"])
            assertEquals("Forbidden pre-flight request", it.body)
        }

        client.stop()
        server.stop()
    }

    @Test fun `Allowed CORS pre-flight without origin`() {
        val server = server()
        val client = client(server)

        listOf(
            client.options("/default", headers = Headers(
                Header("access-control-request-method", "GET"),
            )),
        ).forEach {
            assertEquals(NO_CONTENT_204, it.status)
            assertNull(it.headers["access-control-allow-origin"])
            assert(it.bodyString().isEmpty())
        }

        client.stop()
        server.stop()
    }

    @Test fun `CORS full pre-flight`() {
        val server = server()
        val client = client(server)

        client.options("/default", headers = Headers(
            Header("origin", "example.org"),
            Header("access-control-request-method", "GET"),
            Header("access-control-request-headers", "header1,header2"),
        )).apply {
            assertEquals(NO_CONTENT_204, status)
            assertEquals("example.org", headers["access-control-allow-origin"]?.value)
            assert(bodyString().isEmpty())
        }
        client.options("/cache", headers = Headers(
            Header("origin", "example.org"),
            Header("access-control-request-method", "GET"),
            Header("access-control-request-headers", "header1,header2"),
        )).apply {
            assertEquals(NO_CONTENT_204, status)
            assert(bodyString().isEmpty())
            assertEquals("example.org", headers["access-control-allow-origin"]?.value)
            assertEquals("10", headers["access-control-max-age"]?.value)
        }

        client.stop()
        server.stop()
    }

    @Test fun `CORS pre-flight with not allowed method`() {
        val server = server()
        val client = client(server)

        val result = client.options("/only/post", headers = Headers(
            Header("origin", "example.org"),
            Header("access-control-request-method", "GET"),
        ))
        assertEquals(FORBIDDEN_403, result.status)
        assertEquals("Not allowed method: GET", result.body)

        client.stop()
        server.stop()
    }

    @Test fun `CORS pre-flight with not allowed headers`() {
        val server = server()
        val client = client(server)

        val result = client.options("/allowed/headers", headers = Headers(
            Header("origin", "example.org"),
            Header("access-control-request-method", "GET"),
            Header("access-control-request-headers", "header1,header2"),
        ))
        assertEquals(FORBIDDEN_403, result.status)
        assertEquals("Not allowed headers", result.body)

        client.stop()
        server.stop()
    }

    @Test fun `CORS pre-flight with allowed headers`() {
        val server = server()
        val client = client(server)

        val result = client.options("/allowed/headers", headers = Headers(
            Header("origin", "example.org"),
            Header("access-control-request-method", "GET"),
            Header("access-control-request-headers", "head"),
        ))
        assertEquals(NO_CONTENT_204, result.status)
        assert(result.bodyString().isEmpty())

        client.stop()
        server.stop()
    }
}
