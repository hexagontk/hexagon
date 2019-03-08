package com.hexagonkt.http.server.examples

import com.hexagonkt.http.client.Client
import com.hexagonkt.http.server.Server
import com.hexagonkt.http.server.ServerPort
import org.asynchttpclient.Response
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

@Test abstract class HexagonTest(adapter: ServerPort) {

    private val server: Server by lazy {
        Server(adapter) {
            post("/hexagon/files") {
                ok(request.parts.keys.joinToString(":"))
            }
            get("/hexagon/books/{id}") {
                ok(pathParameters["id"])
            }
            get("/hexagon/books/{id}/{title}") {
                ok("${pathParameters["id"]}:${pathParameters["title"]} ${request.body}")
            }
            trace("/hexagon/books/{id}/{title}") {
                ok("${pathParameters["id"]}:${pathParameters["title"]} ${request.body}")
            }
            patch("/hexagon/books/{id}/{title}") {
                ok("${pathParameters["id"]}:${pathParameters["title"]} ${request.body}")
            }
            head("/hexagon/books/{id}/{title}") {
                response.setHeader("id", pathParameters["id"])
                response.setHeader("title", pathParameters["title"])
            }
        }
    }

    private val client: Client by lazy { Client("http://localhost:${server.runtimePort}") }

    @BeforeClass fun initialize() {
        server.start()
    }

    @AfterClass fun shutdown() {
        server.stop()
    }

    @Test fun foo() {
        assertResponseContains(client.get ("/hexagon/books/101"), "101")
    }

    @Test fun getBook() {
        assertResponseContains(client.get ("/hexagon/books/101/Hamlet"), "101", "Hamlet")
        assertResponseContains(client.trace ("/hexagon/books/101/Hamlet"), "101", "Hamlet")
        assertResponseContains(client.patch ("/hexagon/books/101/Hamlet"), "101", "Hamlet")
        assertResponseContains(client.head ("/hexagon/books/101/Hamlet"))
    }

    @Test fun sendParts() {
        client.post("/hexagon/files")
    }

    protected fun assertResponseEquals(response: Response?, content: String, status: Int = 200) {
        assert (response?.statusCode == status)
        assert (response?.responseBody == content)
    }

    protected fun assertResponseContains(response: Response?, status: Int, vararg content: String) {
        assert (response?.statusCode == status)
        content.forEach {
            assert (response?.responseBody?.contains (it) ?: false)
        }
    }

    protected fun assertResponseContains(response: Response?, vararg content: String) {
        assertResponseContains(response, 200, *content)
    }
}
