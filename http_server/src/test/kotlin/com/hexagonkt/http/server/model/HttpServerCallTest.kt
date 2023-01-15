package com.hexagonkt.http.server.model

import com.hexagonkt.core.media.TextMedia.HTML
import com.hexagonkt.core.media.TextMedia.PLAIN
import com.hexagonkt.http.model.*
import com.hexagonkt.http.model.ClientErrorStatus.NOT_FOUND
import com.hexagonkt.http.model.HttpMethod.*
import com.hexagonkt.http.model.HttpProtocol.HTTPS
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

internal class HttpServerCallTest {

    private val fullServerCall: HttpServerCall = httpServerCallData()

    private fun httpServerCallData(): HttpServerCall =
        HttpServerCall(
            httpServerRequestData(),
            httpServerResponseData()
        )

    private fun httpServerResponseData(): HttpServerResponse =
        HttpServerResponse(
            body = "response",
            headers = Headers(Header("hr1", "hr1v1", "hr1v2")),
            contentType = ContentType(HTML),
            cookies = listOf(Cookie("cn", "cv")),
            status = NOT_FOUND,
        )

    private fun httpServerRequestData(): HttpServerRequest =
        HttpServerRequest(
            method = POST,
            protocol = HTTPS,
            host = "127.0.0.1",
            port = 9999,
            path = "/path",
            headers = Headers(Header("h1", "h1v1", "h1v2")),
            body = "request",
            parts = listOf(HttpPart("n", "b")),
            formParameters = FormParameters(FormParameter("fp1", "fp1v1", "fp1v2")),
            cookies = listOf(Cookie("cn", "cv")),
            contentType = ContentType(PLAIN),
            certificateChain = emptyList(),
            accept = listOf(ContentType(HTML)),
        )

    @Test fun `HTTP Server Call comparison works ok`() {
        assertEquals(fullServerCall, fullServerCall)
        assertEquals(httpServerCallData(), httpServerCallData())
        assertFalse(fullServerCall.equals(""))
        assertEquals(fullServerCall.hashCode(), httpServerCallData().hashCode())
    }
}
