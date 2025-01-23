package com.hexagontk.http.test.examples

import com.hexagontk.core.urlOf
import com.hexagontk.http.client.HttpClientPort
import com.hexagontk.http.model.HttpRequest
import com.hexagontk.http.model.*
import com.hexagontk.http.model.HttpMethod.POST
import com.hexagontk.http.model.OK_200
import com.hexagontk.http.server.HttpServerPort
import com.hexagontk.http.server.HttpServerSettings
import com.hexagontk.http.handlers.PathHandler
import com.hexagontk.http.handlers.HttpHandler
import com.hexagontk.http.handlers.path
import com.hexagontk.http.test.BaseTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import kotlin.test.assertEquals

@TestInstance(PER_CLASS)
@Suppress("FunctionName") // This class's functions are intended to be used only in tests
abstract class MultipartTest(
    final override val clientAdapter: () -> HttpClientPort,
    final override val serverAdapter: () -> HttpServerPort,
    final override val serverSettings: HttpServerSettings = HttpServerSettings(),
) : BaseTest() {

    // multipart
    private val path: PathHandler = path {

        // Serve `public` resources folder on `/*`
        post("/multipart") {
            val headers = parts.first().let { p ->
                val name = p.name
                val bodyString = p.bodyString()
                val size = p.size.toString()
                Headers(
                    Header("name", name),
                    Header("body", bodyString),
                    Header("size", size),
                )
            }

            ok(headers = headers)
        }

        post("/file") {
            val part = parts.first()
            val content = part.bodyString()
            val submittedFile = part.submittedFileName ?: ""
            ok(content, headers = response.headers + Header("submitted-file", submittedFile))
        }

        post("/form") {
            fun serializeMap(map: Parameters): List<String> = listOf(
                map.all.entries.joinToString("\n") { (k, v) ->
                    "$k:${v.joinToString(",") { it.text }}"
                }
            )

            val queryParams = serializeMap(queryParameters).map { Parameter("query-params", it) }
            val formParams = serializeMap(formParameters).map { Parameter("form-params", it) }

            ok(headers = response.headers + Headers(queryParams) + Headers(formParams))
        }
    }
    // multipart

    override val handler: HttpHandler = path

    @Test fun `Parameters are separated from each other`() {
        val server = server()
        val client = client(server)

        val parts = listOf(HttpPart("name", "value"))
        val response = client.send(
            HttpRequest(POST, path = "/form?queryName=queryValue", parts = parts)
        )
        assertEquals("queryName:queryValue", response.headers["query-params"]?.value)
        assert(!(response.headers["query-params"]?.text?.contains("name:value") ?: true))
        assert(response.headers["form-params"]?.text?.contains("name:value") ?: false)
        assert(!(response.headers["form-params"]?.text?.contains("queryName:queryValue") ?: true))

        client.stop()
        server.stop()
    }

    @Test fun `Sending multi part content works properly`() {
        val server = server()
        val client = client(server)

        // clientForm
        val parts = listOf(HttpPart("name", "value"))
        val response = client.send(HttpRequest(POST, path = "/multipart", parts = parts))
        // clientForm
        val expectedHeaders = Headers(
            Header("name", "name"),
            Header("body", "value"),
            Header("size", "5"),
        )
        expectedHeaders.forEach {
            assertEquals(it.value, response.headers[it.name]?.text)
        }

        client.stop()
        server.stop()
    }

    @Test fun `Sending files works properly`() {
        val server = server()
        val client = client(server)

        // clientFile
        val stream = urlOf("classpath:assets/index.html").readBytes()
        val parts = listOf(HttpPart("file", stream, "index.html"))
        val response = client.send(HttpRequest(POST, path = "/file", parts = parts))
        // clientFile
        assertEquals("index.html", response.headers["submitted-file"]?.value)
        assertResponseContains(response, OK_200, "<!DOCTYPE html>", "<title>Hexagon</title>", "</html>")

        client.stop()
        server.stop()
    }
}
