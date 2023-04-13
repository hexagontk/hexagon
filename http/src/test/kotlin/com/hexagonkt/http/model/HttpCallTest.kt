package com.hexagonkt.http.model

import com.hexagonkt.core.media.TEXT_HTML
import com.hexagonkt.core.media.TEXT_PLAIN
import com.hexagonkt.http.model.HttpMethod.*
import com.hexagonkt.http.model.HttpProtocol.HTTPS
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

internal class HttpCallTest {

    private val fullCall: HttpCall = httpCallData()

    private fun httpCallData(): HttpCall =
        HttpCall(
            httpRequestData(),
            httpResponseData()
        )

    private fun httpResponseData(): HttpResponse =
        HttpResponse(
            body = "response",
            headers = Headers(Header("hr1", "hr1v1", "hr1v2")),
            contentType = ContentType(TEXT_HTML),
            cookies = listOf(Cookie("cn", "cv")),
            status = NOT_FOUND_404,
        )

    private fun httpRequestData(): HttpRequest =
        HttpRequest(
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
            contentType = ContentType(TEXT_PLAIN),
            certificateChain = emptyList(),
            accept = listOf(ContentType(TEXT_HTML)),
        )

    @Test fun `HTTP Server Call comparison works ok`() {
        assertEquals(fullCall, fullCall)
        assertEquals(httpCallData(), httpCallData())
        assertFalse(fullCall.equals(""))
        assertEquals(fullCall.hashCode(), httpCallData().hashCode())
    }
}
