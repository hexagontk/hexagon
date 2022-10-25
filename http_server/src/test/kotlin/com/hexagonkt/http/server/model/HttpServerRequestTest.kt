package com.hexagonkt.http.server.model

import com.hexagonkt.core.media.TextMedia.CSS
import com.hexagonkt.core.media.TextMedia.HTML
import com.hexagonkt.core.media.TextMedia.PLAIN
import com.hexagonkt.core.media.TextMedia.RICHTEXT
import com.hexagonkt.http.model.HttpMethod.*
import com.hexagonkt.http.model.HttpProtocol.HTTP2
import com.hexagonkt.http.model.HttpProtocol.HTTPS
import com.hexagonkt.core.security.loadKeyStore
import com.hexagonkt.http.model.*
import org.junit.jupiter.api.Test
import java.net.URL
import java.security.cert.X509Certificate
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

internal class HttpServerRequestTest {

    private val keyStoreResource = "hexagonkt.p12"
    private val keyStoreUrl = URL("classpath:$keyStoreResource")
    private val keyStore = loadKeyStore(keyStoreUrl, keyStoreResource.reversed())
    private val certificate = keyStore.getCertificate("hexagonkt")
    private val certificates = listOf(certificate as X509Certificate)

    private fun httpServerRequestData(): HttpServerRequest =
        HttpServerRequest(
            method = POST,
            protocol = HTTPS,
            host = "127.0.0.1",
            port = 9999,
            path = "/path",
            queryParameters = QueryParameters(QueryParameter("k", "v")),
            headers = Headers(Header("h1", "h1v1", "h1v2")),
            body = "request",
            parts = listOf(HttpPart("n", "b")),
            formParameters = FormParameters(FormParameter("fp1", "fp1v1", "fp1v2")),
            cookies = listOf(Cookie("cn", "cv")),
            contentType = ContentType(PLAIN),
            certificateChain = emptyList(),
            accept = listOf(ContentType(HTML)),
        )

    @Test fun `HTTP Server Request comparison works ok`() {
        val httpServerRequest = httpServerRequestData()

        assertEquals(httpServerRequest, httpServerRequest)
        assertEquals(httpServerRequestData(), httpServerRequestData())
        assertFalse(httpServerRequest.equals(""))

        val headers = Headers(Header("h1", "v1"))
        val parts = listOf(HttpPart("p", "v"))
        val formParameters = FormParameters(FormParameter("h1", "v1"))
        val cookies = listOf(Cookie("p", "v"))
        val contentType = ContentType(RICHTEXT)
        val accept = listOf(ContentType(CSS))

        assertNotEquals(httpServerRequest, httpServerRequest.copy(method = PUT))
        assertNotEquals(httpServerRequest, httpServerRequest.copy(protocol = HTTP2))
        assertNotEquals(httpServerRequest, httpServerRequest.copy(host = "host"))
        assertNotEquals(httpServerRequest, httpServerRequest.copy(port = 1234))
        assertNotEquals(httpServerRequest, httpServerRequest.copy(path = "/aPath"))
        assertNotEquals(httpServerRequest, httpServerRequest.copy(headers = headers))
        assertNotEquals(httpServerRequest, httpServerRequest.copy(body = "body"))
        assertNotEquals(httpServerRequest, httpServerRequest.copy(parts = parts))
        assertNotEquals(httpServerRequest, httpServerRequest.copy(formParameters = formParameters))
        assertNotEquals(httpServerRequest, httpServerRequest.copy(cookies = cookies))
        assertNotEquals(httpServerRequest, httpServerRequest.copy(contentType = contentType))
        assertNotEquals(httpServerRequest, httpServerRequest.copy(certificateChain = certificates))
        assertNotEquals(httpServerRequest, httpServerRequest.copy(accept = accept))
        assertNotEquals(
            httpServerRequest,
            httpServerRequest.copy(queryParameters = QueryParameters(QueryParameter("k", "v", "v2")))
        )

        assertEquals(httpServerRequest.hashCode(), httpServerRequestData().hashCode())
        assertEquals(
            httpServerRequest.copy(contentType = null).hashCode(),
            httpServerRequestData().copy(contentType = null).hashCode()
        )
    }

    @Test fun `'certificate' returns the first chain certificate`() {
        val requestData = httpServerRequestData()
        assertNull(requestData.certificate())
        assertEquals(certificate,requestData.copy(certificateChain = certificates).certificate())
    }

    @Test fun `Common headers access methods work as expected`() {
        val requestData = httpServerRequestData()

        assertNull(requestData.userAgent())
        assertNull(requestData.referer())
        assertNull(requestData.origin())

        requestData.copy(
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
}
