package com.hexagonkt.http.handlers

import com.hexagonkt.http.model.HttpRequest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class HttpControllerTest {

    class TestController : HttpController {
        override val handler: HttpHandler = Get { ok("Good") }
    }

    @Test fun `A controller can be used as a handler`() {

        val path = path {
            use(TestController())
        }

        val response = path.process(HttpRequest())
        assertEquals("Good", response.response.bodyString())
    }
}
