package com.hexagonkt.http.server.model

import com.hexagonkt.core.media.TEXT_HTML
import com.hexagonkt.core.media.TEXT_RICHTEXT
import com.hexagonkt.http.model.*
import com.hexagonkt.http.model.NOT_FOUND_404
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

internal class HttpServerResponseTest {

    private fun httpServerResponseData(
        contentType: ContentType? = ContentType(TEXT_HTML)
    ): HttpServerResponse =
            HttpServerResponse(
                body = "response",
                headers = Headers(Header("hr1", "hr1v1", "hr1v2")),
                contentType = contentType,
                cookies = listOf(Cookie("cn", "cv")),
                status = NOT_FOUND_404,
            )

    @Test fun `HTTP Server Response comparison works ok`() {
        val httpServerRequest = httpServerResponseData()

        assertEquals(httpServerRequest, httpServerRequest)
        assertEquals(httpServerResponseData(), httpServerResponseData())
        assertFalse(httpServerRequest.equals(""))

        val headers = Headers(Header("h1", "v1"))
        val cookies = listOf(Cookie("p", "v"))
        val contentType = ContentType(TEXT_RICHTEXT)

        assertNotEquals(httpServerRequest, httpServerRequest.copy(body = "body"))
        assertNotEquals(httpServerRequest, httpServerRequest.copy(headers = headers))
        assertNotEquals(httpServerRequest, httpServerRequest.copy(contentType = contentType))
        assertNotEquals(httpServerRequest, httpServerRequest.copy(cookies = cookies))
        assertNotEquals(httpServerRequest, httpServerRequest.copy(status = OK_200))

        assertEquals(httpServerRequest.hashCode(), httpServerResponseData().hashCode())
        assertEquals(
            httpServerRequest.copy(contentType = null).hashCode(),
            httpServerResponseData(null).hashCode()
        )
    }
}
