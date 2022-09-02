package com.hexagonkt.http.model

import com.hexagonkt.core.fail
import com.hexagonkt.http.model.HttpProtocol.HTTP
import org.junit.jupiter.api.Test
import java.net.URL
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class HttpRequestTest {

    private companion object {
        var testProtocol: HttpProtocol = HTTP
        var testHost: String = "localhost"
        var testPort: Int = 80
        var testPath: String = "path"
        var testHeaders: HttpFields<Header> = HttpFields(
            Header("user-agent", "User Agent"),
            Header("referer", "Referer"),
            Header("origin", "Origin"),
        )
        var testQueryParameters: HttpFields<QueryParameter> = HttpFields(
            QueryParameter("qp1", "value1", "value2")
        )
    }

    private object TestRequest : HttpRequest {
        override val method: HttpMethod get() = fail
        override val protocol: HttpProtocol get() = testProtocol
        override val host: String get() = testHost
        override val port: Int get() = testPort
        override val path: String get() = testPath
        override val queryParameters: HttpFields<QueryParameter> get() = testQueryParameters
        override val formParameters: HttpFields<FormParameter> get() = fail
        override val body: Any get() = fail
        override val headers: HttpFields<Header> get() = testHeaders
        override val contentType: ContentType get() = fail
        override val accept: List<ContentType> get() = fail
        override val authorization: Authorization get() = fail

        override val cookies: List<HttpCookie> =
            listOf(HttpCookie("name1", "value1"), HttpCookie("name2", "value2"))

        override val parts: List<HttpPart> =
            listOf(HttpPart("name1", "value1"), HttpPart("name2", "value2"))
    }

    @Test fun `Header convenience methods works properly`() {
        assertEquals("User Agent", TestRequest.userAgent())
        assertEquals("Referer", TestRequest.referer())
        assertEquals("Origin", TestRequest.origin())
    }

    @Test fun `Cookies map works properly`() {
        assertEquals(HttpCookie("name1", "value1"), TestRequest.cookiesMap()["name1"])
        assertEquals(HttpCookie("name2", "value2"), TestRequest.cookiesMap()["name2"])
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
        testQueryParameters = HttpFields()
        assertEquals(URL("http://localhost:9999/path"), TestRequest.url())
    }
}
