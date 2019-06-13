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

    // books
    data class Book(val author: String, val title: String)

    private val books: MutableMap<Int, Book> = linkedMapOf(
        100 to Book("Miguel de Cervantes", "Don Quixote"),
        101 to Book("William Shakespeare", "Hamlet"),
        102 to Book("Homer", "The Odyssey")
    )

    val server: Server by lazy {
        Server(adapter) {
            post("/books") {
                // Require fails if parameter does not exists
                val author = queryParameters.require("author").first()
                val title = queryParameters.require("title").first()
                val id = (books.keys.max() ?: 0) + 1
                books += id to Book(author, title)
                send(201, id)
            }

            get("/books/{id}") {
                // Path parameters *must* exist an error is thrown if they are not present
                val bookId = pathParameters["id"].toInt()
                val book = books[bookId]
                if (book != null)
                    // ok() is a shortcut to send(200)
                    ok("Title: ${book.title}, Author: ${book.author}")
                else
                    send(404, "Book not found")
            }

            put("/books/{id}") {
                val bookId = pathParameters["id"].toInt()
                val book = books[bookId]
                if (book != null) {
                    books += bookId to book.copy(
                        author = queryParameters["author"]?.first() ?: book.author,
                        title = queryParameters["title"]?.first() ?: book.title
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

            // Matches path's requests with *any* HTTP method as a fallback (return 404 instead 405)
            any("/books/{id}") { send(405) }

            get("/books") { ok(books.keys.joinToString(" ", transform = Int::toString)) }
        }
    }
    // books

    private val client: Client by lazy { Client("http://localhost:${server.runtimePort}") }

    @BeforeClass fun initialize() {
        server.start()
    }

    @AfterClass fun shutdown() {
        server.stop()
    }

    @Test fun `Create book returns 201 and new book ID`() {
        val result = client.post("/books?author=Vladimir%20Nabokov&title=Lolita")
        assert(Integer.valueOf(result.responseBody) > 0)
        assert(201 == result.statusCode)
    }

    @Test fun `List books contains all books IDs`() {
        val result = client.get("/books")
        assertResponseContains(result, "100", "101")
    }

    @Test fun `Get book returns all book's fields`() {
        val result = client.get("/books/101")
        assertResponseContains(result, "William Shakespeare", "Hamlet")
    }

    @Test fun `Update book overrides existing book data`() {
        val resultPut = client.put("/books/100?title=Don%20Quixote")
        assertResponseContains(resultPut, "100", "updated")

        val resultGet = client.get("/books/100")
        assertResponseContains(resultGet, "Miguel de Cervantes", "Don Quixote")
    }

    @Test fun `Delete book returns the deleted record ID`() {
        val result = client.delete("/books/102")
        assertResponseContains(result, "102", "deleted")
    }

    @Test fun `Book not found returns a 404`() {
        val result = client.get("/books/9999")
        assertResponseContains(result, 404, "not found")
    }

    @Test fun `Invalid method returns 405`() {
        val result = client.options("/books/9999")
        assert(405 == result.statusCode)
    }

    private fun assertResponseContains(response: Response?, status: Int, vararg content: String) {
        assert(response?.statusCode == status)
        content.forEach {
            assert(response?.responseBody?.contains(it) ?: false)
        }
    }

    private fun assertResponseContains(response: Response?, vararg content: String) {
        assertResponseContains(response, 200, *content)
    }
}
