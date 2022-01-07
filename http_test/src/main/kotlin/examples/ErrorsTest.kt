package com.hexagonkt.http.test.examples

import com.hexagonkt.core.helpers.fail
import com.hexagonkt.http.client.HttpClientPort
import com.hexagonkt.http.model.ClientErrorStatus.NOT_FOUND
import com.hexagonkt.http.model.HttpStatus
import com.hexagonkt.http.model.ServerErrorStatus.INTERNAL_SERVER_ERROR
import com.hexagonkt.http.server.HttpServerPort
import com.hexagonkt.http.server.handlers.PathHandler
import com.hexagonkt.http.server.handlers.ServerHandler
import com.hexagonkt.http.server.handlers.path
import com.hexagonkt.http.test.BaseTest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@Suppress("FunctionName") // This class's functions are intended to be used only in tests
abstract class ErrorsTest(
    override val clientAdapter: () -> HttpClientPort,
    override val serverAdapter: () -> HttpServerPort,
) : BaseTest() {

    // errors
    class CustomException : IllegalArgumentException()

    private val path: PathHandler = path {

        // Catching `Exception` handles any unhandled exception before (it has to be the last)
        after(exception = Exception::class, status = NOT_FOUND) {
            internalServerError("Root handler")
        }

        get("/exception") { throw UnsupportedOperationException("error message") }
        get("/baseException") { throw CustomException() }
        get("/unhandledException") { error("error message") }

        get("/halt") { internalServerError("halted") }
        get("/588") { send(HttpStatus(588)) }

        on(exception = UnsupportedOperationException::class) {
            val error = context.exception?.message ?: context.exception?.javaClass?.name ?: fail
            val newHeaders = response.headers + ("error" to error)
            send(HttpStatus(599), "Unsupported", headers = newHeaders)
        }

        on(exception = IllegalArgumentException::class) {
            val error = context.exception?.message ?: context.exception?.javaClass?.name ?: fail
            val newHeaders = response.headers + ("runtime-error" to error)
            send(HttpStatus(598), "Runtime", headers = newHeaders)
        }

        // It is possible to execute a handler upon a given status code before returning
        on(status = HttpStatus(588)) {
            send(HttpStatus(578), "588 -> 578")
        }
    }
    // errors

    override val handlers: List<ServerHandler> = listOf(path)

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
