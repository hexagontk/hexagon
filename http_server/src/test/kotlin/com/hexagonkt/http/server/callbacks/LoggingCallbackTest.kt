package com.hexagonkt.http.server.callbacks

import com.hexagonkt.core.media.ApplicationMedia.JSON
import com.hexagonkt.core.media.TextMedia.HTML
import com.hexagonkt.core.media.TextMedia.PLAIN
import com.hexagonkt.http.model.ContentType
import com.hexagonkt.http.model.Header
import com.hexagonkt.http.model.Headers
import com.hexagonkt.http.server.handlers.HttpServerContext
import com.hexagonkt.http.server.model.HttpServerRequest
import com.hexagonkt.http.server.model.HttpServerResponse
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class LoggingCallbackTest {

    @Test fun `Logging callback include the details`() {
        LoggingCallback()(HttpServerContext())
    }

    @Test fun `Details are configurable for requests`() {
        val prefix = "Request:\nGET"

        assertEquals("$prefix \n\nh: 42\n\nb", requestDetails(headers = true, body = true))
        assertEquals("$prefix \n\nh: 42", requestDetails(headers = true, body = false))
        assertEquals("$prefix \n\nb", requestDetails(headers = false, body = true))
        assertEquals(prefix, requestDetails(headers = false, body = false))
    }

    @Test fun `Request headers are displayed properly`() {
        val prefix = "Request:\nGET"

        assertEquals(
            "$prefix \n\naccept: text/plain, text/html",
            requestDetails(
                request = HttpServerRequest(accept = listOf(ContentType(PLAIN), ContentType(HTML)))
            )
        )
        assertEquals(
            "$prefix \n\ncontent-type: application/json",
            requestDetails(request = HttpServerRequest(contentType = ContentType(JSON)))
        )
    }

    @Test fun `Details are configurable for responses`() {
        val prefix = "Response (partial headers):\nGET  -> NOT_FOUND(404) (1.0 ms)"

        assertEquals("$prefix\n\nh: 42\n\nb", responseDetails(headers = true, body = true))
        assertEquals("$prefix\n\nh: 42", responseDetails(headers = true, body = false))
        assertEquals("$prefix\n\nb", responseDetails(headers = false, body = true))
        assertEquals(prefix, responseDetails(headers = false, body = false))
    }

    @Test fun `Response headers are displayed properly`() {
        val prefix = "Response (partial headers):\nGET  -> NOT_FOUND(404) (1.0 ms)"

        assertEquals(
            "$prefix\n\ncontent-type: application/json",
            responseDetails(
                response = HttpServerResponse(contentType = ContentType(JSON))
            )
        )
    }

    private fun requestDetails(
        headers: Boolean = true,
        body: Boolean = true,
        request: HttpServerRequest =
            HttpServerRequest(
                headers = Headers(Header("h", 42)),
                body = "b"
            )
    ): String =
        LoggingCallback(includeHeaders = headers, includeBody = body).details(request)

    private fun responseDetails(
        headers: Boolean = true,
        body: Boolean = true,
        request: HttpServerRequest =
            HttpServerRequest(
                headers = Headers(Header("h", 42)),
                body = "b"
            ),
        response: HttpServerResponse =
            HttpServerResponse(
                headers = Headers(Header("h", 42)),
                body = "b",
            ),
        t: Long = 1_000_000,
    ): String =
        LoggingCallback(includeHeaders = headers, includeBody = body).details(request, response, t)
}
