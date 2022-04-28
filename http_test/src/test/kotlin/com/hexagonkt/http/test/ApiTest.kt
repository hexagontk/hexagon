package com.hexagonkt.http.test

import com.hexagonkt.core.media.TextMedia.PLAIN
import com.hexagonkt.http.client.jetty.JettyClientAdapter
import com.hexagonkt.http.model.ContentType
import com.hexagonkt.http.model.SuccessStatus.OK
import com.hexagonkt.http.server.handlers.path
import com.hexagonkt.http.server.jetty.JettyServletAdapter
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import kotlin.test.assertEquals

@TestInstance(PER_CLASS)
class ApiTest {
    private val mockServer: MockServer = MockServer(JettyServletAdapter())

    @BeforeAll fun `Set up mock services`() {
        mockServer.server.start()
    }

    @AfterAll fun `Shut down mock services`() {
        mockServer.server.stop()
    }

    @Test fun `Do HTTP requests`() {
        mockServer.path = path {
            get("/hello/{name}") {
                val name = pathParameters["name"]

                ok("Hello, $name!", contentType = ContentType(PLAIN))
            }
        }
        val http = Http("http://localhost:${mockServer.server.runtimePort}", JettyClientAdapter())
        http.get("/hello/mike")
        assertEquals(OK, http.responseOrNull?.status)
    }

    @Test fun `Mock HTTP response`() {
        mockServer.path = path {
            get("/foo") {
                ok("dynamic")
            }
        }

        val http = Http("http://localhost:${mockServer.server.runtimePort}", JettyClientAdapter())
        http.get("/foo")
        assertEquals(OK, http.responseOrNull?.status)
        assertEquals("dynamic", http.responseOrNull?.body)
        mockServer.path = path {
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
