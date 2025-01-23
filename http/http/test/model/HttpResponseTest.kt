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
            headers = Headers(Header("hr1", "hr1v1"), Header("hr1", "hr1v2")),
            contentType = contentType,
            cookies = listOf(Cookie("cn", "cv")),
            status = NOT_FOUND_404,
        )

    @Test fun `HTTP Response accepts mixed headers`() {
        HttpResponse(
            body = "response",
            headers = Headers(Header("hr1", "hr1v1"), ContentType(APPLICATION_JSON)),
            cookies = listOf(Cookie("cn", "cv")),
            status = NOT_FOUND_404,
        )
    }

    @Test fun `HTTP Response comparison works ok`() {
        val httpResponse = httpResponseData()

        assertEquals(httpResponse, httpResponse)
        assertEqualHttpResponses(httpResponseData(), httpResponseData())
        assertFalse(httpResponse.equals(""))

        val headers = Headers(Header("h1", "v1"))
        val cookies = listOf(Cookie("p", "v"))
        val contentType = ContentType(TEXT_RICHTEXT)

        assertNotEquals(httpResponse, httpResponse.with(body = "body"))
        assertNotEquals(httpResponse, httpResponse.with(headers = headers))
        assertNotEquals(httpResponse, httpResponse.with(contentType = contentType))
        assertNotEquals(httpResponse, httpResponse.with(cookies = cookies))
        assertNotEquals(httpResponse, httpResponse.with(status = OK_200))

        assertEqualHttpResponses(httpResponse, httpResponseData())
        assertEqualHttpResponses(
            httpResponse.with(contentType = null),
            httpResponseData(null)
        )
    }

    @Test fun `HTTP Response operators work ok`() {
        val httpResponse = httpResponseData()

        val header = Header("h", "v")
        assertEqualHttpResponses(
            httpResponse + header,
            httpResponse.with(headers = httpResponse.headers + header)
        )
        assertEqualHttpResponses(
            httpResponse + Headers(header),
            httpResponse.with(headers = httpResponse.headers + header)
        )

        val cookie = Cookie("n", "v")
        assertEqualHttpResponses(
            httpResponse + cookie,
            httpResponse.with(cookies = httpResponse.cookies + cookie)
        )
        assertEqualHttpResponses(
            httpResponse + listOf(cookie),
            httpResponse.with(cookies = httpResponse.cookies + cookie)
        )
    }

    @Test fun `HTTP Status ranges are correct`() {
        assert(EARLY_HINTS_103 in INFORMATION)
        assert(IM_USED_226 in SUCCESS)
        assert(PERMANENT_REDIRECT_308 in REDIRECTION)
        assert(UNAVAILABLE_FOR_LEGAL_REASONS_451 in CLIENT_ERROR)
        assert(LOOP_DETECTED_508 in SERVER_ERROR)
    }
}
