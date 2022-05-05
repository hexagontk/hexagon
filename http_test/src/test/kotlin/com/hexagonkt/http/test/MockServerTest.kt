package com.hexagonkt.http.test

import com.hexagonkt.core.media.TextMedia.PLAIN
import com.hexagonkt.http.client.jetty.JettyClientAdapter
import com.hexagonkt.http.client.model.HttpClientResponse
import com.hexagonkt.http.model.ContentType
import com.hexagonkt.http.model.SuccessStatus.OK
import com.hexagonkt.http.server.handlers.path
import com.hexagonkt.http.server.jetty.JettyServletAdapter
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(PER_CLASS)
class MockServerTest {
    private val mockServer: MockServer = MockServer(JettyServletAdapter())

    @BeforeAll fun `Set up mock services`() {
        mockServer.server.start()
    }

    @AfterAll fun `Shut down mock services`() {
        mockServer.server.stop()
    }

    @Test fun `Do HTTP requests`() {
        mockServer.path = path {
            get("/hello/{name}") {
                val name = pathParameters["name"]

                ok("Hello, $name!", contentType = ContentType(PLAIN))
            }
        }

        val http = Http("http://localhost:${mockServer.server.runtimePort}", JettyClientAdapter())
        http.get("/hello/mike")
        assertEquals(OK, http.response.status)
    }

    @Test fun `Mock HTTP response`() {
        mockServer.path = path {
            get("/foo") {
                ok("dynamic")
            }
        }

        val http = Http("http://localhost:${mockServer.server.runtimePort}", JettyClientAdapter())
        http.get("/foo")
        assertEquals(OK, http.responseOrNull?.status)
        assertEquals("dynamic", http.responseOrNull?.body)
        mockServer.path = path {
            get("/foo") {
                ok("changed")
            }
        }
        http.get("/foo")
        assertEquals(OK, http.responseOrNull?.status)
        assertEquals(OK, http.response.status)
        assertEquals("changed", http.responseOrNull?.body)
        assertEquals("changed", http.response.body)
    }

    @Test fun `Check all HTTP methods`() {
        mockServer.path = path {
            on("*") {
                ok("$method $path ${request.headers}", contentType = ContentType(PLAIN))
            }
        }

        val url = "http://localhost:${mockServer.server.runtimePort}"
        val adapter = JettyClientAdapter()
        val headers = mapOf("alfa" to "beta", "charlie" to listOf("delta", "echo"))
        val http = Http(url, adapter, headers = headers)

        http.get("/hello/mike").assertBody("GET /hello/mike", headers)
        http.get().assertBody("GET / ", headers)

        http.put("/hello/mike").assertBody("PUT /hello/mike", headers)
        http.put().assertBody("PUT / ", headers)

        http.post("/hello/mike").assertBody("POST /hello/mike", headers)
        http.post().assertBody("POST / ", headers)

        http.options("/hello/mike").assertBody("OPTIONS /hello/mike", headers)
        http.options().assertBody("OPTIONS / ", headers)

        http.delete("/hello/mike").assertBody("DELETE /hello/mike", headers)
        http.delete().assertBody("DELETE / ", headers)

        http.patch("/hello/mike").assertBody("PATCH /hello/mike", headers)
        http.patch().assertBody("PATCH / ", headers)

        http.trace("/hello/mike").assertBody("TRACE /hello/mike", headers)
        http.trace().assertBody("TRACE / ", headers)
    }

    private fun HttpClientResponse.assertBody(
        expectedBody: String, checkedHeaders: Map<String, *>) {

        val bodyString = bodyString()

        assertEquals(OK, status)
        assertTrue(bodyString.startsWith(expectedBody))

        for (entry in checkedHeaders.entries) {
            assertTrue(bodyString.contains(entry.key))
            assertTrue(bodyString.contains(entry.value.toString()))
        }
    }
}
