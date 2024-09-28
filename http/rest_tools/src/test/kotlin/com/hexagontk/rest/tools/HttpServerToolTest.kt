package com.hexagontk.rest.tools

import com.hexagontk.core.info
import com.hexagontk.core.media.TEXT_PLAIN
import com.hexagontk.core.require
import com.hexagontk.http.client.jetty.JettyHttpClient
import com.hexagontk.http.handlers.FilterHandler
import com.hexagontk.http.handlers.PathHandler
import com.hexagontk.http.model.OK_200
import com.hexagontk.http.handlers.path
import com.hexagontk.http.model.HttpMethod.*
import com.hexagontk.http.model.HttpResponsePort
import com.hexagontk.http.model.HttpStatusType.SUCCESS
import com.hexagontk.http.server.HttpServerSettings
import com.hexagontk.http.server.jetty.JettyServletHttpServer
import com.hexagontk.rest.bodyMap
import com.hexagontk.rest.jsonContentType
import com.hexagontk.rest.textContentType
import com.hexagontk.serialization.SerializationManager
import com.hexagontk.serialization.jackson.json.Json
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(PER_CLASS)
class HttpServerToolTest {
    private val dynamicServer: HttpServerTool = HttpServerTool(JettyServletHttpServer())

    @BeforeAll fun `Set up mock services`() {
        SerializationManager.formats = setOf(Json)
        dynamicServer.start()
    }

    @AfterAll fun `Shut down mock services`() {
        dynamicServer.stop()
    }

    @Test fun `Do HTTP requests`() {
        dynamicServer.path {
            get("/hello/{name}") {
                val name = pathParameters["name"]

                ok("Hello, $name!", contentType = textContentType)
            }
        }

        val url = "http://localhost:${dynamicServer.runtimePort}"
        HttpClientTool(JettyHttpClient(), url).request {
            start()
            get("/hello/mike")
            assertOk()
        }
    }

    @Test fun `Mock HTTP response`() {
        dynamicServer.path = path {
            get("/foo") {
                ok("dynamic")
            }
        }

        val url = "http://localhost:${dynamicServer.runtimePort}"
        HttpClientTool(JettyHttpClient(), url).request {
            get("/foo")
            assertOk()
            assertBody("dynamic")
            dynamicServer.path = path {
                get("/foo") {
                    ok("changed")
                }
            }
            stop()
            get("/foo")
            assertOk()
            assertBody("changed")
        }
    }

    @Test fun `Check all HTTP methods (absolute path)`() {
        dynamicServer.path = path {
            on("*") {
                ok("$method $path ${request.headers}", contentType = textContentType)
            }
        }

        val port = dynamicServer.runtimePort
        val adapter = JettyHttpClient()
        val headers = mapOf("alfa" to "beta", "charlie" to listOf("delta", "echo"))
        val recordCallback = RecordCallback()
        val recordHandler = FilterHandler("*", recordCallback)
        val http = HttpClientTool(
            adapter,
            httpHeaders = headers,
            handler = PathHandler(recordHandler, HttpClientTool.serializeHandler)
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
                ok("$method $path ${request.headers}", contentType = textContentType)
            }
        }

        val url = "http://localhost:${dynamicServer.runtimePort}"
        val adapter = JettyHttpClient()
        val headers = mapOf("alfa" to "beta", "charlie" to listOf("delta", "echo"))
        val http = HttpClientTool(adapter, url, httpHeaders = headers)

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
        val serverAdapter = JettyServletHttpServer()
        val server = HttpServerTool(serverAdapter, settings).apply(HttpServerTool::start)
        val headers = mapOf("alfa" to "beta", "charlie" to listOf("delta", "echo"))
        val binding = server.binding.toString()
        val adapter = JettyHttpClient()
        val http = HttpClientTool(adapter, binding, jsonContentType, httpHeaders = headers)

        server.path {
            before("*") {
                ok("$method $path ${request.headers}", contentType = textContentType)
            }

            put("/data/{id}") {
                val id = pathParameters.require("id")
                val data = request.bodyMap()
                val content = mapOf(id to data)

                ok(content, contentType = jsonContentType)
            }
        }

        http.request {
            put("/data/102030") {
                object {
                    val title = "Casino Royale"
                    val tags = listOf("007", "action")
                }
            }

            assertOk()
            response.body.info("BODY: ")
            response.contentType.info("CONTENT TYPE: ")

            post("/data/102039") {
                object {
                    val title = "Batman Begins"
                    val tags = listOf("DC", "action")
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

    private fun HttpClientTool.assertBody(expectedBody: String, checkedHeaders: Map<String, *>) {
        assertOk()
        assertSuccess()
        assertStatus(OK_200)
        assertStatus(SUCCESS)
        assertContentType(textContentType)
        assertContentType(TEXT_PLAIN)
        assertBodyContains(expectedBody)

        assertEquals(jsonContentType, request.contentType)
        assertEquals(OK_200, status)

        for ((k, v) in checkedHeaders.entries) {
            assertBodyContains(k, v.toString())
        }
    }

    private fun HttpResponsePort.assertBody(expectedBody: String, checkedHeaders: Map<String, *>) {
        val bodyString = bodyString()

        assertEquals(OK_200, status)
        assertTrue(bodyString.startsWith(expectedBody))

        for (entry in checkedHeaders.entries) {
            assertTrue(bodyString.contains(entry.key))
            assertTrue(bodyString.contains(entry.value.toString()))
        }
    }
}
