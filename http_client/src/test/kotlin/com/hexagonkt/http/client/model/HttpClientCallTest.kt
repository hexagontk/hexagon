package com.hexagonkt.http.client.model

import com.hexagonkt.core.media.TextMedia.HTML
import com.hexagonkt.core.media.TextMedia.PLAIN
import com.hexagonkt.http.model.*
import com.hexagonkt.http.model.HttpMethod.*
import com.hexagonkt.http.model.HttpProtocol.HTTPS
import com.hexagonkt.http.model.ClientErrorStatus.NOT_FOUND
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

internal class HttpClientCallTest {

    private val fullClientCall: HttpClientCall = httpClientCallData()

    private fun httpClientCallData(): HttpClientCall =
        HttpClientCall(
            httpClientRequestData(),
            httpClientResponseData()
        )

    private fun httpClientResponseData(): HttpClientResponse =
        HttpClientResponse(
            body = "response",
            headers = Headers(Header("hr1", "hr1v1", "hr1v2")),
            contentType = ContentType(HTML),
            cookies = listOf(HttpCookie("cn", "cv")),
            status = NOT_FOUND,
        )

    private fun httpClientRequestData(): HttpClientRequest =
        HttpClientRequest(
            method = POST,
            protocol = HTTPS,
            host = "127.0.0.1",
            port = 9999,
            path = "/path",
            headers = Headers(Header("h1", "h1v1", "h1v2")),
            body = "request",
            parts = listOf(HttpPart("n", "b")),
            formParameters = FormParameters(FormParameter("fp1", "fp1v1", "fp1v2")),
            cookies = listOf(HttpCookie("cn", "cv")),
            contentType = ContentType(PLAIN),
            accept = listOf(ContentType(HTML)),
        )

    @Test fun `HTTP Client Call comparison works ok`() {
        assertEquals(fullClientCall, fullClientCall)
        assertEquals(httpClientCallData(), httpClientCallData())
        assertFalse(fullClientCall.equals(""))
        assertEquals(fullClientCall.hashCode(), httpClientCallData().hashCode())
    }
}
