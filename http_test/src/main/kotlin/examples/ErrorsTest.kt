package com.hexagonkt.http.test.examples

import com.hexagonkt.core.fail
import com.hexagonkt.http.client.HttpClientPort
import com.hexagonkt.http.model.ClientErrorStatus.NOT_FOUND
import com.hexagonkt.http.model.HttpStatus
import com.hexagonkt.http.model.ServerErrorStatus.INTERNAL_SERVER_ERROR
import com.hexagonkt.http.server.HttpServerPort
import com.hexagonkt.http.server.HttpServerSettings
import com.hexagonkt.http.server.handlers.PathHandler
import com.hexagonkt.http.server.handlers.ServerHandler
import com.hexagonkt.http.server.handlers.path
import com.hexagonkt.http.test.BaseTest
import kotlinx.coroutines.runBlocking
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
        exception<Exception>(NOT_FOUND) {
            internalServerError("Root handler")
        }

        exception<IllegalArgumentException> {
            val error = exception?.message ?: exception?.javaClass?.name ?: fail
            val newHeaders = response.headers + ("runtime-error" to error)
            send(HttpStatus(598), "Runtime", headers = newHeaders)
        }

        exception<UnsupportedOperationException> {
            val error = exception?.message ?: exception?.javaClass?.name ?: fail
            val newHeaders = response.headers + ("error" to error)
            send(HttpStatus(599), "Unsupported", headers = newHeaders)
        }

        get("/exception") { throw UnsupportedOperationException("error message") }
        get("/baseException") { throw CustomException() }
        get("/unhandledException") { error("error message") }
        get("/invalidBody") { ok(LocalDateTime.now()) }

        get("/halt") { internalServerError("halted") }
        get("/588") { send(HttpStatus(588)) }

        // It is possible to execute a handler upon a given status code before returning
        on(pattern = "*", status = HttpStatus(588)) {
            send(HttpStatus(578), "588 -> 578")
        }
    }
    // errors

    override val handler: ServerHandler = path

    @Test fun `Invalid body returns 500 status code`() = runBlocking {
        val response = client.get("/invalidBody")
        val message = "Unsupported body type: LocalDateTime"
        assertResponseContains(response, INTERNAL_SERVER_ERROR, message)
    }

    @Test fun `Halt stops request with 500 status code`() = runBlocking {
        val response = client.get("/halt")
        assertResponseEquals(response, INTERNAL_SERVER_ERROR, "halted")
    }

    @Test fun `Handling status code allows to change the returned code`() = runBlocking {
        val response = client.get("/588")
        assertResponseEquals(response, HttpStatus(578), "588 -> 578")
    }

    @Test fun `Handle exception allows to catch unhandled callback exceptions`() = runBlocking {
        val response = client.get("/exception")
        assertEquals("error message", response.headers["error"])
        assertResponseContains(response, HttpStatus(599), "Unsupported")
    }

    @Test fun `Base error handler catch all exceptions that subclass a given one`() = runBlocking {
        val response = client.get("/baseException")
        val runtimeError = response.headers["runtime-error"]
        assertEquals(CustomException::class.java.name, runtimeError)
        assertResponseContains(response, HttpStatus(598), "Runtime")
    }

    @Test fun `A runtime exception returns a 500 code`() = runBlocking {
        val response = client.get("/unhandledException")
        assertResponseContains(response, INTERNAL_SERVER_ERROR, "Root handler")
    }
}
