package com.hexagonkt.http.test.examples

import com.hexagonkt.core.helpers.MultiMap
import com.hexagonkt.core.helpers.multiMapOf
import com.hexagonkt.core.helpers.multiMapOfLists
import com.hexagonkt.core.helpers.require
import com.hexagonkt.core.media.TextMedia.CSS
import com.hexagonkt.core.media.TextMedia.HTML
import com.hexagonkt.http.client.HttpClientPort
import com.hexagonkt.http.client.model.HttpClientRequest
import com.hexagonkt.http.model.ClientErrorStatus.NOT_FOUND
import com.hexagonkt.http.model.HttpMethod.GET
import com.hexagonkt.http.model.HttpMethod.POST
import com.hexagonkt.http.model.HttpPart
import com.hexagonkt.http.model.SuccessStatus.OK
import com.hexagonkt.http.server.HttpServerPort
import com.hexagonkt.http.server.callbacks.FileCallback
import com.hexagonkt.http.server.callbacks.UrlCallback
import com.hexagonkt.http.server.handlers.PathHandler
import com.hexagonkt.http.server.handlers.ServerHandler
import com.hexagonkt.http.server.handlers.path
import com.hexagonkt.http.test.BaseTest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.io.File
import java.net.URL
import kotlin.test.assertEquals

@TestInstance(PER_CLASS)
@Suppress("FunctionName") // This class's functions are intended to be used only in tests
abstract class FilesTest(
    override val clientAdapter: () -> HttpClientPort,
    override val serverAdapter: () -> HttpServerPort
) : BaseTest() {

    private val directory = File("http_test/src/main/resources/assets").let {
        if (it.exists()) it.path
        else "src/main/resources/assets"
    }

    // files
    private val path: PathHandler = path {

        // Serve `public` resources folder on `/*`
        after(
            methods = setOf(GET),
            pattern = "/*",
            status = NOT_FOUND,
            callback = UrlCallback(URL("classpath:public"))
        )

        path("/static") {
            get("/files/*", UrlCallback(URL("classpath:assets")))
            get("/resources/*", FileCallback(File(directory)))
        }

        get("/html/*", UrlCallback(URL("classpath:assets"))) // Serve `assets` files on `/html/*`
        get("/pub/*", FileCallback(File(directory))) // Serve `test` folder on `/pub/*`

        post("/multipart") {
            val headers: MultiMap<String, String> = request.parts.first().let { p ->
                val name = p.name
                val bodyString = p.bodyString()
                val size = p.size.toString()
                val fullType = p.contentType?.mediaType?.fullType ?: ""
                val contentDisposition = p.headers.require("content-disposition")
                multiMapOf(
                    "name" to name,
                    "body" to bodyString,
                    "size" to size,
                    "type" to fullType,
                    "content-disposition" to contentDisposition
                )
            }

            ok(headers = headers)
        }

        post("/file") {
            val part = request.parts.first()
            val content = part.bodyString()
            ok(content)
        }

        post("/form") {
            fun serializeMap(map: Map<String, List<String>>): List<String> = listOf(
                map.map { "${it.key}:${it.value.joinToString(",")}}" }.joinToString("\n")
            )

            val queryParams = serializeMap(request.queryParameters.allValues)
            val formParams = serializeMap(request.formParameters.allValues)
            val headers =
                multiMapOfLists("query-params" to queryParams, "form-params" to formParams)

            ok(headers = response.headers + headers)
        }
    }
    // files

    override val handlers: List<ServerHandler> = listOf(path)

    @Test fun `Parameters are separated from each other`() = runBlocking {
        val parts = listOf(HttpPart("name", "value"))
        val response = client.send(
            HttpClientRequest(POST, path = "/form?queryName=queryValue", parts = parts)
        )
        assert(response.headers["query-params"]?.contains("queryName:queryValue") ?: false)
        assert(!(response.headers["query-params"]?.contains("name:value") ?: true))
        assert(response.headers["form-params"]?.contains("name:value") ?: false)
        assert(!(response.headers["form-params"]?.contains("queryName:queryValue") ?: true))
    }

    @Test fun `Requesting a folder with an existing file name returns 404`() = runBlocking {
        val response = client.get ("/file.txt/")
        assertResponseContains(response, NOT_FOUND)
    }

    @Test fun `An static file from resources can be fetched`() = runBlocking {
        val response = client.get("/file.txt")
        assertResponseEquals(response, content = "file content")
    }

    @Test fun `Files content type is returned properly`() = runBlocking<Unit> {
        val response = client.get("/file.css")
        assertEquals(CSS, response.contentType?.mediaType)
        assertResponseEquals(response, content = "/* css */")

        val responseFile = client.get("/pub/css/mkdocs.css")
        assertResponseContains(responseFile, OK, "article")
        assertEquals(CSS, responseFile.contentType?.mediaType)

        client.get("/static/resources/css/mkdocs.css").apply {
            assertEquals(CSS, contentType?.mediaType)
            assertResponseContains(this, OK, "article")
        }
    }

    @Test fun `Not found resources return 404`() = runBlocking {
        assertEquals(NOT_FOUND, client.get("/not_found.css").status)
        assertEquals(NOT_FOUND, client.get("/pub/not_found.css").status)
        assertEquals(NOT_FOUND, client.get("/html/not_found.css").status)
    }

    @Test fun `Sending multi part content works properly`() = runBlocking {
        // clientForm
        val parts = listOf(HttpPart("name", "value"))
        val response = client.send(HttpClientRequest(POST, path = "/multipart", parts = parts))
        // clientForm
        val expectedHeaders = multiMapOf(
            "transfer-encoding" to "chunked",
            "name" to "name",
            "body" to "value",
            "size" to "5",
            "type" to "text/plain",
            "content-disposition" to "form-data; name=\"name\"",
        )
        assertEquals(expectedHeaders, response.headers)
    }

    @Test fun `Sending files works properly`() = runBlocking {
        // clientFile
        val stream = URL("classpath:assets/index.html").readBytes()
        val parts = listOf(HttpPart("file", stream, "index.html"))
        val response = client.send(HttpClientRequest(POST, path = "/file", parts = parts))
        // clientFile
        assertResponseContains(response, OK, "<title>Hexagon</title>")
    }

    @Test fun `Files mounted on a path are returned properly`() = runBlocking<Unit> {
        val response = client.get("/html/index.html")
        assertEquals(HTML, response.contentType?.mediaType)
        assertResponseContains(response, OK, "<title>Hexagon</title>")

        client.get("/static/files/index.html").apply {
            assertEquals(HTML, contentType?.mediaType)
            assertResponseContains(this, OK, "<title>Hexagon</title>")
        }
    }
}
