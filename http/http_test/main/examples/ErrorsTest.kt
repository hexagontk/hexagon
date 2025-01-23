package com.hexagontk.http.test.examples

import com.hexagontk.core.fail
import com.hexagontk.http.client.HttpClientPort
import com.hexagontk.http.model.INTERNAL_SERVER_ERROR_500
import com.hexagontk.http.server.HttpServerPort
import com.hexagontk.http.server.HttpServerSettings
import com.hexagontk.http.handlers.PathHandler
import com.hexagontk.http.handlers.HttpHandler
import com.hexagontk.http.handlers.path
import com.hexagontk.http.model.Header
import com.hexagontk.http.test.BaseTest
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals

@Suppress("FunctionName") // This class's functions are intended to be used only in tests
abstract class ErrorsTest(
    final override val clientAdapter: () -> HttpClientPort,
    final override val serverAdapter: () -> HttpServerPort,
    final override val serverSettings: HttpServerSettings = HttpServerSettings(),
) : BaseTest() {

    // errors
    class CustomException : IllegalArgumentException()

    private val path: PathHandler = path {

        /*
         * Catching `Exception` handles any unhandled exception, has to be the last executed (first
         * declared)
         */
        exception<Exception> {
            internalServerError("Root handler")
        }

        exception<IllegalArgumentException> {
            val error = exception?.message ?: exception?.javaClass?.name ?: fail
            val newHeaders = response.headers + Header("runtime-error", error)
            send(598, "Runtime", headers = newHeaders)
        }

        exception<UnsupportedOperationException> {
            val error = exception?.message ?: exception?.javaClass?.name ?: fail
            val newHeaders = response.headers + Header("error", error)
            send(599, "Unsupported", headers = newHeaders)
        }

        get("/exception") { throw UnsupportedOperationException("error message") }
        get("/baseException") { throw CustomException() }
        get("/unhandledException") { error("error message") }
        get("/invalidBody") { ok(LocalDateTime.now()) }

        get("/halt") { internalServerError("halted") }
        get("/588") { send(588) }

        // It is possible to execute a handler upon a given status code before returning
        before(pattern = "*", status = 588) {
            send(578, "588 -> 578")
        }
    }
    // errors

    override val handler: HttpHandler = path

    @Test fun `Invalid body returns 500 status code`() {
        val server = server()
        val client = client(server)

        val response = client.get("/invalidBody")
        val message = "Unsupported body type: LocalDateTime"
        assertResponseContains(response, INTERNAL_SERVER_ERROR_500, message)

        client.stop()
        server.stop()
    }

    @Test fun `Halt stops request with 500 status code`() {
        val server = server()
        val client = client(server)

        val response = client.get("/halt")
        assertResponseEquals(response, INTERNAL_SERVER_ERROR_500, "halted")

        client.stop()
        server.stop()
    }

    @Test fun `Handling status code allows to change the returned code`() {
        val server = server()
        val client = client(server)

        val response = client.get("/588")
        assertResponseEquals(response, 578, "588 -> 578")

        client.stop()
        server.stop()
    }

    @Test fun `Handle exception allows to catch unhandled callback exceptions`() {
        val server = server()
        val client = client(server)

        val response = client.get("/exception")
        assertEquals("error message", response.headers["error"]?.value)
        assertResponseContains(response, 599, "Unsupported")

        client.stop()
        server.stop()
    }

    @Test fun `Base error handler catch all exceptions that subclass a given one`() {
        val server = server()
        val client = client(server)

        val response = client.get("/baseException")
        val runtimeError = response.headers["runtime-error"]?.value
        assertEquals(CustomException::class.java.name, runtimeError)
        assertResponseContains(response, 598, "Runtime")

        client.stop()
        server.stop()
    }

    @Test fun `A runtime exception returns a 500 code`() {
        val server = server()
        val client = client(server)

        val response = client.get("/unhandledException")
        assertResponseContains(response, INTERNAL_SERVER_ERROR_500, "Root handler")

        client.stop()
        server.stop()
    }
}
