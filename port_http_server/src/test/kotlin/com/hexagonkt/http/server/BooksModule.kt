package com.hexagonkt.http.server

import com.hexagonkt.http.client.Client
import java.util.*
import java.util.Collections.synchronizedMap

@Suppress("unused", "MemberVisibilityCanPrivate") // Test methods are flagged as unused
internal class BooksModule : TestModule() {
    data class Book (val author: String, val title: String)

    private var id = 1
    private var books: MutableMap<Int, Book> = LinkedHashMap ()

    private fun initBooks () {
        books = synchronizedMap(linkedMapOf(
            100 to Book("Miguel_de_Cervantes", "Don_Quixote"),
            101 to Book("William_Shakespeare", "Hamlet"),
            102 to Book("Homer", "The_Odyssey")
        ))
    }

    override fun initialize(): Router = Router {
        post ("/books") {
            books [id] = Book(request.requireSingleParameter("author"), request.requireSingleParameter("title"))
            send (201, (id++).toString ())
        }

        get("/books/{id}") {
            val bookId = request.pathParameter("id").toInt()
            val book = books [bookId]
            if (book != null)
                ok ("Title: ${book.title}, Author: ${book.author}")
            else
                send (404, "Book not found")
        }

        put("/books/{id}") {
            val bookId = request.pathParameter("id").toInt()
            val book = books[bookId]
            if (book != null) {
                books[bookId] = book.copy (
                    author = request.parameters["author"]?.first() ?: book.author,
                    title = request.parameters["title"]?.first() ?: book.title
                )

                ok("Book with id '$bookId' updated")
            }
            else {
                send(404, "Book not found")
            }
        }

        delete ("/books/{id}") {
            val bookId = request.pathParameter("id").toInt()
            val book = books.remove (bookId)
            if (book != null)
                ok ("Book with id '$bookId' deleted")
            else
                send(404, "Book not found")
        }

        get ("/books") {
            ok (books.keys.joinToString(" ", transform = Int::toString))
        }
    }

    fun createBook (client: Client) {
        val result = client.post ("/books?author=Vladimir_Nabokov&title=Lolita")
        assert (Integer.valueOf (result.responseBody) > 0)
        assert (201 == result.statusCode)
    }

    fun listBooks (client: Client) {
        val result = client.get ("/books")
        assertResponseContains(result, "100", "101")
    }

    fun getBook (client: Client) {
        val result = client.get ("/books/101")
        assertResponseContains (result, "William_Shakespeare", "Hamlet")
    }

    fun updateBook (client: Client) {
        val resultPut = client.put ("/books/100?title=Don_Quixote")
        assertResponseContains (resultPut, "100", "updated")

        val resultGet = client.get ("/books/100")
        assertResponseContains (resultGet, "Miguel_de_Cervantes", "Don_Quixote")
    }

    fun deleteBook (client: Client) {
        val result = client.delete ("/books/102")
        assertResponseContains (result, "102", "deleted")
        books.put (102, Book("Homer", "The_Odyssey")) // Restore book for next tests
    }

    fun bookNotFound (client: Client) {
        val result = client.get ("/books/9999")
        assertResponseContains(result, 404, "not found")
    }

    fun invalidMethodReturns405 (client: Client) {
        val result = client.options ("/books/9999")
        assert (405 == result.statusCode)
    }

    override fun validate(client: Client) {
        initBooks ()
        createBook(client)
        client.cookies.clear()
        initBooks ()
        listBooks(client)
        client.cookies.clear()
        initBooks ()
        getBook(client)
        client.cookies.clear()
        initBooks ()
        updateBook(client)
        client.cookies.clear()
        initBooks ()
        deleteBook(client)
        client.cookies.clear()
        initBooks ()
        bookNotFound(client)
        client.cookies.clear()
        initBooks ()
        invalidMethodReturns405(client)
    }
}
