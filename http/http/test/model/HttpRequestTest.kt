package com.hexagontk.http.model

import com.hexagontk.core.fail
import com.hexagontk.core.media.TEXT_CSS
import com.hexagontk.core.media.TEXT_HTML
import com.hexagontk.core.media.TEXT_PLAIN
import com.hexagontk.core.media.TEXT_RICHTEXT
import com.hexagontk.core.security.loadKeyStore
import com.hexagontk.core.urlOf
import com.hexagontk.http.model.HttpMethod.POST
import com.hexagontk.http.model.HttpMethod.PUT
import com.hexagontk.http.model.HttpProtocol.*
import com.hexagontk.http.patterns.PathPattern
import com.hexagontk.http.patterns.createPathPattern
import java.security.cert.X509Certificate
import org.junit.jupiter.api.Test
import java.net.URI
import kotlin.test.*

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
        var testQueryParameters: Parameters = Parameters(
            Parameter("qp1", "value1"), Parameter("qp1", "value2")
        )
    }

    private object TestEmptyRequest: HttpRequestPort {
        override val method: HttpMethod get() = fail
        override val protocol: HttpProtocol = testProtocol
        override val host: String = testHost
        override val port: Int = testPort
        override val path: String = testPath
        override val queryParameters: Parameters = testQueryParameters
        override val formParameters: Parameters get() = fail
        override val body: Any get() = fail
        override val headers: Headers = Headers()
        override val contentType: ContentType get() = fail
        override val accept: List<ContentType> get() = fail
        override val authorization: Authorization? = authorization()
        override val certificateChain: List<X509Certificate> get() = fail
        override val contentLength: Long get() = fail
        override val pathPattern: PathPattern? get() = fail
        override val pathParameters: Map<String, Any> get() = fail

        override fun with(
            body: Any,
            headers: Headers,
            contentType: ContentType?,
            method: HttpMethod,
            protocol: HttpProtocol,
            host: String,
            port: Int,
            path: String,
            queryParameters: Parameters,
            parts: List<HttpPart>,
            formParameters: Parameters,
            cookies: List<Cookie>,
            accept: List<ContentType>,
            authorization: Authorization?,
            certificateChain: List<X509Certificate>,
            pathPattern: PathPattern?,
            pathParameters: Map<String, Any>,
        ): HttpRequestPort =
            fail

        override val cookies: List<Cookie> =
            listOf(Cookie("name1", "value1"), Cookie("name2", "value2"))

        override val parts: List<HttpPart> =
            listOf(HttpPart("name1", "value1"), HttpPart("name2", "value2"))
    }

    private object TestRequest : HttpRequestPort {
        override val method: HttpMethod get() = fail
        override val protocol: HttpProtocol = testProtocol
        override val host: String = testHost
        override val port: Int get() = testPort
        override val path: String = testPath
        override val queryParameters: Parameters get() = testQueryParameters
        override val formParameters: Parameters get() = fail
        override val body: Any get() = fail
        override val headers: Headers get() = testHeaders
        override val contentType: ContentType get() = fail
        override val accept: List<ContentType> get() = fail
        override val authorization: Authorization? = authorization()
        override val certificateChain: List<X509Certificate> get() = fail
        override val contentLength: Long get() = fail
        override val pathPattern: PathPattern? get() = fail
        override val pathParameters: Map<String, Any> get() = fail

        override fun with(
            body: Any,
            headers: Headers,
            contentType: ContentType?,
            method: HttpMethod,
            protocol: HttpProtocol,
            host: String,
            port: Int,
            path: String,
            queryParameters: Parameters,
            parts: List<HttpPart>,
            formParameters: Parameters,
            cookies: List<Cookie>,
            accept: List<ContentType>,
            authorization: Authorization?,
            certificateChain: List<X509Certificate>,
            pathPattern: PathPattern?,
            pathParameters: Map<String, Any>,
        ): HttpRequestPort =
            fail

        override val cookies: List<Cookie> =
            listOf(Cookie("name1", "value1"), Cookie("name2", "value2"))

        override val parts: List<HttpPart> =
            listOf(HttpPart("name1", "value1"), HttpPart("name2", "value2"))
    }

    private val keyStoreResource = "hexagontk.p12"
    private val keyStoreUrl = urlOf("classpath:$keyStoreResource")
    private val keyStore = loadKeyStore(keyStoreUrl, keyStoreResource.reversed())
    private val certificate = keyStore.getCertificate("hexagontk")
    private val certificates = listOf(certificate as X509Certificate)

    private fun httpRequestData(): HttpRequest =
        HttpRequest(
            method = POST,
            protocol = HTTPS,
            host = "127.0.0.1",
            port = 9999,
            path = "/path",
            queryParameters = Parameters(Parameter("k", "v")),
            headers = Headers(Header("h1", "h1v1"), Header("h1", "h1v2")),
            body = "request",
            parts = listOf(HttpPart("n", "b")),
            formParameters =
                Parameters(Parameter("fp1", "fp1v1"), Parameter("fp1", "fp1v2")),
            cookies = listOf(Cookie("cn", "cv")),
            contentType = ContentType(TEXT_PLAIN),
            certificateChain = emptyList(),
            accept = listOf(ContentType(TEXT_HTML)),
        )

    @Test fun `HTTP Request comparison works ok`() {
        val httpRequest = httpRequestData()

        assertEquals(httpRequest, httpRequest)
        assertEqualHttpRequests(httpRequestData(), httpRequestData())
        assertFalse(httpRequest.equals(""))

        val headers = Headers(Header("h1", "v1"))
        val parts = listOf(HttpPart("p", "v"))
        val formParameters = Parameters(Parameter("h1", "v1"))
        val cookies = listOf(Cookie("p", "v"))
        val contentType = ContentType(TEXT_RICHTEXT)
        val accept = listOf(ContentType(TEXT_CSS))

        assertNotEquals(httpRequest, httpRequest.with(method = PUT))
        assertNotEquals(httpRequest, httpRequest.with(protocol = HTTP2))
        assertNotEquals(httpRequest, httpRequest.with(host = "host"))
        assertNotEquals(httpRequest, httpRequest.with(port = 1234))
        assertNotEquals(httpRequest, httpRequest.with(path = "/aPath"))
        assertNotEquals(httpRequest, httpRequest.with(headers = headers))
        assertNotEquals(httpRequest, httpRequest.with(body = "body"))
        assertNotEquals(httpRequest, httpRequest.with(parts = parts))
        assertNotEquals(httpRequest, httpRequest.with(formParameters = formParameters))
        assertNotEquals(httpRequest, httpRequest.with(cookies = cookies))
        assertNotEquals(httpRequest, httpRequest.with(contentType = contentType))
        assertNotEquals(httpRequest, httpRequest.with(certificateChain = certificates))
        assertNotEquals(httpRequest, httpRequest.with(accept = accept))
        assertNotEquals(
            httpRequest,
            httpRequest.with(queryParameters =
                Parameters(Parameter("k", "v"), Parameter("k", "v2"))
            )
        )

        assertEqualHttpRequests(httpRequest, httpRequestData())
        assertEqualHttpRequests(
            httpRequest.with(contentType = null),
            httpRequestData().with(contentType = null)
        )
    }

    @Test fun `'certificate' returns the first chain certificate`() {
        val requestData = httpRequestData()
        assertNull(requestData.certificate())
        assertEquals(certificate,requestData.with(certificateChain = certificates).certificate())
    }

    @Test fun `Common headers access methods work as expected`() {
        val requestData = httpRequestData()

        assertNull(requestData.userAgent())
        assertNull(requestData.referer())
        assertNull(requestData.origin())

        requestData.with(
            headers = Headers(
                Header("user-agent", "ua"),
                Header("referer", "r"),
                Header("origin", "o"),
            )
        ).let {
            assertEquals("ua", it.userAgent())
            assertEquals("r", it.referer())
            assertEquals("o", it.origin())
        }
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
        assertEquals("value", TestRequest.authorization()?.body)

        val invalidAuthorization = Header("authorization", "Basic words header")
        testHeaders = Headers(invalidAuthorization) + testHeaders
        assertEquals("Basic", TestRequest.authorization()?.type)
        assertEquals("words header", TestRequest.authorization()?.body)
        assertEquals("Basic words header", TestRequest.authorization()?.text)

        assertNull(TestEmptyRequest.authorization)
    }

    @Test fun `Cookies map works properly`() {
        assertEqualCookies(Cookie("name1", "value1"), TestRequest.cookiesMap()["name1"])
        assertEqualCookies(Cookie("name2", "value2"), TestRequest.cookiesMap()["name2"])
        assertNull(TestRequest.cookiesMap()["name3"])
    }

    @Test fun `Parts map works properly`() {
        assertEqualHttpParts(HttpPart("name1", "value1"), TestRequest.partsMap()["name1"])
        assertEqualHttpParts(HttpPart("name2", "value2"), TestRequest.partsMap()["name2"])
        assertNull(TestRequest.partsMap()["name3"])
    }

    @Test fun `URL is generated correctly`() {
        assertEquals(URI("http://localhost/path?qp1=value1&qp1=value2"), TestRequest.uri())
        testPort = 9999
        assertEquals(URI("http://localhost:9999/path?qp1=value1&qp1=value2"), TestRequest.uri())
        testQueryParameters = Parameters()
        assertEquals(URI("http://localhost:9999/path"), TestRequest.uri())
    }

    @Test fun `HTTP Request operators work ok`() {
        val httpRequest = httpRequestData()

        val header = Header("h", "v")
        assertEqualHttpRequests(
            httpRequest + header,
            httpRequest.with(headers = httpRequest.headers + header)
        )
        assertEqualHttpRequests(
            httpRequest + Headers(header),
            httpRequest.with(headers = httpRequest.headers + header)
        )

        val part = HttpPart("a", "b")
        assertEqualHttpRequests(
            httpRequest + part,
            httpRequest.with(parts = httpRequest.parts + part)
        )

        val cookie = Cookie("n", "v")
        assertEqualHttpRequests(
            httpRequest + cookie,
            httpRequest.with(cookies = httpRequest.cookies + cookie)
        )
    }

    @Test fun `HTTP Request with path pattern create a correct path`() {
        val request = HttpRequest(
            pathPattern = createPathPattern("/{path}"),
            pathParameters = mapOf("path" to "path"),
            method = POST,
            protocol = HTTPS,
            host = "127.0.0.1",
            port = 9999,
            queryParameters = Parameters(Parameter("k", "v")),
            headers = Headers(Header("h1", "h1v1"), Header("h1", "h1v2")),
            body = "request",
            parts = listOf(HttpPart("n", "b")),
            formParameters = Parameters(Parameter("fp1", "fp1")),
            cookies = listOf(Cookie("cn", "cv")),
            contentType = ContentType(TEXT_PLAIN),
            certificateChain = emptyList(),
            accept = listOf(ContentType(TEXT_HTML)),
        )

        assertEquals(POST, request.method)
        assertEquals(HTTPS, request.protocol)
        assertEquals("127.0.0.1", request.host)
        assertEquals(9999, request.port)
        assertEquals("/path", request.path)
        assertEquals("request", request.body)
        assertEquals(emptyList(), request.certificateChain)
        assertEquals(createPathPattern("/{path}").pattern, request.pathPattern?.pattern)
        assertEquals(mapOf("path" to "path"), request.pathParameters)

        val request2 = HttpRequest(
            pathPattern = createPathPattern("/{path}"),
            pathParameters = mapOf("path" to "path"),
        )

        assertEquals("/path", request2.path)
        assertEquals(createPathPattern("/{path}").pattern, request2.pathPattern?.pattern)
        assertEquals(mapOf("path" to "path"), request2.pathParameters)
    }
}
