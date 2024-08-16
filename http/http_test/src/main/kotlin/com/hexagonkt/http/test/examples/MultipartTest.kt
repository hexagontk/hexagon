package com.hexagonkt.http.test.examples

import com.hexagonkt.core.urlOf
import com.hexagonkt.http.client.HttpClientPort
import com.hexagonkt.http.model.HttpRequest
import com.hexagonkt.http.model.*
import com.hexagonkt.http.model.NOT_FOUND_404
import com.hexagonkt.http.model.HttpMethod.GET
import com.hexagonkt.http.model.HttpMethod.POST
import com.hexagonkt.http.model.OK_200
import com.hexagonkt.http.server.HttpServerPort
import com.hexagonkt.http.server.HttpServerSettings
import com.hexagonkt.http.server.callbacks.FileCallback
import com.hexagonkt.http.server.callbacks.UrlCallback
import com.hexagonkt.http.handlers.PathHandler
import com.hexagonkt.http.handlers.HttpHandler
import com.hexagonkt.http.handlers.path
import com.hexagonkt.http.test.BaseTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.io.File
import kotlin.test.assertEquals

@TestInstance(PER_CLASS)
@Suppress("FunctionName") // This class's functions are intended to be used only in tests
abstract class MultipartTest(
    final override val clientAdapter: () -> HttpClientPort,
    final override val serverAdapter: () -> HttpServerPort,
    final override val serverSettings: HttpServerSettings = HttpServerSettings(),
) : BaseTest() {

    private val directory = File("http_test/src/main/resources/assets").let {
        if (it.exists()) it.path
        else "../http_test/src/main/resources/assets"
    }

    // TODO Remove unnecessary handlers
    // files
    private val path: PathHandler = path {

        // Serve `public` resources folder on `/*`
        after(
            methods = setOf(GET),
            pattern = "/*",
            status = NOT_FOUND_404,
            callback = UrlCallback(urlOf("classpath:public"))
        )

        path("/static") {
            get("/files/*", UrlCallback(urlOf("classpath:assets")))
            get("/resources/*", FileCallback(File(directory)))
        }

        get("/html/*", UrlCallback(urlOf("classpath:assets"))) // Serve `assets` files on `/html/*`
        get("/pub/*", FileCallback(File(directory))) // Serve `test` folder on `/pub/*`

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
            fun <T : HttpField> serializeMap(map: Collection<T>): List<String> = listOf(
                map.joinToString("\n") { "${it.name}:${it.values.joinToString(",")}" }
            )

            val queryParams = serializeMap(queryParameters.values)
            val formParams = serializeMap(formParameters.values)
            val headers =
                Headers(Header("query-params", queryParams), Header("form-params", formParams))

            ok(headers = response.headers + headers)
        }
    }
    // files

    override val handler: HttpHandler = path

    @Test fun `Parameters are separated from each other`() {
        val parts = listOf(HttpPart("name", "value"))
        val response = client.send(
            HttpRequest(POST, path = "/form?queryName=queryValue", parts = parts)
        )
        assertEquals("queryName:queryValue", response.headers["query-params"]?.value)
        assert(!(response.headers["query-params"]?.string()?.contains("name:value") ?: true))
        assert(response.headers["form-params"]?.string()?.contains("name:value") ?: false)
        assert(!(response.headers["form-params"]?.string()?.contains("queryName:queryValue") ?: true))
    }

    @Test fun `Sending multi part content works properly`() {
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
            assertEquals(it.value, response.headers[it.key])
        }
    }

    @Test fun `Sending files works properly`() {
        // clientFile
        val stream = urlOf("classpath:assets/index.html").readBytes()
        val parts = listOf(HttpPart("file", stream, "index.html"))
        val response = client.send(HttpRequest(POST, path = "/file", parts = parts))
        // clientFile
        assertEquals("index.html", response.headers["submitted-file"]?.value)
        assertResponseContains(response, OK_200, "<!DOCTYPE html>", "<title>Hexagon</title>", "</html>")
    }
}
