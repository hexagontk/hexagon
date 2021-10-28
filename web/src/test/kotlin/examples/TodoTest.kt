package com.hexagonkt.web.examples

import com.hexagonkt.helpers.require
import com.hexagonkt.http.client.Client
import com.hexagonkt.http.client.Response
import com.hexagonkt.http.client.ahc.AhcAdapter
import com.hexagonkt.http.server.Server
import com.hexagonkt.http.server.ServerPort
import com.hexagonkt.http.server.ServerSettings
import com.hexagonkt.logging.Logger
import com.hexagonkt.serialization.json.JacksonMapper
import com.hexagonkt.serialization.json.Json
import com.hexagonkt.serialization.SerializationManager
import com.hexagonkt.serialization.parse
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS

/**
 * TODO Use templates
 */
@TestInstance(PER_CLASS)
abstract class TodoTest(adapter: ServerPort) {

    // sample
    private val log: Logger = Logger(TodoTest::class)

    data class Task(val number: Int, val title: String, val description: String)

    private val taskList = listOf(
        Task(1, "Simple Task", "Piece of cake"),
        Task(102, "A Task", "Something to do"),
        Task(103, "Second Task", "More things to do")
    )

    private val tasks: MutableMap<Int, Task> =
        LinkedHashMap(taskList.map { it.number to it }.toMap())

    private val server: Server by lazy {
        Server(adapter, ServerSettings(bindPort = 0)) {
            before { log.debug { "Start" } }
            after { log.debug { "End" } }

            path("/tasks") {
                post {
                    val task = request.body.parse(Task::class, requestFormat)
                    tasks += task.number to task
                    send(201, task.number)
                }

                put {
                    val task = request.body.parse(Task::class, requestFormat)
                    tasks += task.number to task
                    ok("Task with id '${task.number}' updated")
                }

                path("/{id}") {
                    patch {
                        val taskId = pathParameters.require("id").toInt()
                        val task = tasks[taskId]
                        val fields = request.body.parse<Map<*, *>>(requestFormat)
                        if (task != null) {
                            tasks += taskId to task.copy(
                                number = fields["number"] as? Int ?: task.number,
                                title = fields["title"] as? String ?: task.title,
                                description = fields["description"] as? String ?: task.description
                            )

                            ok("Task with id '$taskId' updated")
                        }
                        else {
                            send(404, "Task not found")
                        }
                    }

                    get {
                        val taskId = pathParameters.require("id").toInt()
                        val task = tasks[taskId]
                        if (task != null)
                            ok(task, responseFormat)
                        else
                            send(404, "Task: $taskId not found")
                    }

                    delete {
                        val taskId = pathParameters.require("id").toInt()
                        val task = tasks[taskId]
                        tasks -= taskId
                        if (task != null)
                            ok("Task with id '$taskId' deleted")
                        else
                            send(404, "Task not found")
                    }
                }

                get { ok(tasks.values, responseFormat) }
            }
        }
    }
    // sample

    private val client: Client by lazy {
        Client(AhcAdapter(), "http://localhost:${server.runtimePort}")
    }

    @BeforeAll fun initialize() {
        SerializationManager.mapper = JacksonMapper
        SerializationManager.formats = linkedSetOf(Json)
        server.start()
    }

    @AfterAll fun shutdown() {
        server.stop()
    }

    @Test fun `Create task`() {
        val body = Task(101, "Tidy Things", "Tidy everything")
        val result = client.post("/tasks", body, Json.contentType)
        assert(Integer.valueOf(result.body) == 101)
        assert(201 == result.status)
    }

    @Test fun `List tasks`() {
        client.post("/tasks", Task(101, "Tidy Things", "Tidy everything"), Json.contentType)
        val result = client.get("/tasks")
        assertResponseContains(result, "1", "101")
    }

    @Test fun `Get task`() {
        val result = client.get("/tasks/101")
        assertResponseContains(result, "Tidy Things", "Tidy everything")
    }

    @Test fun `Update task`() {
        val body = Task(103, "Changed Task", "Change of plans")
        val resultPut = client.put("/tasks", body, Json.contentType)
        assertResponseContains(resultPut, "103", "updated")

        val resultGet = client.get("/tasks/103")
        assertResponseContains(resultGet, "Changed Task", "Change of plans")
    }

    @Test fun `Delete task`() {
        val result = client.delete("/tasks/102")
        assertResponseContains(result, "102", "deleted")
    }

    @Test fun `Task not found`() {
        val result = client.get("/tasks/9999")
        assertResponseContains(result, 404, "not found")
    }

    private fun assertResponseContains(response: Response?, status: Int, vararg content: String) {
        assert(response?.status == status)
        content.forEach {
            assert(response?.body?.contains(it) ?: false)
        }
    }

    private fun assertResponseContains(response: Response?, vararg content: String) {
        assertResponseContains(response, 200, *content)
    }
}
