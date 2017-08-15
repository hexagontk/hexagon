package com.hexagonkt.server

import com.hexagonkt.client.Client

@Suppress("unused", "MemberVisibilityCanPrivate") // Test methods are flagged as unused
internal class HexagonModule : TestModule() {
    override fun initialize(): Router = router {
        get ("/hexagon/books/{id}") {
            ok ("${request ["id"]}:${request.body}")
        }
        get ("/hexagon/books/{id}/{title}") { ok ("${request ["id"]}:${request ["title"]} ${request.body}") }
        trace ("/hexagon/books/{id}/{title}") { ok ("${request ["id"]}:${request ["title"]} ${request.body}") }
        patch ("/hexagon/books/{id}/{title}") { ok ("${request ["id"]}:${request ["title"]} ${request.body}") }
        head ("/hexagon/books/{id}/{title}") {
            response.addHeader("id", request.parameter("id"))
            response.addHeader("title", request.parameter("title"))
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

        assertResponseContains (client.get ("/hexagon/books/101/Hamlet", "body"), "101", "Hamlet", "body")
        assertResponseContains (client.trace ("/hexagon/books/101/Hamlet", "body"), "101", "Hamlet", "body")
        assertResponseContains (client.patch ("/hexagon/books/101/Hamlet", "body"), "101", "Hamlet", "body")
        assertResponseContains (client.head ("/hexagon/books/101/Hamlet", "body"))
    }

    override fun validate(client: Client) {
        foo(client)
        getBook(client)
    }
}
