package com.hexagontk.http.model

import com.hexagontk.core.media.APPLICATION_JSON
import com.hexagontk.core.media.TEXT_HTML
import com.hexagontk.core.media.TEXT_RICHTEXT
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

internal class HttpResponseTest {

    private fun httpResponseData(contentType: ContentType? = ContentType(TEXT_HTML)): HttpResponse =
        HttpResponse(
            body = "response",
            headers = Headers(Field("hr1", "hr1v1"), Field("hr1", "hr1v2")),
            contentType = contentType,
            cookies = listOf(Cookie("cn", "cv")),
            status = NOT_FOUND_404,
        )

    @Test fun `HTTP Response accepts mixed headers`() {
        HttpResponse(
            body = "response",
            headers = Headers(Field("hr1", "hr1v1"), ContentType(APPLICATION_JSON)),
            cookies = listOf(Cookie("cn", "cv")),
            status = NOT_FOUND_404,
        )
    }

    @Test fun `HTTP Response comparison works ok`() {
        val httpResponse = httpResponseData()

        assertEquals(httpResponse, httpResponse)
        assertEquals(httpResponseData(), httpResponseData())
        assertFalse(httpResponse.equals(""))

        val headers = Headers(Field("h1", "v1"))
        val cookies = listOf(Cookie("p", "v"))
        val contentType = ContentType(TEXT_RICHTEXT)

        assertNotEquals(httpResponse, httpResponse.with(body = "body"))
        assertNotEquals(httpResponse, httpResponse.with(headers = headers))
        assertNotEquals(httpResponse, httpResponse.with(contentType = contentType))
        assertNotEquals(httpResponse, httpResponse.with(cookies = cookies))
        assertNotEquals(httpResponse, httpResponse.with(status = OK_200))

        assertEquals(httpResponse.hashCode(), httpResponseData().hashCode())
        assertEquals(
            httpResponse.copy(contentType = null).hashCode(),
            httpResponseData(null).hashCode()
        )
    }

    @Test fun `HTTP Response operators work ok`() {
        val httpResponse = httpResponseData()

        val header = Field("h", "v")
        assertEquals(
            httpResponse + header,
            httpResponse.copy(headers = httpResponse.headers + header)
        )
        assertEquals(
            httpResponse + Headers(header),
            httpResponse.copy(headers = httpResponse.headers + header)
        )

        val cookie = Cookie("n", "v")
        assertEquals(
            httpResponse + cookie,
            httpResponse.copy(cookies = httpResponse.cookies + cookie)
        )
        assertEquals(
            httpResponse + listOf(cookie),
            httpResponse.copy(cookies = httpResponse.cookies + cookie)
        )
    }
}
