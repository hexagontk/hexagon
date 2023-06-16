package com.hexagonkt.http.test

import com.hexagonkt.core.media.TEXT_PLAIN
import com.hexagonkt.http.client.jetty.JettyClientAdapter
import com.hexagonkt.http.model.ContentType
import com.hexagonkt.http.model.OK_200
import com.hexagonkt.http.handlers.path
import com.hexagonkt.http.model.HttpResponsePort
import com.hexagonkt.http.server.jetty.JettyServletAdapter
import com.hexagonkt.http.test.Http.Companion.http
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import kotlin.test.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(PER_CLASS)
class DynamicServerTest {
    private val dynamicServer: DynamicServer = DynamicServer(JettyServletAdapter())

    @BeforeAll fun `Set up mock services`() {
        dynamicServer.start()
    }

    @AfterAll fun `Shut down mock services`() {
        dynamicServer.stop()
    }

    @Test fun `Do HTTP requests`() {
        dynamicServer.path = path {
            get("/hello/{name}") {
                val name = pathParameters["name"]

                ok("Hello, $name!", contentType = ContentType(TEXT_PLAIN))
            }
        }

        http(JettyClientAdapter(), "http://localhost:${dynamicServer.runtimePort}") {
            get("/hello/mike")
            assertEquals(OK_200, response.status)
        }
    }

    @Test fun `Mock HTTP response`() {
        dynamicServer.path = path {
            get("/foo") {
                ok("dynamic")
            }
        }

        Http(JettyClientAdapter(), "http://localhost:${dynamicServer.runtimePort}").request {
            get("/foo")
            assertEquals(OK_200, response.status)
            assertEquals("dynamic", response.body)
            dynamicServer.path = path {
                get("/foo") {
                    ok("changed")
                }
            }
            get("/foo")
            assertEquals(OK_200, response.status)
            assertEquals(OK_200, response.status)
            assertEquals("changed", response.body)
            assertEquals("changed", response.body)
        }
    }

    @Test fun `Check all HTTP methods (absolute path)`() {
        dynamicServer.path = path {
            on("*") {
                ok("$method $path ${request.headers}", contentType = ContentType(TEXT_PLAIN))
            }
        }

        val port = dynamicServer.runtimePort
        val adapter = JettyClientAdapter()
        val headers = mapOf("alfa" to "beta", "charlie" to listOf("delta", "echo"))
        val http = Http(adapter, headers = headers)

        http.get("http://localhost:$port/hello/mike").assertBody("GET /hello/mike", headers)
        http.get("http://localhost:$port").assertBody("GET / ", headers)

        http.put("http://localhost:$port/hello/mike").assertBody("PUT /hello/mike", headers)
        http.put("http://localhost:$port").assertBody("PUT / ", headers)

        http.post("http://localhost:$port/hello/mike").assertBody("POST /hello/mike", headers)
        http.post("http://localhost:$port").assertBody("POST / ", headers)

        http.options("http://localhost:$port/hello/mike").assertBody("OPTIONS /hello/mike", headers)
        http.options("http://localhost:$port").assertBody("OPTIONS / ", headers)

        http.delete("http://localhost:$port/hello/mike").assertBody("DELETE /hello/mike", headers)
        http.delete("http://localhost:$port").assertBody("DELETE / ", headers)

        http.patch("http://localhost:$port/hello/mike").assertBody("PATCH /hello/mike", headers)
        http.patch("http://localhost:$port").assertBody("PATCH / ", headers)

        http.trace("http://localhost:$port/hello/mike").assertBody("TRACE /hello/mike", headers)
        http.trace("http://localhost:$port").assertBody("TRACE / ", headers)
    }

    @Test fun `Check all HTTP methods`() {
        dynamicServer.path = path {
            on("*") {
                ok("$method $path ${request.headers}", contentType = ContentType(TEXT_PLAIN))
            }
        }

        val url = "http://localhost:${dynamicServer.runtimePort}"
        val adapter = JettyClientAdapter()
        val headers = mapOf("alfa" to "beta", "charlie" to listOf("delta", "echo"))
        val http = Http(adapter, url, headers = headers)

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

    private fun HttpResponsePort.assertBody(
        expectedBody: String, checkedHeaders: Map<String, *>) {

        val bodyString = bodyString()

        assertEquals(OK_200, status)
        assertTrue(bodyString.startsWith(expectedBody))

        for (entry in checkedHeaders.entries) {
            assertTrue(bodyString.contains(entry.key))
            assertTrue(bodyString.contains(entry.value.toString()))
        }
    }
}
