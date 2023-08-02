package com.hexagonkt.web.examples

import com.hexagonkt.core.fieldsMapOfNotNull
import com.hexagonkt.core.require
import com.hexagonkt.core.requirePath
import com.hexagonkt.core.logging.Logger
import com.hexagonkt.core.logging.LoggingLevel.DEBUG
import com.hexagonkt.core.logging.LoggingManager
import com.hexagonkt.core.media.APPLICATION_JSON
import com.hexagonkt.core.urlOf
import com.hexagonkt.logging.jul.JulLoggingAdapter
import com.hexagonkt.http.client.HttpClient
import com.hexagonkt.http.client.HttpClientSettings
import com.hexagonkt.http.client.jetty.JettyClientAdapter
import com.hexagonkt.http.model.*
import com.hexagonkt.http.server.HttpServer
import com.hexagonkt.http.server.HttpServerPort
import com.hexagonkt.http.server.HttpServerSettings
import com.hexagonkt.serialization.*
import com.hexagonkt.serialization.jackson.json.Json
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import kotlin.test.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import kotlin.test.assertEquals

/**
 * TODO Use templates
 */
@TestInstance(PER_CLASS)
abstract class TodoTest(adapter: HttpServerPort) {

    // sample
    private val log: Logger = Logger(TodoTest::class)

    data class Task(
        val number: Int = 0,
        val title: String = "",
        val description: String = ""
    ) : Data<Task> {
        override fun data(): Map<String, *> =
            fieldsMapOfNotNull(
                Task::description to description,
                Task::number to number,
                Task::title to title,
            )

        override fun with(data: Map<String, *>): Task =
            Task(
                description = data.requirePath(Task::description),
                number = data.requirePath(Task::number),
                title = data.requirePath(Task::title),
            )
    }

    private val taskList = listOf(
        Task(1, "Simple Task", "Piece of cake"),
        Task(102, "A Task", "Something to do"),
        Task(103, "Second Task", "More things to do")
    )

    private val tasks: MutableMap<Int, Task> =
        LinkedHashMap(taskList.associateBy { it.number })

    private val server: HttpServer by lazy {
        HttpServer(adapter, HttpServerSettings(bindPort = 0)) {
            filter("/*") {
                log.debug { "Start" }
                val next = next()
                log.debug { "End" }
                next
            }

            after(pattern = "/*", exception = Exception::class) {
                log.error(exception) { "Internal error" }
                internalServerError(exception?.message ?: "Internal error")
            }

            path("/tasks") {
                post {
                    val task = Task().with(request.bodyString().parseMap(Json))
                    tasks += task.number to task
                    send(CREATED_201, task.number.toString())
                }

                put {
                    val task = Task().with(request.bodyString().parseMap(Json))
                    tasks += task.number to task
                    ok("Task with id '${task.number}' updated")
                }

                path("/{id}") {
                    patch {
                        val taskId = pathParameters.require("id").toInt()
                        val task = tasks[taskId]
                        val fields = request.bodyString().parse(Json) as Map<*, *>
                        if (task != null) {
                            tasks += taskId to task.copy(
                                number = fields["number"] as? Int ?: task.number,
                                title = fields["title"] as? String ?: task.title,
                                description = fields["description"] as? String ?: task.description
                            )

                            ok("Task with id '$taskId' updated")
                        }
                        else {
                            send(NOT_FOUND_404, "Task not found")
                        }
                    }

                    get {
                        val taskId = pathParameters.require("id").toInt()
                        val task = tasks[taskId]
                        if (task != null)
                            ok(
                                body = task.data().serialize(Json),
                                contentType = ContentType(APPLICATION_JSON)
                            )
                        else
                            send(NOT_FOUND_404, "Task: $taskId not found")
                    }

                    delete {
                        val taskId = pathParameters.require("id").toInt()
                        val task = tasks[taskId]
                        tasks -= taskId
                        if (task != null)
                            ok("Task with id '$taskId' deleted")
                        else
                            send(NOT_FOUND_404, "Task not found")
                    }
                }

                get {
                    val body = tasks.values.map { it.data() }.serialize(Json)
                    ok(body, contentType = ContentType(APPLICATION_JSON))
                }
            }
        }
    }
    // sample

    private val client: HttpClient by lazy {
        HttpClient(
            JettyClientAdapter(),
            HttpClientSettings(
                baseUrl = urlOf("http://localhost:${server.runtimePort}"),
                contentType = ContentType(APPLICATION_JSON)
            )
        )
    }

    @BeforeAll fun initialize() {
        SerializationManager.formats = linkedSetOf(Json)
        LoggingManager.adapter = JulLoggingAdapter()
        LoggingManager.setLoggerLevel("com.hexagonkt", DEBUG)
        server.start()
        client.start()
    }

    @AfterAll fun shutdown() {
        server.stop()
        client.stop()
    }

    @Test fun `Create task`() {
        val body = Task(101, "Tidy Things", "Tidy everything").serialize(Json)
        val result = client.post("/tasks", body)
        assert(Integer.valueOf(result.bodyString()) == 101)
        assert(CREATED_201 == result.status)
    }

    @Test fun `List tasks`() {
        val body = Task(101, "Tidy Things", "Tidy everything").serialize(Json)
        assertResponseContains(client.post("/tasks", body), CREATED_201)
        val result = client.get("/tasks")
        assertResponseContains(result, "1", "101")
    }

    @Test fun `Get task`() {
        val result = client.get("/tasks/101")
        assertResponseContains(result, "Tidy Things", "Tidy everything")
    }

    @Test fun `Update task`() {
        val body = Task(103, "Changed Task", "Change of plans").serialize(Json)
        val resultPut = client.put("/tasks", body)
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
        assertResponseContains(result, NOT_FOUND_404, "not found")
    }

    private fun assertResponseContains(
        response: HttpResponsePort?, status: HttpStatus, vararg content: String) {

        assertEquals(status, response?.status)
        content.forEach {
            assert(response?.bodyString()?.contains(it) ?: false)
        }
    }

    private fun assertResponseContains(response: HttpResponsePort?, vararg content: String) {
        assertResponseContains(response, OK_200, *content)
    }
}
