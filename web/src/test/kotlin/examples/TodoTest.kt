package com.hexagonkt.web.examples

import com.hexagonkt.core.converters.ConvertersManager
import com.hexagonkt.core.converters.convert
import com.hexagonkt.core.require
import com.hexagonkt.core.requireKeys
import com.hexagonkt.core.logging.Logger
import com.hexagonkt.core.logging.LoggingLevel.DEBUG
import com.hexagonkt.core.logging.LoggingManager
import com.hexagonkt.core.logging.jul.JulLoggingAdapter
import com.hexagonkt.core.media.ApplicationMedia.JSON
import com.hexagonkt.http.client.HttpClient
import com.hexagonkt.http.client.HttpClientSettings
import com.hexagonkt.http.client.jetty.JettyClientAdapter
import com.hexagonkt.http.client.model.HttpClientResponse
import com.hexagonkt.http.model.ClientErrorStatus.NOT_FOUND
import com.hexagonkt.http.model.ContentType
import com.hexagonkt.http.model.HttpStatus
import com.hexagonkt.http.model.SuccessStatus.CREATED
import com.hexagonkt.http.model.SuccessStatus.OK
import com.hexagonkt.http.server.HttpServer
import com.hexagonkt.http.server.HttpServerPort
import com.hexagonkt.http.server.HttpServerSettings
import com.hexagonkt.serialization.SerializationManager
import com.hexagonkt.serialization.jackson.json.Json
import com.hexagonkt.serialization.parse
import com.hexagonkt.serialization.serialize
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.net.URL
import kotlin.test.assertEquals

/**
 * TODO Use templates
 */
@TestInstance(PER_CLASS)
abstract class TodoTest(adapter: HttpServerPort) {

    // sample
    private val log: Logger = Logger(TodoTest::class)

    data class Task(val number: Int, val title: String, val description: String)

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
                val e = context.exception
                log.error(e) { "Internal error" }
                internalServerError(e?.message ?: "Internal error")
            }

            path("/tasks") {
                post {
                    val task = request.bodyString().parse(Json).convert<Task>()
                    tasks += task.number to task
                    send(CREATED, task.number.toString())
                }

                put {
                    val task = request.bodyString().parse(Json).convert<Task>()
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
                            send(NOT_FOUND, "Task not found")
                        }
                    }

                    get {
                        val taskId = pathParameters.require("id").toInt()
                        val task = tasks[taskId]
                        if (task != null)
                            ok(
                                body = task.convert(Map::class).serialize(Json),
                                contentType = ContentType(JSON)
                            )
                        else
                            send(NOT_FOUND, "Task: $taskId not found")
                    }

                    delete {
                        val taskId = pathParameters.require("id").toInt()
                        val task = tasks[taskId]
                        tasks -= taskId
                        if (task != null)
                            ok("Task with id '$taskId' deleted")
                        else
                            send(NOT_FOUND, "Task not found")
                    }
                }

                get {
                    val body = tasks.values.map { it.convert(Map::class) }.serialize(Json)
                    ok(body, contentType = ContentType(JSON))
                }
            }
        }
    }
    // sample

    private val client: HttpClient by lazy {
        HttpClient(
            JettyClientAdapter(),
            HttpClientSettings(
                baseUrl = URL("http://localhost:${server.runtimePort}"),
                contentType = ContentType(JSON)
            )
        )
    }

    @BeforeAll fun initialize() {
        SerializationManager.formats = linkedSetOf(Json)
        LoggingManager.adapter = JulLoggingAdapter()
        LoggingManager.setLoggerLevel("com.hexagonkt", DEBUG)
        ConvertersManager.register(Task::class to Map::class) {
            mapOf(
                Task::description.name to it.description,
                Task::number.name to it.number,
                Task::title.name to it.title,
            )
        }
        ConvertersManager.register(Map::class to Task::class) {
            Task(
                description = it.requireKeys(Task::description.name),
                number = it.requireKeys(Task::number.name),
                title = it.requireKeys(Task::title.name),
            )
        }
        server.start()
        client.start()
    }

    @AfterAll fun shutdown() {
        server.stop()
        client.stop()
    }

    @Test fun `Create task`() = runBlocking {
        val body = Task(101, "Tidy Things", "Tidy everything").serialize(Json)
        val result = client.post("/tasks", body)
        assert(Integer.valueOf(result.bodyString()) == 101)
        assert(CREATED == result.status)
    }

    @Test fun `List tasks`() = runBlocking {
        val body = Task(101, "Tidy Things", "Tidy everything").serialize(Json)
        assertResponseContains(client.post("/tasks", body), CREATED)
        val result = client.get("/tasks")
        assertResponseContains(result, "1", "101")
    }

    @Test fun `Get task`() = runBlocking {
        val result = client.get("/tasks/101")
        assertResponseContains(result, "Tidy Things", "Tidy everything")
    }

    @Test fun `Update task`() = runBlocking {
        val body = Task(103, "Changed Task", "Change of plans").serialize(Json)
        val resultPut = client.put("/tasks", body)
        assertResponseContains(resultPut, "103", "updated")

        val resultGet = client.get("/tasks/103")
        assertResponseContains(resultGet, "Changed Task", "Change of plans")
    }

    @Test fun `Delete task`() = runBlocking {
        val result = client.delete("/tasks/102")
        assertResponseContains(result, "102", "deleted")
    }

    @Test fun `Task not found`() = runBlocking {
        val result = client.get("/tasks/9999")
        assertResponseContains(result, NOT_FOUND, "not found")
    }

    private fun assertResponseContains(
        response: HttpClientResponse?, status: HttpStatus, vararg content: String) {

        assertEquals(status, response?.status)
        content.forEach {
            assert(response?.bodyString()?.contains(it) ?: false)
        }
    }

    private fun assertResponseContains(response: HttpClientResponse?, vararg content: String) {
        assertResponseContains(response, OK, *content)
    }
}
