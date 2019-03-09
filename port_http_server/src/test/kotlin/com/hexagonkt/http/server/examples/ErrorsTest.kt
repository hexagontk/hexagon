package com.hexagonkt.http.server.examples

import com.hexagonkt.http.client.Client
import com.hexagonkt.http.server.Server
import com.hexagonkt.http.server.ServerPort
import org.asynchttpclient.Response
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.util.Locale.getDefault as defaultLocale

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

            error(588) { send(578, "588 -> 578") }

            get("/exception") { throw UnsupportedOperationException("error message") }
            get("/baseException") { throw CustomException() }
            get("/unhandledException") { error("error message") }

            get("/halt") { halt("halted") }
            get("/588") { halt(588) }
        }
    }
    // errors

    private val client: Client by lazy { Client("http://localhost:${server.runtimePort}") }

    @BeforeClass fun initialize() {
        server.start()
    }

    @AfterClass fun shutdown() {
        server.stop()
    }

    @Test fun halt() {
        val response = client.get ("/halt")
        assertResponseEquals(response, "halted", 500)
    }

    @Test fun codeHandling() {
        val response = client.get ("/588")
        assertResponseEquals(response, "588 -> 578", 578)
    }

    @Test fun handleException() {
        val response = client.get ("/exception")
        assert("error message" == response.headers["error"]?.toString())
        assertResponseContains(response, 599, "Unsupported")
    }

    @Test fun base_error_handler() {
        val response = client.get ("/baseException")
        assert(response.headers["runtimeError"]?.toString() == CustomException::class.java.name)
        assertResponseContains(response, 598, "Runtime")
    }

    @Test fun not_registered_error_handler() {
        val response = client.get ("/unhandledException")
        assertResponseContains(response, 500)
    }

    private fun assertResponseEquals(response: Response?, content: String, status: Int = 200) {
        assert (response?.statusCode == status)
        assert (response?.responseBody == content)
    }

    private fun assertResponseContains(response: Response?, status: Int, vararg content: String) {
        assert (response?.statusCode == status)
        content.forEach {
            assert (response?.responseBody?.contains (it) ?: false)
        }
    }
}

