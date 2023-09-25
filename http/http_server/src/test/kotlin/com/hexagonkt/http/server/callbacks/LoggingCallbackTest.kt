package com.hexagonkt.http.server.callbacks

import com.hexagonkt.core.media.APPLICATION_JSON
import com.hexagonkt.core.media.TEXT_HTML
import com.hexagonkt.core.media.TEXT_PLAIN
import com.hexagonkt.http.model.ContentType
import com.hexagonkt.http.model.Header
import com.hexagonkt.http.model.Headers
import com.hexagonkt.http.handlers.HttpContext
import com.hexagonkt.http.model.HttpRequest
import com.hexagonkt.http.model.HttpResponse
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class LoggingCallbackTest {

    @Test fun `Logging callback include the details`() {
        LoggingCallback()(HttpContext())
    }

    @Test fun `Details are configurable for requests`() {
        val prefix = "GET"

        assertEquals("$prefix \n\nh: 42\n\nb", requestDetails(headers = true, body = true))
        assertEquals("$prefix \n\nh: 42", requestDetails(headers = true, body = false))
        assertEquals("$prefix \n\nb", requestDetails(headers = false, body = true))
        assertEquals(prefix, requestDetails(headers = false, body = false))
    }

    @Test fun `Request headers are displayed properly`() {
        val prefix = "GET"

        assertEquals(
            "$prefix \n\naccept: text/plain, text/html",
            requestDetails(
                request = HttpRequest(
                    accept = listOf(ContentType(TEXT_PLAIN), ContentType(TEXT_HTML))
                )
            )
        )
        assertEquals(
            "$prefix \n\ncontent-type: application/json",
            requestDetails(request = HttpRequest(contentType = ContentType(APPLICATION_JSON)))
        )
    }

    @Test fun `Details are configurable for responses`() {
        val prefix = "GET  -> CLIENT_ERROR(404) (1.0 ms)"

        assertEquals("$prefix\n\nh: 42\n\nb", responseDetails(headers = true, body = true))
        assertEquals("$prefix\n\nh: 42", responseDetails(headers = true, body = false))
        assertEquals("$prefix\n\nb", responseDetails(headers = false, body = true))
        assertEquals(prefix, responseDetails(headers = false, body = false))
    }

    @Test fun `Response headers are displayed properly`() {
        val prefix = "GET  -> CLIENT_ERROR(404) (1.0 ms)"

        assertEquals(
            "$prefix\n\ncontent-type: application/json",
            responseDetails(
                response = HttpResponse(contentType = ContentType(APPLICATION_JSON))
            )
        )
    }

    private fun requestDetails(
        headers: Boolean = true,
        body: Boolean = true,
        request: HttpRequest =
            HttpRequest(
                headers = Headers(Header("h", 42)),
                body = "b"
            )
    ): String =
        LoggingCallback(includeHeaders = headers, includeBody = body).details(request)

    private fun responseDetails(
        headers: Boolean = true,
        body: Boolean = true,
        request: HttpRequest =
            HttpRequest(
                headers = Headers(Header("h", 42)),
                body = "b"
            ),
        response: HttpResponse =
            HttpResponse(
                headers = Headers(Header("h", 42)),
                body = "b",
            ),
        t: Long = 1_000_000,
    ): String =
        LoggingCallback(includeHeaders = headers, includeBody = body).details(request, response, t)
}
