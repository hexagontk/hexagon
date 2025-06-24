package com.hexagontk.http.handlers

import com.hexagontk.core.media.TEXT_HTML
import com.hexagontk.core.media.TEXT_PLAIN
import com.hexagontk.http.model.*
import com.hexagontk.http.model.HttpMethod.*
import com.hexagontk.http.model.HttpProtocol.HTTPS
import com.hexagontk.http.model.FOUND_302
import com.hexagontk.http.model.BAD_GATEWAY_502
import com.hexagontk.http.model.INTERNAL_SERVER_ERROR_500
import com.hexagontk.http.patterns.TemplatePathPattern
import com.hexagontk.http.model.HttpCall
import com.hexagontk.http.model.HttpRequest
import com.hexagontk.http.model.HttpResponse
import org.junit.jupiter.api.Test
import java.lang.RuntimeException
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class HttpContextTest {

    private fun httpServerRequest(): HttpRequest =
        HttpRequest(
            method = POST,
            protocol = HTTPS,
            host = "127.0.0.1",
            port = 9999,
            path = "/path/v1",
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

    @Test fun `Context helper methods work properly`() {
        val context = HttpContext(
            request = httpServerRequest(),
            predicate = HttpPredicate(pathPattern = TemplatePathPattern("/path/{p1}")),
        )

        assertSame(context.method, context.event.request.method)
        assertSame(context.protocol, context.event.request.protocol)
        assertSame(context.host, context.event.request.host)
        assertEquals(context.port, context.event.request.port)
        assertSame(context.path, context.event.request.path)
        assertSame(context.queryParameters, context.event.request.queryParameters)
        assertSame(context.parts, context.event.request.parts)
        assertSame(context.formParameters, context.event.request.formParameters)
        assertSame(context.accept, context.event.request.accept)
        assertSame(context.certificateChain, context.event.request.certificateChain)
        assertEquals(context.partsMap, context.event.request.partsMap())
        assertEquals(context.uri, context.event.request.uri())
        assertSame(context.userAgent, context.event.request.userAgent())
        assertSame(context.referer, context.event.request.referer())
        assertSame(context.origin, context.event.request.origin())
        assertSame(context.certificate, context.event.request.certificate())
        assertEquals(context.status, context.event.response.status)
    }

    @Test fun `Send without parameters return the same response`() {
        val serverContext = HttpContext(
            request = httpServerRequest(),
            predicate = HttpPredicate(pathPattern = TemplatePathPattern("/path/{p1}", true)),
        )

        assertEquals(serverContext.request, serverContext.send().request)
        assertEqualHttpResponses(serverContext.response, serverContext.send().response)
    }

    @Test fun `Response helpers return correct values`() {
        val path = PathHandler(
            OnHandler("/ok") { ok() },
            OnHandler("/notFound") { notFound() },
            OnHandler("/noContent") { send(HttpResponse(status = NO_CONTENT_204)) },
            OnHandler("/accepted") { send(status = ACCEPTED_202) },
            OnHandler("/created") { created() },
            OnHandler("/badRequest") { badRequest() },
            OnHandler("/clientError") { send(FORBIDDEN_403) },
            OnHandler("/success") { send(NO_CONTENT_204) },
            OnHandler("/serverError") { send(BAD_GATEWAY_502) },
            OnHandler("/redirect") { send(FOUND_302) },
            OnHandler("/internalServerError") { internalServerError() },
            OnHandler("/internalServerErrorException") { internalServerError(RuntimeException()) },
            OnHandler("/serverErrorException") { serverError(BAD_GATEWAY_502, RuntimeException()) },
        )

        assertEquals(OK_200, path.process(HttpRequest(path = "/ok")).status)
        assertEquals(NOT_FOUND_404, path.process(HttpRequest(path = "/notFound")).status)
        assertEquals(NO_CONTENT_204, path.process(HttpRequest(path = "/noContent")).status)
        assertEquals(ACCEPTED_202, path.process(HttpRequest(path = "/accepted")).status)
        assertEquals(CREATED_201, path.process(HttpRequest(path = "/created")).status)
        assertEquals(BAD_REQUEST_400, path.process(HttpRequest(path = "/badRequest")).status)
        assertEquals(FORBIDDEN_403, path.process(HttpRequest(path = "/clientError")).status)
        assertEquals(NO_CONTENT_204, path.process(HttpRequest(path = "/success")).status)
        assertEquals(BAD_GATEWAY_502, path.process(HttpRequest(path = "/serverError")).status)
        assertEquals(FOUND_302, path.process(HttpRequest(path = "/redirect")).status)
        assertEquals(
            INTERNAL_SERVER_ERROR_500,
            path.process(HttpRequest(path = "/internalServerError")).status
        )

        path.process(HttpRequest(path = "/internalServerErrorException")).response.let {
            assertEquals(INTERNAL_SERVER_ERROR_500, it.status)
            assertTrue(it.bodyString().contains(RuntimeException::class.java.name))
            assertEquals(ContentType(TEXT_PLAIN).text, it.contentType?.text)
        }

        path.process(HttpRequest(path = "/serverErrorException")).response.let {
            assertEquals(BAD_GATEWAY_502, it.status)
            assertTrue(it.bodyString().contains(RuntimeException::class.java.name))
            assertEquals(ContentType(TEXT_PLAIN).text, it.contentType?.text)
        }
    }

    @Test fun `'next' executes the next handler in the chain`() {
        val context = HttpContext(
            HttpCall(httpServerRequest(), HttpResponse()),
            HttpPredicate(),
            listOf(
                OnHandler("*") { ok() },
            )
        )

        assertEquals(OK_200, context.next().response.status)
    }

    @Test fun `Client errors helpers returns proper status`() {
        assertEquals(UNAUTHORIZED_401, HttpContext().unauthorized().status)
        assertEquals(FORBIDDEN_403, HttpContext().forbidden().status)
    }

    @Test fun `Context can change the request`() {
        val path = PathHandler(
            BeforeHandler("*") { receive(body = request.bodyString() + "_modified") },
            OnHandler("/test") { ok(body = request.body) },
        )

        path.process(HttpRequest(path = "/test")).apply {
            assertEquals(OK_200, status)
            assertEquals("_modified", response.bodyString())
        }

        path.process(HttpRequest(path = "/test", body = "body")).apply {
            assertEquals(OK_200, status)
            assertEquals("body_modified", response.bodyString())
        }
    }

    private fun assertEqualCookies(a: Cookie, b: Any?) {
        if (a.javaClass != b?.javaClass) assert(false)

        b as Cookie

        if (a.name != b.name) assert(false)
        if (a.value != b.value) assert(false)
        if (a.maxAge != b.maxAge) assert(false)
        if (a.secure != b.secure) assert(false)
        if (a.path != b.path) assert(false)
        if (a.httpOnly != b.httpOnly) assert(false)
        if (a.domain != b.domain) assert(false)
        if (a.sameSite != b.sameSite) assert(false)
        if (!(a.expires?.equals(b.expires) ?: (b.expires == null))) assert(false)
    }

    private fun assertEqualHttpResponses(a: HttpResponsePort, b: Any?) {
        if (a.javaClass != b?.javaClass) assert(false)

        b as HttpResponse

        if (a.body != b.body) assert(false)
        if (a.headers != b.headers) assert(false)
        if (a.contentType?.text != b.contentType?.text) assert(false)
        if (a.status != b.status) assert(false)
        if (a.reason != b.reason) assert(false)
        if (a.contentLength != b.contentLength) assert(false)
        if (a.onConnect != b.onConnect) assert(false)
        if (a.onBinary != b.onBinary) assert(false)
        if (a.onText != b.onText) assert(false)
        if (a.onPing != b.onPing) assert(false)
        if (a.onPong != b.onPong) assert(false)
        if (a.onClose != b.onClose) assert(false)

        a.cookies.forEachIndexed { index, cookie ->
            assertEqualCookies(cookie, b.cookies[index])
        }
    }
}
