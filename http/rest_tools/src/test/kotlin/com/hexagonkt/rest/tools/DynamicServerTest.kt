package com.hexagonkt.rest.tools

import com.hexagonkt.core.logging.info
import com.hexagonkt.core.media.APPLICATION_JSON
import com.hexagonkt.core.media.TEXT_PLAIN
import com.hexagonkt.core.require
import com.hexagonkt.http.client.jetty.JettyClientAdapter
import com.hexagonkt.http.handlers.BeforeHandler
import com.hexagonkt.http.handlers.PathHandler
import com.hexagonkt.http.model.ContentType
import com.hexagonkt.http.model.OK_200
import com.hexagonkt.http.handlers.path
import com.hexagonkt.http.model.HttpMethod.*
import com.hexagonkt.http.model.HttpResponsePort
import com.hexagonkt.http.model.HttpStatusType.SUCCESS
import com.hexagonkt.http.server.HttpServerSettings
import com.hexagonkt.http.server.jetty.JettyServletAdapter
import com.hexagonkt.rest.bodyMap
import com.hexagonkt.serialization.SerializationManager
import com.hexagonkt.serialization.jackson.json.Json
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
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
        dynamicServer.path {
            get("/hello/{name}") {
                val name = pathParameters["name"]

                ok("Hello, $name!", contentType = ContentType(TEXT_PLAIN))
            }
        }

        Http(JettyClientAdapter(), "http://localhost:${dynamicServer.runtimePort}").request {
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
        val recordCallback = RecordCallback()
        val recordHandler = BeforeHandler("*", recordCallback)
        val http = Http(
            adapter,
            httpHeaders = headers,
            handler = PathHandler(recordHandler, Http.serializeHandler)
        )

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

        assertEquals(14, recordCallback.calls.size)
        assertEquals(GET, recordCallback.calls[0].request.method)
        assertEquals("http://localhost:$port/hello/mike", recordCallback.calls[0].request.path)
        assertEquals(OPTIONS, recordCallback.calls[6].request.method)
        assertEquals("http://localhost:$port/hello/mike", recordCallback.calls[6].request.path)
        assertEquals(TRACE, recordCallback.calls[12].request.method)
        assertEquals("http://localhost:$port/hello/mike", recordCallback.calls[12].request.path)
    }

    @Test fun `Check all HTTP methods`() {
        dynamicServer.path {
            before("*") {
                ok("$method $path ${request.headers}", contentType = ContentType(TEXT_PLAIN))
            }
        }

        val url = "http://localhost:${dynamicServer.runtimePort}"
        val adapter = JettyClientAdapter()
        val headers = mapOf("alfa" to "beta", "charlie" to listOf("delta", "echo"))
        val http = Http(adapter, url, httpHeaders = headers)

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

        http.request {
            get("/hello/mike").assertBody("GET /hello/mike", headers)
            get().assertBody("GET / ", headers)

            put("/hello/mike").assertBody("PUT /hello/mike", headers)
            put().assertBody("PUT / ", headers)

            post("/hello/mike").assertBody("POST /hello/mike", headers)
            post().assertBody("POST / ", headers)

            options("/hello/mike").assertBody("OPTIONS /hello/mike", headers)
            options().assertBody("OPTIONS / ", headers)

            delete("/hello/mike").assertBody("DELETE /hello/mike", headers)
            delete().assertBody("DELETE / ", headers)

            patch("/hello/mike").assertBody("PATCH /hello/mike", headers)
            patch().assertBody("PATCH / ", headers)

            trace("/hello/mike").assertBody("TRACE /hello/mike", headers)
            trace().assertBody("TRACE / ", headers)
        }
    }

    @Suppress("unused") // Object only used for serialization
    @Test fun `Check HTTP helper`() {
        SerializationManager.formats = linkedSetOf(Json)

        val settings = HttpServerSettings(bindPort = 0)
        val server = DynamicServer(JettyServletAdapter(), settings).apply(DynamicServer::start)
        val headers = mapOf("alfa" to "beta", "charlie" to listOf("delta", "echo"))
        val text = ContentType(TEXT_PLAIN)
        val json = ContentType(APPLICATION_JSON)
        val binding = server.binding.toString()
        val adapter = JettyClientAdapter()
        val http = Http(adapter, url = binding, httpHeaders = headers, httpContentType = json)

        server.path {
            before("*") {
                ok("$method $path ${request.headers}", contentType = text)
            }

            put("/data/{id}") {
                val id = pathParameters.require("id")
                val data = request.bodyMap()
                val content = mapOf(id to data)

                ok(content, contentType = json)
            }
        }

        http.request {
            put("/data/{id}" to mapOf("id" to 102030)) {
                object {
                    val title = "Casino Royale"
                    val tags = listOf("007", "action")
                }
            }

            assertOk()
            response.body.info("BODY: ")
            response.contentType.info("CONTENT TYPE: ")
        }

        http.request {
            get("/hello/mike")
            assertBody("GET /hello/mike", headers)
            get()
            assertBody("GET / ", headers)

            put("/hello/mike")
            assertBody("PUT /hello/mike", headers)
            put()
            assertBody("PUT / ", headers)

            post("/hello/mike")
            assertBody("POST /hello/mike", headers)
            post()
            assertBody("POST / ", headers)

            options("/hello/mike")
            assertBody("OPTIONS /hello/mike", headers)
            options()
            assertBody("OPTIONS / ", headers)

            delete("/hello/mike")
            assertBody("DELETE /hello/mike", headers)
            delete()
            assertBody("DELETE / ", headers)

            patch("/hello/mike")
            assertBody("PATCH /hello/mike", headers)
            patch()
            assertBody("PATCH / ", headers)

            trace("/hello/mike")
            assertBody("TRACE /hello/mike", headers)
            trace()
            assertBody("TRACE / ", headers)
        }
    }

    private fun Http.assertBody(expectedBody: String, checkedHeaders: Map<String, *>) {
        assertOk()
        assertSuccess()
        assertStatus(OK_200)
        assertStatus(SUCCESS)
        assertBodyContains(expectedBody)

        for ((k, v) in checkedHeaders.entries) {
            assertBodyContains(k, v.toString())
        }
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
