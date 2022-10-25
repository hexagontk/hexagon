package com.hexagonkt.http.client.model

import com.hexagonkt.core.media.TextMedia.HTML
import com.hexagonkt.core.media.TextMedia.RICHTEXT
import com.hexagonkt.http.model.ContentType
import com.hexagonkt.http.model.HttpCookie
import com.hexagonkt.http.model.ClientErrorStatus.NOT_FOUND
import com.hexagonkt.http.model.Header
import com.hexagonkt.http.model.Headers
import com.hexagonkt.http.model.SuccessStatus.OK
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

internal class HttpClientResponseTest {

    private fun httpClientResponseData(
        contentType: ContentType? = ContentType(HTML)
    ): HttpClientResponse =
            HttpClientResponse(
                body = "response",
                headers = Headers(Header("hr1", "hr1v1", "hr1v2")),
                contentType = contentType,
                cookies = listOf(HttpCookie("cn", "cv")),
                status = NOT_FOUND,
            )

    @Test fun `HTTP Client Response comparison works ok`() {
        val httpClientRequest = httpClientResponseData()

        assertEquals(httpClientRequest, httpClientRequest)
        assertEquals(httpClientResponseData(), httpClientResponseData())
        assertFalse(httpClientRequest.equals(""))

        val headers = Headers(Header("h1", "v1"))
        val cookies = listOf(HttpCookie("p", "v"))
        val contentType = ContentType(RICHTEXT)

        assertNotEquals(httpClientRequest, httpClientRequest.copy(body = "body"))
        assertNotEquals(httpClientRequest, httpClientRequest.copy(headers = headers))
        assertNotEquals(httpClientRequest, httpClientRequest.copy(contentType = contentType))
        assertNotEquals(httpClientRequest, httpClientRequest.copy(cookies = cookies))
        assertNotEquals(httpClientRequest, httpClientRequest.copy(status = OK))

        assertEquals(httpClientRequest.hashCode(), httpClientResponseData().hashCode())
        assertEquals(
            httpClientRequest.copy(contentType = null).hashCode(),
            httpClientResponseData(null).hashCode()
        )
    }
}
