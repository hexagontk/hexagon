package co.there4.hexagon.server.integration

import co.there4.hexagon.server.Router
import co.there4.hexagon.server.ServerEngine
import java.util.*
import java.util.Collections.synchronizedMap

@Suppress("unused") // Test methods are flagged as unused
class BooksIT(serverEngine: ServerEngine) : ItTest (serverEngine) {
    data class Book (val author: String, val title: String)

    private var id = 1
    private var books: MutableMap<Int, Book> = LinkedHashMap ()

    fun initBooks () {
        books = synchronizedMap(LinkedHashMap(mapOf(
            100 to Book ("Miguel_de_Cervantes", "Don_Quixote"),
            101 to Book ("William_Shakespeare", "Hamlet"),
            102 to Book ("Homer", "The_Odyssey")
        )))
    }

    override fun Router.initialize() {
        post ("/books") {
            books [id] = Book (request.parameter("author"), request.parameter("title"))
            created ((id++).toString ())
        }

        get ("/books/{id}") {
            val bookId = request.parameter("id").toInt()
            val book = books [bookId]
            if (book != null)
                ok ("Title: ${book.title}, Author: ${book.author}")
            else
                error (404, "Book not found")
        }

        put ("/books/{id}") {
            val bookId = request.parameter("id").toInt()
            val book = books[bookId]
            if (book != null) {
                books.put(
                    bookId,
                    book.copy (
                        author = request.parameters ["author"]?.first() ?: book.author,
                        title = request.parameters ["title"]?.first() ?: book.title
                    )
                )

                ok ("Book with id '$bookId' updated")
            }
            else {
                error (404, "Book not found")
            }
        }

        delete ("/books/{id}") {
            val bookId = request.parameter("id").toInt()
            val book = books.remove (bookId)
            if (book != null)
                ok ("Book with id '$bookId' deleted")
            else
                error (404, "Book not found")
        }

        get ("/books") {
            ok (books.keys.map(Int::toString).joinToString(" "))
        }
    }

    fun createBook () {
        withClients {
            val result = post ("/books?author=Vladimir_Nabokov&title=Lolita")
            assert (Integer.valueOf (result.responseBody) > 0)
            assert (201 == result.statusCode)
        }
    }

    fun listBooks () {
        withClients {
            val result = get ("/books")
            assertResponseContains(result, 200, "100", "101")
        }
    }

    fun getBook () {
        withClients {
            val result = get ("/books/101")
            assertResponseContains (result, 200, "William_Shakespeare", "Hamlet")
        }
    }

    fun updateBook () {
        withClients {
            val resultPut = put ("/books/100?title=Don_Quixote")
            assertResponseContains (resultPut, 200, "100", "updated")

            val resultGet = get ("/books/100")
            assertResponseContains (resultGet, 200, "Miguel_de_Cervantes", "Don_Quixote")
        }
    }

    fun deleteBook () {
        withClients {
            initBooks ()
            val result = delete ("/books/102")
            assertResponseContains (result, 200, "102", "deleted")
            books.put (102, Book ("Homer", "The_Odyssey")) // Restore book for next tests
        }
    }

    fun bookNotFound () {
        withClients {
            val result = get ("/books/9999")
            assertResponseContains(result, 404, "not found")
        }
    }

    fun invalidMethodReturns405 () {
        withClients {
            val result = options ("/books/9999")
            assert (405 == result.statusCode)
        }
    }

    override fun validate() {
        createBook()
        listBooks()
        getBook()
        updateBook()
        deleteBook()
        bookNotFound()
        invalidMethodReturns405()
    }
}
