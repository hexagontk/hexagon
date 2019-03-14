package com.hexagonkt.http.server.examples

import com.hexagonkt.http.Method
import com.hexagonkt.http.client.Client
import com.hexagonkt.http.server.Server
import com.hexagonkt.http.server.ServerPort
import org.asynchttpclient.Response
import org.asynchttpclient.request.body.multipart.StringPart
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.util.Locale.getDefault as defaultLocale

@Test abstract class FilesTest(adapter: ServerPort) {

    // files
    private val server: Server by lazy {
        Server(adapter) {
            assets("public")
            post("/files") { ok(request.parts.keys.joinToString(":")) }
        }
    }
    // files

    private val client: Client by lazy { Client("http://localhost:${server.runtimePort}") }

    @BeforeClass fun initialize() {
        server.start()
    }

    @AfterClass fun shutdown() {
        server.stop()
    }

    @Test fun `Requesting a folder with an existing file name returns 404`() {
        val response = client.get ("/file.txt/")
        assertResponseContains(response, 404)
    }

    @Test fun `An static file from resources can be fetched`() {
        val response = client.get ("/file.txt")
        assertResponseEquals(response, "file content\n")
    }

    @Test fun `Files content type is returned properly`() {
        val response = client.get ("/file.css")
        assert(response.contentType.contains("css"))
        assertResponseEquals(response, "/* css */\n")
    }

    @Test fun `Sending multi part content works properly`() {
        val parts = listOf(StringPart("name", "value"))
        val response = client.send(Method.POST, "/files", parts = parts)
        assert(response.responseBody == "name")
    }

//    @Test fun `Files mounted on a path are returned properly`() {
//        val response = client.get ("/html/index.html")
//        assert(response.contentType.contains("html"))
//        assertResponseContains(response, 200, "<title>Hexagon</title>")
//    }

    private fun assertResponseEquals(response: Response?, content: String, status: Int = 200) {
        assert (response?.statusCode == status)
        assert (response?.responseBody == content)
    }

    private fun assertResponseContains(response: Response?, status: Int, vararg content: String) {
        assert (response?.statusCode == status)
        content.forEach {
            assert (response?.responseBody?.contains (it) ?: false)
        }
    }
}
