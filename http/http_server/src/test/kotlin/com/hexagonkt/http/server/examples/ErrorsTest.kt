package com.hexagonkt.http.server.examples

import com.hexagonkt.core.fail
import com.hexagonkt.http.model.NOT_FOUND_404
import com.hexagonkt.http.model.HttpStatus
import com.hexagonkt.http.model.Header
import com.hexagonkt.http.model.HttpMethod.GET
import com.hexagonkt.http.model.INTERNAL_SERVER_ERROR_500
import com.hexagonkt.http.handlers.PathHandler
import com.hexagonkt.http.handlers.path
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ErrorsTest {

    class CustomException : IllegalArgumentException()

    private val path: PathHandler = path {

        get("/exception") { throw UnsupportedOperationException("error message") }
        get("/baseException") { throw CustomException() }
        get("/unhandledException") { error("error message") }

        get("/halt") { internalServerError("halted") }
        get("/588") { send(HttpStatus(588)) }

        before(pattern = "*", exception = UnsupportedOperationException::class) {
            val error = exception?.message ?: exception?.javaClass?.name ?: fail
            val newHeaders = response.headers + Header("error", error)
            send(HttpStatus(599), "Unsupported", headers = newHeaders)
        }

        before(pattern = "*", exception = IllegalArgumentException::class) {
            val error = exception?.message ?: exception?.javaClass?.name ?: fail
            val newHeaders = response.headers + Header("runtime-error", error)
            send(HttpStatus(598), "Runtime", headers = newHeaders)
        }

        // Catching `Exception` handles any unhandled exception before (it has to be the last)
        before(pattern = "*", exception = Exception::class, status = NOT_FOUND_404) {
            internalServerError("Root handler")
        }

        // It is possible to execute a handler upon a given status code before returning
        before(pattern = "*", status = HttpStatus(588)) {
            send(HttpStatus(578), "588 -> 578")
        }
    }

    @Test fun `Halt stops request with 500 status code`() {
        val response = path.send(GET, "/halt")
        assertResponseEquals(response, "halted", INTERNAL_SERVER_ERROR_500)
    }

    @Test fun `Handling status code allows to change the returned code`() {
        val response = path.send(GET, "/588")
        assertResponseEquals(response, "588 -> 578", HttpStatus(578))
    }

    @Test fun `Handle exception allows to catch unhandled callback exceptions`() {
        val response = path.send(GET, "/exception")
        assertEquals("error message", response.headers["error"]?.value)
        assertResponseContains(response, HttpStatus(599), "Unsupported")
    }

    @Test fun `Base error handler catch all exceptions that subclass a given one`() {
        val response = path.send(GET, "/baseException")
        val runtimeError = response.headers["runtime-error"]?.value
        assertEquals(CustomException::class.java.name, runtimeError)
        assertResponseContains(response, HttpStatus(598), "Runtime")
    }

    @Test fun `A runtime exception returns a 500 code`() {
        val response = path.send(GET, "/unhandledException")
        assertResponseContains(response, INTERNAL_SERVER_ERROR_500, "Root handler")
    }
}
