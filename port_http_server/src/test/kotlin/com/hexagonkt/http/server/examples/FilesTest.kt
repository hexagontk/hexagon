package com.hexagonkt.http.server.examples

import com.hexagonkt.helpers.Resource
import com.hexagonkt.http.Method.POST
import com.hexagonkt.http.Part
import com.hexagonkt.http.Path
import com.hexagonkt.http.client.Client
import com.hexagonkt.http.client.Request
import com.hexagonkt.http.client.ahc.AhcAdapter
import com.hexagonkt.http.server.Server
import com.hexagonkt.http.server.ServerPort
import com.hexagonkt.http.client.Response
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.io.File

@Test abstract class FilesTest(adapter: ServerPort) {

    private val directory = File("hexagon_site/assets").let {
        if (it.exists()) it.path
        else "../hexagon_site/assets"
    }

    // files
    private val server: Server = Server(adapter) {
        path("/static") {
            get("/files/*", Resource("assets")) // Serve `assets` resources on `/html/*`
            get("/resources/*", File(directory)) // Serve `test` folder on `/pub/*`
        }

        get("/html/*", Resource("assets")) // Serve `assets` resources on `/html/*`
        get("/pub/*", File(directory)) // Serve `test` folder on `/pub/*`
        get(Resource("public")) // Serve `public` resources folder on `/*`

        post("/multipart") { ok(request.parts.keys.joinToString(":")) }

        post("/file") {
            val part = request.parts.values.first()
            val content = part.inputStream.reader().readText()
            ok(content)
        }

        post("/form") {
            fun serializeMap(map: Map<String, List<String>>): List<String> = listOf(
                map.map { "${it.key}:${it.value.joinToString(",")}}" }.joinToString("\n")
            )

            val queryParams = serializeMap(queryParameters)
            val formParams = serializeMap(formParameters)
            val params = serializeMap(parameters)

            response.headers["queryParams"] = queryParams
            response.headers["formParams"] = formParams
            response.headers["params"] = params
        }
    }
    // files

    private val client: Client by lazy {
        Client(AhcAdapter(), "http://localhost:${server.runtimePort}")
    }

    @BeforeClass fun initialize() {
        server.start()
    }

    @AfterClass fun shutdown() {
        server.stop()
    }

    @Test fun `Parameters are separated from each other`() {
        val parts = mapOf("name" to Part("name", "value"))
        val response = client.send(
            Request(POST, Path("/form?queryName=queryValue"), parts = parts)
        )
        assert(response.headers["queryParams"]?.first()?.contains("queryName:queryValue") ?: false)
        assert(!(response.headers["queryParams"]?.first()?.contains("name:value") ?: true))
        assert(response.headers["formParams"]?.first()?.contains("name:value") ?: false)
        assert(!(response.headers["formParams"]?.first()?.contains("queryName:queryValue") ?: true))
        assert(response.headers["params"]?.first()?.contains("queryName:queryValue") ?: false)
        assert(response.headers["params"]?.first()?.contains("name:value") ?: false)
    }

    @Test fun `Requesting a folder with an existing file name returns 404`() {
        val response = client.get ("/file.txt/")
        assertResponseContains(response, 404)
    }

    @Test fun `An static file from resources can be fetched`() {
        val response = client.get("/file.txt")
        assertResponseEquals(response, "file content\n")
    }

    @Test fun `Files content type is returned properly`() {
        val response = client.get("/file.css")
        assert(response.contentType?.contains("css") ?: false)
        assertResponseEquals(response, "/* css */\n")

        val responseFile = client.get("/pub/css/mkdocs.css")
        assert(responseFile.contentType?.contains("css") ?: false)
        assertResponseContains(responseFile, 200, "article")

        client.get("/static/resources/css/mkdocs.css").apply {
            assert(contentType?.contains("css") ?: false)
            assertResponseContains(this, 200, "article")
        }
    }

    @Test fun `Not found resources return 404`() {
        assert(client.get("/not_found.css").status == 404)
    }

    @Test fun `Sending multi part content works properly`() {
        val parts = mapOf("name" to Part("name", "value"))
        val response = client.send(Request(POST, Path("/multipart"), parts = parts))
        assert(response.body == "name")
    }

    @Test fun `Sending files works properly`() {
        val stream = Resource("assets/index.html").requireStream()
        val parts = mapOf("file" to Part("file", stream, "index.html"))
        val response = client.send(Request(POST, Path("/file"), parts = parts))
        assertResponseContains(response, 200, "<title>Hexagon</title>")
    }

    @Test fun `Files mounted on a path are returned properly`() {
        val response = client.get("/html/index.html")
        assert(response.contentType?.contains("html") ?: false)
        assertResponseContains(response, 200, "<title>Hexagon</title>")

        client.get("/static/files/index.html").apply {
            assert(contentType?.contains("html") ?: false)
            assertResponseContains(this, 200, "<title>Hexagon</title>")
        }
    }

    private fun assertResponseEquals(response: Response?, content: String, status: Int = 200) {
        assert(response?.status == status)
        assert(response?.body == content)
    }

    private fun assertResponseContains(response: Response?, status: Int, vararg content: String) {
        assert(response?.status == status)
        content.forEach {
            assert (response?.body?.contains (it) ?: false)
        }
    }
}
