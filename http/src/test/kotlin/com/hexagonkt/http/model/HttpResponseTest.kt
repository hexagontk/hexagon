package com.hexagonkt.http.model

import com.hexagonkt.core.media.TEXT_HTML
import com.hexagonkt.core.media.TEXT_RICHTEXT
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

internal class HttpResponseTest {

    private fun httpResponseData(contentType: ContentType? = ContentType(TEXT_HTML)): HttpResponse =
        HttpResponse(
            body = "response",
            headers = Headers(Header("hr1", "hr1v1", "hr1v2")),
            contentType = contentType,
            cookies = listOf(Cookie("cn", "cv")),
            status = NOT_FOUND_404,
        )

    @Test fun `HTTP Response comparison works ok`() {
        val httpRequest = httpResponseData()

        assertEquals(httpRequest, httpRequest)
        assertEquals(httpResponseData(), httpResponseData())
        assertFalse(httpRequest.equals(""))

        val headers = Headers(Header("h1", "v1"))
        val cookies = listOf(Cookie("p", "v"))
        val contentType = ContentType(TEXT_RICHTEXT)

        assertNotEquals(httpRequest, httpRequest.with(body = "body"))
        assertNotEquals(httpRequest, httpRequest.with(headers = headers))
        assertNotEquals(httpRequest, httpRequest.with(contentType = contentType))
        assertNotEquals(httpRequest, httpRequest.with(cookies = cookies))
        assertNotEquals(httpRequest, httpRequest.with(status = OK_200))

        assertEquals(httpRequest.hashCode(), httpResponseData().hashCode())
        assertEquals(
            httpRequest.copy(contentType = null).hashCode(),
            httpResponseData(null).hashCode()
        )
    }
}
