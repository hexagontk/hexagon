package com.hexagontk.rest.tools

import com.hexagontk.core.media.TEXT_PLAIN
import com.hexagontk.http.client.jetty.JettyHttpClient
import com.hexagontk.http.model.HttpMethod.POST
import com.hexagontk.http.model.HttpMethod.PUT
import com.hexagontk.http.model.HttpResponsePort
import com.hexagontk.http.model.OK_200
import com.hexagontk.http.server.jetty.JettyServletHttpServer
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
internal class HttpClientToolTest {
    private val server: HttpServerTool = HttpServerTool(JettyServletHttpServer())
    private val text = textContentType

    @BeforeAll fun `Set up mock services`() {
        SerializationManager.formats = setOf(Json)
        server.start()
    }

    @AfterAll fun `Shut down mock services`() {
        server.stop()
    }

    @Test fun `Check all HTTP methods`() {
        server.path {
            before("*") {
                ok("$method $path ${request.headers}", contentType = text)
            }

            on(setOf(PUT, POST), "/bye/mike/{id}") {
                ok("$method $path ${request.bodyString()} ${request.headers}", contentType = text)
            }
        }

        val url = "http://localhost:${server.runtimePort}"
        val adapter = JettyHttpClient()
        val headers = mapOf("alfa" to "beta", "charlie" to listOf("delta", "echo"))
        val params = mapOf("id" to 9)
        val client = HttpClientTool(adapter, url, TEXT_PLAIN, headers = headers)

        client.get("/hello/mike/{id}" to params).assertBody("GET /hello/mike/9", headers)
        client.put("/hello/mike/{id}" to params).assertBody("PUT /hello/mike/9", headers)
        client.post("/hello/mike/{id}" to params).assertBody("POST /hello/mike/9", headers)
        client.options("/hello/mike/{id}" to params).assertBody("OPTIONS /hello/mike/9", headers)
        client.delete("/hello/mike/{id}" to params).assertBody("DELETE /hello/mike/9", headers)
        client.patch("/hello/mike/{id}" to params).assertBody("PATCH /hello/mike/9", headers)
        client.trace("/hello/mike/{id}" to params).assertBody("TRACE /hello/mike/9", headers)

        client.put("/bye/mike/{id}" to params) { "putLambdaBody" }
            .assertBody("PUT /bye/mike/9 putLambdaBody", headers)
        client.post("/bye/mike/{id}" to params) { "postLambdaBody" }
            .assertBody("POST /bye/mike/9 postLambdaBody", headers)

        client.request {
            get("/hello/mike").assertBody("GET /hello/mike", headers)
            put("/hello/mike").assertBody("PUT /hello/mike", headers)
            post("/hello/mike").assertBody("POST /hello/mike", headers)
            options("/hello/mike").assertBody("OPTIONS /hello/mike", headers)
            delete("/hello/mike").assertBody("DELETE /hello/mike", headers)
            patch("/hello/mike").assertBody("PATCH /hello/mike", headers)
            trace("/hello/mike").assertBody("TRACE /hello/mike", headers)

            assertEquals(text, contentType)
            assertBodyContains("TRACE /hello/mike")
            assertTrue(cookies.isEmpty())
            assertTrue(attributes.isEmpty())
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
