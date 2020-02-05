package com.hexagonkt.http.server.examples

import com.hexagonkt.http.client.Client
import com.hexagonkt.http.client.Response
import com.hexagonkt.http.client.ahc.AhcAdapter
import com.hexagonkt.http.server.Server
import com.hexagonkt.http.server.ServerPort
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

@Test abstract class ErrorsTest(adapter: ServerPort) {

    // errors
    class CustomException : IllegalArgumentException()

    val server: Server by lazy {
        Server(adapter) {
            error(UnsupportedOperationException::class) {
                response.setHeader("error", it.message ?: it.javaClass.name)
                send(599, "Unsupported")
            }

            error(IllegalArgumentException::class) {
                response.setHeader("runtimeError", it.message ?: it.javaClass.name)
                send(598, "Runtime")
            }

            // Catching `Exception` handles any unhandled exception before (it has to be the last)
            error(Exception::class) { send(500, "Root handler") }

            // It is possible to execute a handler upon a given status code before returning
            error(588) { send(578, "588 -> 578") }

            get("/exception") { throw UnsupportedOperationException("error message") }
            get("/baseException") { throw CustomException() }
            get("/unhandledException") { error("error message") }

            get("/halt") { halt("halted") }
            get("/588") { halt(588) }
        }
    }
    // errors

    private val client: Client by lazy {
        Client(AhcAdapter(), "http://localhost:${server.runtimePort}")
    }

    @BeforeClass fun initialize() {
        server.start()
    }

    @AfterClass fun shutdown() {
        server.stop()
    }

    @Test fun `Halt stops request with 500 status code`() {
        val response = client.get ("/halt")
        assertResponseEquals(response, "halted", 500)
    }

    @Test fun `Handling status code allows to change the returned code`() {
        val response = client.get ("/588")
        assertResponseEquals(response, "588 -> 578", 578)
    }

    @Test fun `Handle exception allows to catch unhandled callback exceptions`() {
        val response = client.get ("/exception")
        assert("error message" == response.headers["error"]?.first().toString())
        assertResponseContains(response, 599, "Unsupported")
    }

    @Test fun `Base error handler catch all exceptions that subclass a given one`() {
        val response = client.get ("/baseException")
        val runtimeError = response.headers["runtimeError"]?.first()
        assert(runtimeError.toString() == CustomException::class.java.name)
        assertResponseContains(response, 598, "Runtime")
    }

    @Test fun `A runtime exception returns a 500 code`() {
        val response = client.get ("/unhandledException")
        assertResponseContains(response, 500, "Root handler")
    }

    private fun assertResponseEquals(response: Response?, content: String, status: Int = 200) {
        assert (response?.status == status)
        assert (response?.body == content)
    }

    private fun assertResponseContains(response: Response?, status: Int, vararg content: String) {
        assert (response?.status == status)
        content.forEach {
            assert (response?.body?.contains (it) ?: false)
        }
    }
}

