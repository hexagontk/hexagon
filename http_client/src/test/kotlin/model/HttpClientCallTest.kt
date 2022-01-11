package com.hexagonkt.http.client.model

import com.hexagonkt.core.media.TextMedia.HTML
import com.hexagonkt.core.media.TextMedia.PLAIN
import com.hexagonkt.core.multiMapOfLists
import com.hexagonkt.http.model.ContentType
import com.hexagonkt.http.model.HttpCookie
import com.hexagonkt.http.model.HttpMethod.*
import com.hexagonkt.http.model.HttpPart
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
            headers = multiMapOfLists("hr1" to listOf("hr1v1", "hr1v2")),
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
            queryString = "k=v",
            headers = multiMapOfLists("h1" to listOf("h1v1", "h1v2")),
            body = "request",
            parts = listOf(HttpPart("n", "b")),
            formParameters = multiMapOfLists("fp1" to listOf("fp1v1", "fp1v2")),
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
