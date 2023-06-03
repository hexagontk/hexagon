package com.hexagonkt.http.test.async.examples

import com.hexagonkt.core.require
import com.hexagonkt.handlers.async.done
import com.hexagonkt.http.client.HttpClientPort
import com.hexagonkt.http.model.*
import com.hexagonkt.http.model.HttpMethod.*
import com.hexagonkt.http.model.HttpMethod.Companion.ALL
import com.hexagonkt.http.model.CREATED_201
import com.hexagonkt.http.server.async.HttpServerPort
import com.hexagonkt.http.server.async.HttpServerSettings
import com.hexagonkt.http.handlers.async.PathHandler
import com.hexagonkt.http.handlers.async.HttpHandler
import com.hexagonkt.http.handlers.async.path
import com.hexagonkt.http.test.async.BaseTest
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
            val author = queryParameters["author"]?.string() ?: return@post badRequest("Missing author").done()
            val title = queryParameters["title"]?.string() ?: return@post badRequest("Missing title").done()
            val id = (books.keys.maxOrNull() ?: 0) + 1
            books += id to Book(author, title)
            created(id.toString()).done()
        }

        get("/books/{id}") {
            val bookId = pathParameters.require("id").toInt()
            val book = books[bookId]
            if (book != null)
                ok("Title: ${book.title}, Author: ${book.author}").done()
            else
                notFound("Book not found").done()
        }

        put("/books/{id}") {
            val bookId = pathParameters.require("id").toInt()
            val book = books[bookId]
            if (book != null) {
                books += bookId to book.copy(
                    author = queryParameters["author"]?.string() ?: book.author,
                    title = queryParameters["title"]?.string() ?: book.title
                )

                ok("Book with id '$bookId' updated").done()
            }
            else {
                notFound("Book not found").done()
            }
        }

        delete("/books/{id}") {
            val bookId = pathParameters.require("id").toInt()
            val book = books[bookId]
            books -= bookId
            if (book != null)
                ok("Book with id '$bookId' deleted").done()
            else
                notFound("Book not found").done()
        }

        // Matches path's requests with *any* HTTP method as a fallback (return 404 instead 405)
        after(ALL - DELETE - PUT - GET, "/books/{id}", status = NOT_FOUND_404) {
            send(METHOD_NOT_ALLOWED_405).done()
        }

        get("/books") {
            ok(books.keys.joinToString(" ", transform = Int::toString)).done()
        }
    }
    // books

    private val pathAlternative: PathHandler = path("/books") {

        post {
            val author = queryParameters["author"]?.string() ?: return@post badRequest("Missing author").done()
            val title = queryParameters["title"]?.string() ?: return@post badRequest("Missing title").done()
            val id = (books.keys.maxOrNull() ?: 0) + 1
            books += id to Book(author, title)
            created(id.toString()).done()
        }

        get("/{id}") {
            val bookId = pathParameters.require("id").toInt()
            val book = books[bookId]
            if (book != null)
                ok("Title: ${book.title}, Author: ${book.author}").done()
            else
                notFound("Book not found").done()
        }

        put("/{id}") {
            val bookId = pathParameters.require("id").toInt()
            val book = books[bookId]
            if (book != null) {
                books += bookId to book.copy(
                    author = queryParameters["author"]?.string() ?: book.author,
                    title = queryParameters["title"]?.string() ?: book.title
                )

                ok("Book with id '$bookId' updated").done()
            }
            else {
                notFound("Book not found").done()
            }
        }

        delete("/{id}") {
            val bookId = pathParameters.require("id").toInt()
            val book = books[bookId]
            books -= bookId
            if (book != null)
                ok("Book with id '$bookId' deleted").done()
            else
                notFound("Book not found").done()
        }

        // Matches path's requests with *any* HTTP method as a fallback (return 404 instead 405)
        after(ALL - DELETE - PUT - GET, "/{id}", status = NOT_FOUND_404) {
            send(METHOD_NOT_ALLOWED_405).done()
        }

        get {
            ok(books.keys.joinToString(" ", transform = Int::toString)).done()
        }
    }

    private val pathAlternative2: PathHandler = path("/books") {

        post {
            val author = queryParameters["author"]?.string() ?: return@post badRequest("Missing author").done()
            val title = queryParameters["title"]?.string() ?: return@post badRequest("Missing title").done()
            val id = (books.keys.maxOrNull() ?: 0) + 1
            books += id to Book(author, title)
            created(id.toString()).done()
        }

        path("/{id}") {
            get {
                val bookId = pathParameters.require("id").toInt()
                val book = books[bookId]
                if (book != null)
                    ok("Title: ${book.title}, Author: ${book.author}").done()
                else
                    notFound("Book not found").done()
            }

            put {
                val bookId = pathParameters.require("id").toInt()
                val book = books[bookId]
                if (book != null) {
                    books += bookId to book.copy(
                        author = queryParameters["author"]?.string() ?: book.author,
                        title = queryParameters["title"]?.string() ?: book.title
                    )

                    ok("Book with id '$bookId' updated").done()
                }
                else {
                    notFound("Book not found").done()
                }
            }

            delete {
                val bookId = pathParameters.require("id").toInt()
                val book = books[bookId]
                books -= bookId
                if (book != null)
                    ok("Book with id '$bookId' deleted").done()
                else
                    notFound("Book not found").done()
            }

            // Matches path's requests with *any* HTTP method as a fallback (return 404 instead 405)
            after(ALL - DELETE - PUT - GET, status = NOT_FOUND_404) {
                send(METHOD_NOT_ALLOWED_405).done()
            }
        }

        get {
            ok(books.keys.joinToString(" ", transform = Int::toString)).done()
        }
    }

    override val handler: HttpHandler = path {
        path("/a", path)
        path("/b", pathAlternative)
        path("/c", pathAlternative2)
    }

    @Test fun `Create book returns 201 and new book ID`() {
        listOf("/a", "/b", "/c").forEach {
            val result = client.post("$it/books?author=Vladimir%20Nabokov&title=Lolita")
            assert(Integer.valueOf(result.body as String) > 0)
            assertEquals(CREATED_201, result.status)
        }
    }

    @Test fun `Create book returns 400 if a parameter is missing`() {
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
    }

    @Test fun `List books contains all books IDs`() {
        listOf("/a", "/b", "/c").forEach {
            val result = client.get("$it/books")
            assertResponseContains(result, "100", "101")
        }
    }

    @Test fun `Get book returns all book's fields`() {
        listOf("/a", "/b", "/c").forEach {
            val result = client.get("$it/books/101")
            assertResponseContains(result, "William Shakespeare", "Hamlet")
        }
    }

    @Test fun `Update book overrides existing book data`() {
        listOf("/a", "/b", "/c").forEach {
            val resultPut = client.put("$it/books/100?title=Don%20Quixote")
            assertResponseContains(resultPut, "100", "updated")

            val resultGet = client.get("$it/books/100")
            assertResponseContains(resultGet, "Miguel de Cervantes", "Don Quixote")
        }
    }

    @Test fun `Update not found book returns a 404`() {
        listOf("/a", "/b", "/c").forEach {
            val result = client.put("$it/books/9999?title=Don%20Quixote")
            assertResponseContains(result, NOT_FOUND_404, "not found")
        }
    }

    @Test fun `Delete book returns the deleted record ID`() {
        listOf("/a", "/b", "/c").forEach {
            val createResult =
                client.post("$it/books?author=Ken%20Follett&title=The%20Pillars%20of%20the%20Earth")
            val id = Integer.valueOf(createResult.body as String)
            assert(id > 0)
            assertEquals(CREATED_201, createResult.status)
            val result = client.delete("$it/books/$id")
            assertResponseContains(result, id.toString(), "deleted")
        }
    }

    @Test fun `Delete not found book returns a 404`() {
        listOf("/a", "/b", "/c").forEach {
            val result = client.delete("$it/books/9999")
            assertResponseContains(result, NOT_FOUND_404, "not found")
        }
    }

    @Test fun `Book not found returns a 404`() {
        listOf("/a", "/b", "/c").forEach {
            val result = client.get("$it/books/9999")
            assertResponseContains(result, NOT_FOUND_404, "not found")
        }
    }

    @Test fun `Invalid method returns 405`() {
        listOf("/a", "/b", "/c").forEach {
            val result = client.options("$it/books/9999")
            assertEquals(METHOD_NOT_ALLOWED_405, result.status)
        }
    }

    @Test fun `Not handled method returns 404`() {
        listOf("/a", "/b", "/c").forEach {
            val result = client.options("$it/books")
            assertEquals(NOT_FOUND_404, result.status)
        }
    }
}
