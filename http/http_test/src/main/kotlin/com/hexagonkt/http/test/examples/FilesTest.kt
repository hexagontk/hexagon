package com.hexagontk.http.test.examples

import com.hexagontk.core.media.TEXT_CSS
import com.hexagontk.core.media.TEXT_HTML
import com.hexagontk.core.urlOf
import com.hexagontk.http.client.HttpClientPort
import com.hexagontk.http.model.*
import com.hexagontk.http.model.NOT_FOUND_404
import com.hexagontk.http.model.HttpMethod.GET
import com.hexagontk.http.model.OK_200
import com.hexagontk.http.server.HttpServerPort
import com.hexagontk.http.server.HttpServerSettings
import com.hexagontk.http.server.callbacks.FileCallback
import com.hexagontk.http.server.callbacks.UrlCallback
import com.hexagontk.http.handlers.PathHandler
import com.hexagontk.http.handlers.HttpHandler
import com.hexagontk.http.handlers.path
import com.hexagontk.http.test.BaseTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.io.File
import kotlin.test.assertEquals

@TestInstance(PER_CLASS)
@Suppress("FunctionName") // This class's functions are intended to be used only in tests
abstract class FilesTest(
    final override val clientAdapter: () -> HttpClientPort,
    final override val serverAdapter: () -> HttpServerPort,
    final override val serverSettings: HttpServerSettings = HttpServerSettings(),
) : BaseTest() {

    private val directory = File("http_test/src/main/resources/assets").let {
        if (it.exists()) it.path
        else "../http_test/src/main/resources/assets"
    }

    // TODO Remove unnecessary handlers (restructure examples)
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

    @Test fun `Requesting a folder with an existing file name returns 404`() {
        val response = client.get ("/file.txt/")
        assertResponseContains(response, NOT_FOUND_404)
    }

    @Test fun `An static file from resources can be fetched`() {
        val response = client.get("/file.txt")
        assertResponseEquals(response, content = "file content")
    }

    @Test fun `Files content type is returned properly`() {
        val response = client.get("/file.css")
        assertEquals(TEXT_CSS, response.contentType?.mediaType)
        assertResponseEquals(response, content = "/* css */")

        val responseFile = client.get("/pub/css/mkdocs.css")
        assertResponseContains(responseFile, OK_200, "article")
        assertEquals(TEXT_CSS, responseFile.contentType?.mediaType)

        client.get("/static/resources/css/mkdocs.css").apply {
            assertEquals(TEXT_CSS, contentType?.mediaType)
            assertResponseContains(this, OK_200, "article")
        }
    }

    @Test fun `Not found resources return 404`() {
        assertEquals(NOT_FOUND_404, client.get("/not_found.css").status)
        assertEquals(NOT_FOUND_404, client.get("/pub/not_found.css").status)
        assertEquals(NOT_FOUND_404, client.get("/html/not_found.css").status)
    }

    @Test fun `Files mounted on a path are returned properly`() {
        val response = client.get("/html/index.html")
        assertEquals(TEXT_HTML, response.contentType?.mediaType)
        assertResponseContains(response, OK_200, "<title>Hexagon</title>")

        client.get("/static/files/index.html").apply {
            assertEquals(TEXT_HTML, contentType?.mediaType)
            assertResponseContains(this, OK_200, "<title>Hexagon</title>")
        }
    }
}
