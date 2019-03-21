package com.hexagonkt.web.examples

import com.hexagonkt.helpers.Logger
import com.hexagonkt.http.client.Client
import com.hexagonkt.http.server.Server
import com.hexagonkt.http.server.ServerPort
import com.hexagonkt.serialization.parse
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

/**
 * TODO Parse post body with JSON/YAML
 * TODO Serialize response with accept content type
 * TODO Use templates
 */
@Test abstract class TodoTest(adapter: ServerPort) {

    // sample
    val log: Logger = Logger(TodoTest::class)

    data class Task(val number: Int, val title: String, val description: String)

    private val taskList = listOf(Task(1, "Don Quixote", "Miguel de Cervantes"))

    private val tasks: MutableMap<Int, Task> =
        LinkedHashMap(taskList.map { it.number to it }.toMap())

    private val server: Server by lazy {
        Server(adapter) {
            path("/tasks") {
                post {
                    val task = request.body.parse(Task::class, requestFormat)
                    tasks += task.number to task
                    send(201, task.number)
                }

                put {
                    val task = request.body.parse(Task::class, requestFormat)
                    tasks += task.number to task
                    ok("Book with id '${task.number}' updated")
                }

                path("/{id}") {
                    patch {
                        val bookId = pathParameters["id"].toInt()
                        val book = tasks[bookId]
                        if (book != null) {
                            tasks += bookId to book.copy(
                                description = parameters["author"]?.first() ?: book.description,
                                title = parameters["title"]?.first() ?: book.title
                            )

                            ok("Book with id '$bookId' updated")
                        }
                        else {
                            send(404, "Book not found")
                        }
                    }

                    get {
                        val taskId = pathParameters["id"].toInt()
                        val task = tasks[taskId]
                        if (task != null)
                            ok(task, responseFormat)
                        else
                            send(404, "Task: $taskId not found")
                    }

                    delete {
                        val bookId = pathParameters["id"].toInt()
                        val book = tasks[bookId]
                        tasks -= bookId
                        if (book != null)
                            ok("Book with id '$bookId' deleted")
                        else
                            send(404, "Book not found")
                    }
                }

                get {
                    ok(tasks.keys.joinToString(" ", transform = Int::toString))
                }
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

//    @Test fun createBook() {
//        val result = client.post("/books?author=Vladimir%20Nabokov&title=Lolita")
//        assert(Integer.valueOf(result.responseBody) > 0)
//        assert(201 == result.statusCode)
//    }
//
//    @Test fun listBooks() {
//        val result = client.get("/books")
//        assertResponseContains(result, "100", "101")
//    }
//
//    @Test fun getBook() {
//        val result = client.get("/books/101")
//        assertResponseContains(result, "William Shakespeare", "Hamlet")
//    }
//
//    @Test fun updateBook() {
//        val resultPut = client.put("/books/100?title=Don%20Quixote")
//        assertResponseContains(resultPut, "100", "updated")
//
//        val resultGet = client.get("/books/100")
//        assertResponseContains(resultGet, "Miguel de Cervantes", "Don Quixote")
//    }
//
//    @Test fun deleteBook() {
//        val result = client.delete("/books/102")
//        assertResponseContains(result, "102", "deleted")
//    }
//
//    @Test fun bookNotFound() {
//        val result = client.get("/books/9999")
//        assertResponseContains(result, 404, "not found")
//    }
//
//    @Test fun invalidMethodReturns405() {
//        val result = client.options("/books/9999")
//        assert(405 == result.statusCode)
//    }
//
//    private fun assertResponseContains(response: Response?, status: Int, vararg content: String) {
//        assert(response?.statusCode == status)
//        content.forEach {
//            assert(response?.responseBody?.contains(it) ?: false)
//        }
//    }
//
//    private fun assertResponseContains(response: Response?, vararg content: String) {
//        assertResponseContains(response, 200, *content)
//    }
}
