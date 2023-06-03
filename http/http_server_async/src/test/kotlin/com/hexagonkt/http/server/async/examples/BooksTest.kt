package com.hexagonkt.http.server.async.examples

import com.hexagonkt.core.fail
import com.hexagonkt.core.require
import com.hexagonkt.handlers.async.done
import com.hexagonkt.http.model.*
import com.hexagonkt.http.model.HttpMethod.*
import com.hexagonkt.http.model.HttpMethod.Companion.ALL
import com.hexagonkt.http.model.CREATED_201
import com.hexagonkt.http.handlers.async.HttpContext
import com.hexagonkt.http.handlers.async.PathHandler
import com.hexagonkt.http.handlers.async.path
import kotlin.test.Test
import kotlin.test.assertEquals

internal class BooksTest {

    data class Book(val author: String, val title: String)

    private val books: MutableMap<Int, Book> = linkedMapOf(
        100 to Book("Miguel de Cervantes", "Don Quixote"),
        101 to Book("William Shakespeare", "Hamlet"),
        102 to Book("Homer", "The Odyssey")
    )

    private fun HttpContext.missingField(field: String): HttpContext =
        badRequest("Missing $field")

    val path: PathHandler = path {

        post("/books") {
            val author = queryParameters["author"]?.string() ?: return@post missingField("author").done()
            val title = queryParameters["title"]?.string() ?: return@post missingField("title").done()
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

    private val pathAlternative: PathHandler = path("/books") {

        post {
            val author = queryParameters["author"]?.string() ?: return@post missingField("author").done()
            val title = queryParameters["title"]?.string() ?: return@post missingField("title").done()
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
            val author = queryParameters["author"]?.string() ?: return@post missingField("author").done()
            val title = queryParameters["title"]?.string() ?: return@post missingField("title").done()
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

    @Test fun `Create book returns 201 and new book ID`() {
        listOf(
            path,
            pathAlternative,
            pathAlternative2,
            path.byMethod()[POST] ?: fail,
            pathAlternative.byMethod()[POST] ?: fail,
            pathAlternative2.byMethod()[POST] ?: fail,
        ).forEach {
            val result = it.send(POST, "/books", "author=Vladimir%20Nabokov&title=Lolita")
            assert(Integer.valueOf(result.body as String) > 0)
            assertEquals(CREATED_201, result.status)
        }
    }

    @Test fun `Create book returns 400 if a parameter is missing`() {
        listOf(path, pathAlternative).forEach { p ->
            p.send(POST, "/books", "title=Lolita").let {
                assertEquals("Missing author", it.body)
                assertEquals(BAD_REQUEST_400, it.status)
            }

            p.send(POST, "/books", "author=Vladimir%20Nabokov").let {
                assertEquals("Missing title", it.body)
                assertEquals(BAD_REQUEST_400, it.status)
            }
        }
    }

    @Test fun `List books contains all books IDs`() {
        listOf(path, pathAlternative).forEach {
            val result = it.send(GET, "/books")
            assertResponseContains(result, "100", "101")
        }
    }

    @Test fun `Get book returns all book's fields`() {
        listOf(path, pathAlternative).forEach {
            val result = it.send(GET, "/books/101")
            assertResponseContains(result, "William Shakespeare", "Hamlet")
        }
    }

    @Test fun `Update book overrides existing book data`() {
        listOf(path, pathAlternative).forEach {
            val resultPut = it.send(PUT, "/books/100", "title=Don%20Quixote")
            assertResponseContains(resultPut, "100", "updated")

            val resultGet = it.send(GET, "/books/100")
            assertResponseContains(resultGet, "Miguel de Cervantes", "Don Quixote")
        }
    }

    @Test fun `Delete book returns the deleted record ID`() {
        listOf(path, pathAlternative).forEach {
            val createResult = it.send(
                POST, "/books", "author=Ken%20Follett&title=The%20Pillars%20of%20the%20Earth"
            )
            val id = Integer.valueOf(createResult.body as String)
            assert(id > 0)
            assertEquals(CREATED_201, createResult.status)
            val result = it.send(DELETE, "/books/$id")
            assertResponseContains(result, id.toString(), "deleted")
        }
    }

    @Test fun `Book not found returns a 404`() {
        listOf(path, pathAlternative).forEach {
            val result = it.send(GET, "/books/9999")
            assertResponseContains(result, NOT_FOUND_404, "not found")
        }
    }

    @Test fun `Invalid method returns 405`() {
        listOf(path, pathAlternative).forEach {
            val result = it.send(OPTIONS, "/books/9999")
            assertEquals(METHOD_NOT_ALLOWED_405, result.status)
        }
    }
}
