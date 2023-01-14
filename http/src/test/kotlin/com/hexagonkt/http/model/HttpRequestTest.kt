package com.hexagonkt.http.model

import com.hexagonkt.core.fail
import com.hexagonkt.http.model.HttpProtocol.HTTP
import kotlin.test.Test
import java.net.URL
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class HttpRequestTest {

    private companion object {
        var testProtocol: HttpProtocol = HTTP
        var testHost: String = "localhost"
        var testPort: Int = 80
        var testPath: String = "path"
        var testHeaders: Headers = Headers(
            Header("user-agent", "User Agent"),
            Header("referer", "Referer"),
            Header("origin", "Origin"),
            Header("authorization", "Basic value"),
        )
        var testQueryParameters: QueryParameters = QueryParameters(
            QueryParameter("qp1", "value1", "value2")
        )
    }

    private object TestEmptyRequest: HttpRequest {
        override val method: HttpMethod get() = fail
        override val protocol: HttpProtocol = testProtocol
        override val host: String = testHost
        override val port: Int = testPort
        override val path: String = testPath
        override val queryParameters: QueryParameters = testQueryParameters
        override val formParameters: FormParameters get() = fail
        override val body: Any get() = fail
        override val headers: Headers = Headers()
        override val contentType: ContentType get() = fail
        override val accept: List<ContentType> get() = fail
        override val authorization: Authorization? = authorization()

        override val cookies: List<Cookie> =
            listOf(Cookie("name1", "value1"), Cookie("name2", "value2"))

        override val parts: List<HttpPart> =
            listOf(HttpPart("name1", "value1"), HttpPart("name2", "value2"))
    }

    private object TestRequest : HttpRequest {
        override val method: HttpMethod get() = fail
        override val protocol: HttpProtocol = testProtocol
        override val host: String = testHost
        override val port: Int get() = testPort
        override val path: String = testPath
        override val queryParameters: QueryParameters get() = testQueryParameters
        override val formParameters: FormParameters get() = fail
        override val body: Any get() = fail
        override val headers: Headers get() = testHeaders
        override val contentType: ContentType get() = fail
        override val accept: List<ContentType> get() = fail
        override val authorization: Authorization? = authorization()

        override val cookies: List<Cookie> =
            listOf(Cookie("name1", "value1"), Cookie("name2", "value2"))

        override val parts: List<HttpPart> =
            listOf(HttpPart("name1", "value1"), HttpPart("name2", "value2"))
    }

    @Test fun `Header convenience methods works properly`() {
        assertEquals("User Agent", TestRequest.userAgent())
        assertEquals("Referer", TestRequest.referer())
        assertEquals("Origin", TestRequest.origin())

        assertNull(TestEmptyRequest.userAgent())
        assertNull(TestEmptyRequest.referer())
        assertNull(TestEmptyRequest.origin())
    }

    @Test fun `Request authorization header is parsed properly`() {
        assertEquals("Basic", TestRequest.authorization()?.type)
        assertEquals("value", TestRequest.authorization()?.value)

        val invalidAuthorization = "Basic words header"
        testHeaders += Header("authorization", invalidAuthorization)
        assertEquals("Basic", TestRequest.authorization()?.type)
        assertEquals("words header", TestRequest.authorization()?.value)

        assertNull(TestEmptyRequest.authorization)
    }

    @Test fun `Cookies map works properly`() {
        assertEquals(Cookie("name1", "value1"), TestRequest.cookiesMap()["name1"])
        assertEquals(Cookie("name2", "value2"), TestRequest.cookiesMap()["name2"])
        assertNull(TestRequest.cookiesMap()["name3"])
    }

    @Test fun `Parts map works properly`() {
        assertEquals(HttpPart("name1", "value1"), TestRequest.partsMap()["name1"])
        assertEquals(HttpPart("name2", "value2"), TestRequest.partsMap()["name2"])
        assertNull(TestRequest.partsMap()["name3"])
    }

    @Test fun `URL is generated correctly`() {
        assertEquals(URL("http://localhost:80/path?qp1=value1&qp1=value2"), TestRequest.url())
        testPort = 9999
        testQueryParameters = QueryParameters()
        assertEquals(URL("http://localhost:9999/path"), TestRequest.url())
    }
}
