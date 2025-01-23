package com.hexagontk.http.test.examples

import com.hexagontk.core.require
import com.hexagontk.http.client.HttpClientPort
import com.hexagontk.http.model.*
import com.hexagontk.http.model.HttpMethod.*
import com.hexagontk.http.model.HttpMethod.Companion.ALL
import com.hexagontk.http.model.CREATED_201
import com.hexagontk.http.server.HttpServerPort
import com.hexagontk.http.server.HttpServerSettings
import com.hexagontk.http.handlers.PathHandler
import com.hexagontk.http.handlers.HttpHandler
import com.hexagontk.http.handlers.path
import com.hexagontk.http.test.BaseTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@Suppress("FunctionName") // This class's functions are intended to be used only in tests
abstract class BooksTest(
    final override val clientAdapter: () -> HttpClientPort,
    final override val serverAdapter: () -> HttpServerPort,
    final override val serverSettings: HttpServerSettings = HttpServerSettings(),
) : BaseTest() {

    // books
    data class Book(val author: String, val title: String)

    private val books: MutableMap<Int, Book> = linkedMapOf(
        100 to Book("Miguel de Cervantes", "Don Quixote"),
        101 to Book("William Shakespeare", "Hamlet"),
        102 to Book("Homer", "The Odyssey")
    )

    private val path: PathHandler = path {

        post("/books") {
            val author = queryParameters["author"]?.text ?: return@post badRequest("Missing author")
            val title = queryParameters["title"]?.text ?: return@post badRequest("Missing title")
            val id = (books.keys.maxOrNull() ?: 0) + 1
            books += id to Book(author, title)
            created(id.toString())
        }

        get("/books/{id}") {
            val bookId = pathParameters.require("id").toInt()
            val book = books[bookId]
            if (book != null)
                ok("Title: ${book.title}, Author: ${book.author}")
            else
                notFound("Book not found")
        }

        put("/books/{id}") {
            val bookId = pathParameters.require("id").toInt()
            val book = books[bookId]
            if (book != null) {
                books += bookId to book.copy(
                    author = queryParameters["author"]?.text ?: book.author,
                    title = queryParameters["title"]?.text ?: book.title
                )

                ok("Book with id '$bookId' updated")
            }
            else {
                notFound("Book not found")
            }
        }

        delete("/books/{id}") {
            val bookId = pathParameters.require("id").toInt()
            val book = books[bookId]
            books -= bookId
            if (book != null)
                ok("Book with id '$bookId' deleted")
            else
                notFound("Book not found")
        }

        // Matches path's requests with *any* HTTP method as a fallback (return 405 instead 404)
        after(ALL - DELETE - PUT - GET, "/books/{id}") {
            send(METHOD_NOT_ALLOWED_405)
        }

        get("/books") {
            ok(books.keys.joinToString(" ", transform = Int::toString))
        }
    }
    // books

    private val pathAlternative: PathHandler = path("/books") {

        post {
            val author = queryParameters["author"]?.text ?: return@post badRequest("Missing author")
            val title = queryParameters["title"]?.text ?: return@post badRequest("Missing title")
            val id = (books.keys.maxOrNull() ?: 0) + 1
            books += id to Book(author, title)
            created(id.toString())
        }

        get("/{id}") {
            val bookId = pathParameters.require("id").toInt()
            val book = books[bookId]
            if (book != null)
                ok("Title: ${book.title}, Author: ${book.author}")
            else
                notFound("Book not found")
        }

        put("/{id}") {
            val bookId = pathParameters.require("id").toInt()
            val book = books[bookId]
            if (book != null) {
                books += bookId to book.copy(
                    author = queryParameters["author"]?.text ?: book.author,
                    title = queryParameters["title"]?.text ?: book.title
                )

                ok("Book with id '$bookId' updated")
            }
            else {
                notFound("Book not found")
            }
        }

        delete("/{id}") {
            val bookId = pathParameters.require("id").toInt()
            val book = books[bookId]
            books -= bookId
            if (book != null)
                ok("Book with id '$bookId' deleted")
            else
                notFound("Book not found")
        }

        // Matches path's requests with *any* HTTP method as a fallback (return 405 instead 404)
        after(ALL - DELETE - PUT - GET, "/{id}") {
            send(METHOD_NOT_ALLOWED_405)
        }

        get {
            ok(books.keys.joinToString(" ", transform = Int::toString))
        }
    }

    private val pathAlternative2: PathHandler = path("/books") {

        post {
            val author = queryParameters["author"]?.text ?: return@post badRequest("Missing author")
            val title = queryParameters["title"]?.text ?: return@post badRequest("Missing title")
            val id = (books.keys.maxOrNull() ?: 0) + 1
            books += id to Book(author, title)
            created(id.toString())
        }

        path("/{id}") {
            get {
                val bookId = pathParameters.require("id").toInt()
                val book = books[bookId]
                if (book != null)
                    ok("Title: ${book.title}, Author: ${book.author}")
                else
                    notFound("Book not found")
            }

            put {
                val bookId = pathParameters.require("id").toInt()
                val book = books[bookId]
                if (book != null) {
                    books += bookId to book.copy(
                        author = queryParameters["author"]?.text ?: book.author,
                        title = queryParameters["title"]?.text ?: book.title
                    )

                    ok("Book with id '$bookId' updated")
                }
                else {
                    notFound("Book not found")
                }
            }

            delete {
                val bookId = pathParameters.require("id").toInt()
                val book = books[bookId]
                books -= bookId
                if (book != null)
                    ok("Book with id '$bookId' deleted")
                else
                    notFound("Book not found")
            }

            // Matches path's requests with *any* HTTP method as a fallback (return 405 instead 404)
            after(ALL - DELETE - PUT - GET) {
                send(METHOD_NOT_ALLOWED_405)
            }
        }

        get {
            ok(books.keys.joinToString(" ", transform = Int::toString))
        }
    }

    override val handler: HttpHandler = path {
        path("/a", path)
        path("/b", pathAlternative)
        path("/c", pathAlternative2)
    }

    @Test fun `Create book returns 201 and new book ID`() {
        val server = server()
        val client = client(server)

        listOf("/a", "/b", "/c").forEach {
            val result = client.post("$it/books?author=Vladimir%20Nabokov&title=Lolita")
            assert(Integer.valueOf(result.body as String) > 0)
            assertEquals(CREATED_201, result.status)
        }

        client.stop()
        server.stop()
    }

    @Test fun `Create book returns 400 if a parameter is missing`() {
        val server = server()
        val client = client(server)

        listOf("/a", "/b", "/c").forEach { p ->
            client.post("$p/books?title=Lolita").let {
                assertEquals("Missing author", it.body)
                assertEquals(BAD_REQUEST_400, it.status)
            }

            client.post("$p/books?author=Vladimir%20Nabokov").let {
                assertEquals("Missing title", it.body)
                assertEquals(BAD_REQUEST_400, it.status)
            }
        }

        client.stop()
        server.stop()
    }

    @Test fun `List books contains all books IDs`() {
        val server = server()
        val client = client(server)

        listOf("/a", "/b", "/c").forEach {
            val result = client.get("$it/books")
            assertResponseContains(result, "100", "101")
        }

        client.stop()
        server.stop()
    }

    @Test fun `Get book returns all book's fields`() {
        val server = server()
        val client = client(server)

        listOf("/a", "/b", "/c").forEach {
            val result = client.get("$it/books/101")
            assertResponseContains(result, "William Shakespeare", "Hamlet")
        }

        client.stop()
        server.stop()
    }

    @Test fun `Update book overrides existing book data`() {
        val server = server()
        val client = client(server)

        listOf("/a", "/b", "/c").forEach {
            val resultPut = client.put("$it/books/100?title=Don%20Quixote")
            assertResponseContains(resultPut, "100", "updated")

            val resultGet = client.get("$it/books/100")
            assertResponseContains(resultGet, "Miguel de Cervantes", "Don Quixote")
        }

        client.stop()
        server.stop()
    }

    @Test fun `Update not found book returns a 404`() {
        val server = server()
        val client = client(server)

        listOf("/a", "/b", "/c").forEach {
            val result = client.put("$it/books/9999?title=Don%20Quixote")
            assertResponseContains(result, NOT_FOUND_404, "not found")
        }

        client.stop()
        server.stop()
    }

    @Test fun `Delete book returns the deleted record ID`() {
        val server = server()
        val client = client(server)

        listOf("/a", "/b", "/c").forEach {
            val createResult =
                client.post("$it/books?author=Ken%20Follett&title=The%20Pillars%20of%20the%20Earth")
            val id = Integer.valueOf(createResult.body as String)
            assert(id > 0)
            assertEquals(CREATED_201, createResult.status)
            val result = client.delete("$it/books/$id")
            assertResponseContains(result, id.toString(), "deleted")
        }

        client.stop()
        server.stop()
    }

    @Test fun `Delete not found book returns a 404`() {
        val server = server()
        val client = client(server)

        listOf("/a", "/b", "/c").forEach {
            val result = client.delete("$it/books/9999")
            assertResponseContains(result, NOT_FOUND_404, "not found")
        }

        client.stop()
        server.stop()
    }

    @Test fun `Book not found returns a 404`() {
        val server = server()
        val client = client(server)

        listOf("/a", "/b", "/c").forEach {
            val result = client.get("$it/books/9999")
            assertResponseContains(result, NOT_FOUND_404, "not found")
        }

        client.stop()
        server.stop()
    }

    @Test fun `Invalid method returns 405`() {
        val server = server()
        val client = client(server)

        listOf("/a", "/b", "/c").forEach {
            val result = client.options("$it/books/9999")
            assertEquals(METHOD_NOT_ALLOWED_405, result.status)
        }

        client.stop()
        server.stop()
    }

    @Test fun `Not handled method returns 404`() {
        val server = server()
        val client = client(server)

        listOf("/a", "/b", "/c").forEach {
            val result = client.options("$it/books")
            assertEquals(NOT_FOUND_404, result.status)
        }

        client.stop()
        server.stop()
    }
}
