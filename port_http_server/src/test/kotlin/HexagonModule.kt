package com.hexagonkt.http.server

import com.hexagonkt.helpers.require
import com.hexagonkt.http.client.Client

@Suppress("unused", "MemberVisibilityCanPrivate") // Test methods are flagged as unused
internal class HexagonModule : TestModule() {
    override fun initialize(): Router = Router {
        get("/hexagon/files") {
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
            response.addHeader("id", pathParameters["id"])
            response.addHeader("title", pathParameters["title"])
        }
    }

    fun foo (client: Client) {
        assertResponseContains (client.get ("/hexagon/books/101"), "101")
    }

    fun getBook (client: Client) {
        assertResponseContains (client.get ("/hexagon/books/101/Hamlet"), "101", "Hamlet")
        assertResponseContains (client.trace ("/hexagon/books/101/Hamlet"), "101", "Hamlet")
        assertResponseContains (client.patch ("/hexagon/books/101/Hamlet"), "101", "Hamlet")
        assertResponseContains (client.head ("/hexagon/books/101/Hamlet"))
    }

    fun sendParts(client: Client) {
        client.get("/hexagon/files")
    }

    override fun validate(client: Client) {
        foo(client)
        getBook(client)
    }
}
