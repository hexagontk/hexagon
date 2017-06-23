package co.there4.hexagon.server.integration

import co.there4.hexagon.client.Client
import co.there4.hexagon.server.*

class HexagonIT(serverEngine: Client) : ItModule(serverEngine) {
    override fun initialize(router: Router) {
        router.get ("/books/{id}") {
            ok ("${request ["id"]}:${request.body}")
        }
        router.get ("/books/{id}/{title}") { ok ("${request ["id"]}:${request ["title"]} ${request.body}") }
        router.trace ("/books/{id}/{title}") { ok ("${request ["id"]}:${request ["title"]} ${request.body}") }
        router.patch ("/books/{id}/{title}") { ok ("${request ["id"]}:${request ["title"]} ${request.body}") }
        router.head ("/books/{id}/{title}") {
            response.addHeader("id", request.parameter("id"))
            response.addHeader("title", request.parameter("title"))
        }
    }

    fun foo () {
        assertResponseContains (client.get ("/books/101"), "101")
    }

    fun getBook () {
        assertResponseContains (client.get ("/books/101/Hamlet"), "101", "Hamlet")
        assertResponseContains (client.trace ("/books/101/Hamlet"), "101", "Hamlet")
        assertResponseContains (client.patch ("/books/101/Hamlet"), "101", "Hamlet")
        assertResponseContains (client.head ("/books/101/Hamlet"))

        assertResponseContains (client.get ("/books/101/Hamlet", "body"), "101", "Hamlet", "body")
        assertResponseContains (client.trace ("/books/101/Hamlet", "body"), "101", "Hamlet", "body")
        assertResponseContains (client.patch ("/books/101/Hamlet", "body"), "101", "Hamlet", "body")
        assertResponseContains (client.head ("/books/101/Hamlet", "body"))
    }

    override fun validate() {
        foo()
        getBook()
    }
}
