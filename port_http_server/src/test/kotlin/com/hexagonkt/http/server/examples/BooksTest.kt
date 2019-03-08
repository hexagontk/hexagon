package com.hexagonkt.http.server.examples

import com.hexagonkt.helpers.require
import com.hexagonkt.http.client.Client
import com.hexagonkt.http.server.Server
import com.hexagonkt.http.server.ServerPort
import org.asynchttpclient.Response
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

@Test abstract class BooksTest(adapter: ServerPort) {

    // sample
    data class Book (val author: String, val title: String)

    private val books: MutableMap<Int, Book> = linkedMapOf(
        100 to Book("Miguel de Cervantes", "Don Quixote"),
        101 to Book("William Shakespeare", "Hamlet"),
        102 to Book("Homer", "The Odyssey")
    )

    val server: Server by lazy {
        Server(adapter) {
            post("/books") {
                val author = parameters.require("author").first()
                val title = parameters.require("title").first()
                val id = (books.keys.max() ?: 0) + 1
                books += id to Book(author, title)
                send(201, id)
            }

            get("/books/{id}") {
                val bookId = pathParameters["id"].toInt()
                val book = books[bookId]
                if (book != null)
                    ok("Title: ${book.title}, Author: ${book.author}")
                else
                    send(404, "Book not found")
            }

            put("/books/{id}") {
                val bookId = pathParameters["id"].toInt()
                val book = books[bookId]
                if (book != null) {
                    books += bookId to book.copy (
                        author = parameters["author"]?.first() ?: book.author,
                        title = parameters["title"]?.first() ?: book.title
                    )

                    ok("Book with id '$bookId' updated")
                }
                else {
                    send(404, "Book not found")
                }
            }

            delete("/books/{id}") {
                val bookId = pathParameters["id"].toInt()
                val book = books[bookId]
                books -= bookId
                if (book != null)
                    ok("Book with id '$bookId' deleted")
                else
                    send(404, "Book not found")
            }

            get("/books") {
                ok(books.keys.joinToString(" ", transform = Int::toString))
            }
        }
    }
    // sample

    private val client: Client by lazy { Client("http://localhost:${server.runtimePort}") }

    @BeforeClass fun initialize() {
        server.start()
    }

    @AfterClass fun shutdown() {
        server.stop()
    }

    @Test fun createBook() {
        val result = client.post("/books?author=Vladimir%20Nabokov&title=Lolita")
        assert(Integer.valueOf(result.responseBody) > 0)
        assert(201 == result.statusCode)
    }

    @Test fun listBooks() {
        val result = client.get("/books")
        assertResponseContains(result, "100", "101")
    }

    @Test fun getBook () {
        val result = client.get("/books/101")
        assertResponseContains(result, "William Shakespeare", "Hamlet")
    }

    @Test fun updateBook() {
        val resultPut = client.put("/books/100?title=Don%20Quixote")
        assertResponseContains(resultPut, "100", "updated")

        val resultGet = client.get("/books/100")
        assertResponseContains(resultGet, "Miguel de Cervantes", "Don Quixote")
    }

    @Test fun deleteBook () {
        val result = client.delete("/books/102")
        assertResponseContains(result, "102", "deleted")
    }

    @Test fun bookNotFound() {
        val result = client.get ("/books/9999")
        assertResponseContains(result, 404, "not found")
    }

    @Test fun invalidMethodReturns405 () {
        val result = client.options("/books/9999")
        assert(405 == result.statusCode)
    }

    private fun assertResponseContains(response: Response?, status: Int, vararg content: String) {
        assert (response?.statusCode == status)
        content.forEach {
            assert(response?.responseBody?.contains (it) ?: false)
        }
    }

    private fun assertResponseContains(response: Response?, vararg content: String) {
        assertResponseContains(response, 200, *content)
    }
}
