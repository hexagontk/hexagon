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

    private data class Book (val author: String, val title: String)

    private val books: MutableMap<Int, Book> = linkedMapOf(
        100 to Book("Miguel_de_Cervantes", "Don_Quixote"),
        101 to Book("William_Shakespeare", "Hamlet"),
        102 to Book("Homer", "The_Odyssey")
    )

    // Parse post body with JSON/YAML
    // Set start header and attribute (before) and end header (after) with elapsed time
    // Add auth filters
    // Errors and exception handling
    // Templates
    // Serialize response with accept content type
    // Static resources
    // File upload
    // CORS
    // Websocket
    // Middleware (define Call extension functions to add routes/filters)
    private val server: Server by lazy {
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

            delete ("/books/{id}") {
                val bookId = pathParameters["id"].toInt()
                val book = books[bookId]
                books -= bookId
                if (book != null)
                    ok ("Book with id '$bookId' deleted")
                else
                    send(404, "Book not found")
            }

            get ("/books") {
                ok (books.keys.joinToString(" ", transform = Int::toString))
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

    @Test fun createBook () {
        val result = client.post ("/books?author=Vladimir_Nabokov&title=Lolita")
        assert (Integer.valueOf (result.responseBody) > 0)
        assert (201 == result.statusCode)
    }

    @Test fun listBooks () {
        val result = client.get ("/books")
        assertResponseContains(result, "100", "101")
    }

    @Test fun getBook () {
        val result = client.get ("/books/101")
        assertResponseContains (result, "William_Shakespeare", "Hamlet")
    }

    @Test fun updateBook () {
        val resultPut = client.put ("/books/100?title=Don_Quixote")
        assertResponseContains (resultPut, "100", "updated")

        val resultGet = client.get ("/books/100")
        assertResponseContains (resultGet, "Miguel_de_Cervantes", "Don_Quixote")
    }

    @Test fun deleteBook () {
        val result = client.delete ("/books/102")
        assertResponseContains (result, "102", "deleted")
        books += 102 to Book("Homer", "The_Odyssey") // Restore book for next tests
    }

    @Test fun bookNotFound () {
        val result = client.get ("/books/9999")
        assertResponseContains(result, 404, "not found")
    }

    @Test fun invalidMethodReturns405 () {
        val result = client.options ("/books/9999")
        assert (405 == result.statusCode)
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
