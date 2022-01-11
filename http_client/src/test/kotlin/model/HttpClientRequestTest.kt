package com.hexagonkt.http.client.model

import com.hexagonkt.core.media.TextMedia.CSS
import com.hexagonkt.core.media.TextMedia.HTML
import com.hexagonkt.core.media.TextMedia.PLAIN
import com.hexagonkt.core.media.TextMedia.RICHTEXT
import com.hexagonkt.core.multiMapOf
import com.hexagonkt.core.multiMapOfLists
import com.hexagonkt.http.model.ContentType
import com.hexagonkt.http.model.HttpCookie
import com.hexagonkt.http.model.HttpMethod.*
import com.hexagonkt.http.model.HttpPart
import com.hexagonkt.http.model.HttpProtocol.HTTP2
import com.hexagonkt.http.model.HttpProtocol.HTTPS
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

internal class HttpClientRequestTest {

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

    @Test fun `HTTP Client Request comparison works ok`() {
        val httpClientRequest = httpClientRequestData()

        assertEquals(httpClientRequest, httpClientRequest)
        assertEquals(httpClientRequestData(), httpClientRequestData())
        assertFalse(httpClientRequest.equals(""))

        val headers = multiMapOf("h1" to "v1")
        val parts = listOf(HttpPart("p", "v"))
        val formParameters = multiMapOf("h1" to "v1")
        val cookies = listOf(HttpCookie("p", "v"))
        val contentType = ContentType(RICHTEXT)
        val accept = listOf(ContentType(CSS))

        assertNotEquals(httpClientRequest, httpClientRequest.copy(method = PUT))
        assertNotEquals(httpClientRequest, httpClientRequest.copy(protocol = HTTP2))
        assertNotEquals(httpClientRequest, httpClientRequest.copy(host = "host"))
        assertNotEquals(httpClientRequest, httpClientRequest.copy(port = 1234))
        assertNotEquals(httpClientRequest, httpClientRequest.copy(path = "/aPath"))
        assertNotEquals(httpClientRequest, httpClientRequest.copy(queryString = "k=v&k2=v2"))
        assertNotEquals(httpClientRequest, httpClientRequest.copy(headers = headers))
        assertNotEquals(httpClientRequest, httpClientRequest.copy(body = "body"))
        assertNotEquals(httpClientRequest, httpClientRequest.copy(parts = parts))
        assertNotEquals(httpClientRequest, httpClientRequest.copy(formParameters = formParameters))
        assertNotEquals(httpClientRequest, httpClientRequest.copy(cookies = cookies))
        assertNotEquals(httpClientRequest, httpClientRequest.copy(contentType = contentType))
        assertNotEquals(httpClientRequest, httpClientRequest.copy(accept = accept))

        assertEquals(httpClientRequest.hashCode(), httpClientRequestData().hashCode())
        assertEquals(
            httpClientRequest.copy(contentType = null).hashCode(),
            httpClientRequestData().copy(contentType = null).hashCode()
        )
    }
}
