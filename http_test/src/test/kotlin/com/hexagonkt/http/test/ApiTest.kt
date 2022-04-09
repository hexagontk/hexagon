package com.hexagonkt.http.test

import com.hexagonkt.core.media.ApplicationMedia.JSON
import com.hexagonkt.core.media.TextMedia
import com.hexagonkt.core.require
import com.hexagonkt.http.client.jetty.JettyClientAdapter
import com.hexagonkt.serialization.SerializationManager
import com.hexagonkt.http.model.ClientErrorStatus.NOT_FOUND
import com.hexagonkt.http.model.ContentType
import com.hexagonkt.http.model.SuccessStatus.OK
import com.hexagonkt.http.server.HttpServer
import com.hexagonkt.http.server.callbacks.FileCallback
import com.hexagonkt.http.server.handlers.PathHandler
import com.hexagonkt.http.server.handlers.path
import com.hexagonkt.http.server.jetty.serve
import com.hexagonkt.serialization.jackson.json.Json
import com.hexagonkt.serialization.parse
import com.hexagonkt.serialization.serialize
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.io.File
import java.nio.file.Path
import kotlin.test.assertEquals

@TestInstance(PER_CLASS)
class ApiTest {
    private var dynamicPath: PathHandler = PathHandler()

    private val server: HttpServer by lazy {
        val servedPath: Path = File(System.getProperty("user.dir")).toPath()

        SerializationManager.formats = linkedSetOf(Json)

        serve {
            after(pattern = "*", status = NOT_FOUND) {
                // Dynamic resolution picking from dynamicPath: PathHandler
                copy(
                    context = context.copy(
                        event = context.event.copy(
                            response = dynamicPath.process(request)
                        )
                    )
                )
            }

            get("/dir/*", FileCallback(servedPath.toFile()))

            get("/hello/{name}") {
                val name = pathParameters["name"]

                ok("Hello, $name!", contentType = ContentType(TextMedia.PLAIN))
            }

            put("/data/{id}") {
                val id = pathParameters.require("id")
                val data = request.bodyString().parse()
                val content = mapOf(id to data)

                ok(content.serialize(Json), contentType = ContentType(JSON))
            }
        }
    }

    @AfterAll fun `Set up mock services`() {
        server.stop()
    }

    @Test fun `Do HTTP requests`() {
        val http = Http("http://localhost:${server.runtimePort}", JettyClientAdapter())
        http.get("/hello/mike")
        assertEquals(OK, http.responseOrNull?.status)
    }

    @Test fun `Mock HTTP response`() {
        dynamicPath = path {
            get("/foo") {
                ok("dynamic")
            }
        }

        val http = Http("http://localhost:${server.runtimePort}", JettyClientAdapter())
        http.get("/foo")
        assertEquals(OK, http.responseOrNull?.status)
        assertEquals("dynamic", http.responseOrNull?.body)
        dynamicPath = path {
            get("/foo") {
                ok("changed")
            }
        }
        http.get("/foo")
        assertEquals(OK, http.responseOrNull?.status)
        assertEquals(OK, http.response.status)
        assertEquals("changed", http.responseOrNull?.body)
        assertEquals("changed", http.response.body)
    }
}
