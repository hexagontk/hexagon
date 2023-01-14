package com.hexagonkt.http.server.handlers

import com.hexagonkt.handlers.Context
import com.hexagonkt.core.media.TextMedia.HTML
import com.hexagonkt.core.media.TextMedia.PLAIN
import com.hexagonkt.core.disableChecks
import com.hexagonkt.http.model.*
import com.hexagonkt.http.model.ClientErrorStatus.*
import com.hexagonkt.http.model.HttpMethod.*
import com.hexagonkt.http.model.HttpProtocol.HTTPS
import com.hexagonkt.http.model.RedirectionStatus.FOUND
import com.hexagonkt.http.model.ServerErrorStatus.BAD_GATEWAY
import com.hexagonkt.http.model.ServerErrorStatus.INTERNAL_SERVER_ERROR
import com.hexagonkt.http.model.SuccessStatus.*
import com.hexagonkt.http.patterns.TemplatePathPattern
import com.hexagonkt.http.server.model.HttpServerCall
import com.hexagonkt.http.server.model.HttpServerRequest
import com.hexagonkt.http.server.model.HttpServerResponse
import kotlin.test.Test
import java.lang.RuntimeException
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class HttpServerContextTest {

    private fun httpServerRequest(): HttpServerRequest =
        HttpServerRequest(
            method = POST,
            protocol = HTTPS,
            host = "127.0.0.1",
            port = 9999,
            path = "/path/v1",
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

    @Test fun `Context helper methods work properly`() {
        val context = HttpServerContext(
            request = httpServerRequest(),
            predicate = HttpServerPredicate(pathPattern = TemplatePathPattern("/path/{p1}")),
        )

        assertSame(context.method, context.context.event.request.method)
        assertSame(context.protocol, context.context.event.request.protocol)
        assertSame(context.host, context.context.event.request.host)
        assertEquals(context.port, context.context.event.request.port)
        assertSame(context.path, context.context.event.request.path)
        assertSame(context.queryParameters, context.context.event.request.queryParameters)
        assertSame(context.parts, context.context.event.request.parts)
        assertSame(context.formParameters, context.context.event.request.formParameters)
        assertSame(context.accept, context.context.event.request.accept)
        assertSame(context.certificateChain, context.context.event.request.certificateChain)
        assertEquals(context.partsMap, context.context.event.request.partsMap())
        assertEquals(context.url, context.context.event.request.url())
        assertSame(context.userAgent, context.context.event.request.userAgent())
        assertSame(context.referer, context.context.event.request.referer())
        assertSame(context.origin, context.context.event.request.origin())
        assertSame(context.certificate, context.context.event.request.certificate())
        assertSame(context.status, context.context.event.response.status)
    }

    @Test fun `Loading path parameters fails for prefixes`() {
        val serverContext = HttpServerContext(
            request = httpServerRequest(),
            predicate = HttpServerPredicate(pathPattern = TemplatePathPattern("/path/{p1}", true)),
        )

        disableChecks = true
        assertEquals(mapOf("p1" to "v1", "0" to "v1"), serverContext.pathParameters)
        disableChecks = false
    }

    @Test fun `Send without parameters return the same response`() {
        val serverContext = HttpServerContext(
            request = httpServerRequest(),
            predicate = HttpServerPredicate(pathPattern = TemplatePathPattern("/path/{p1}", true)),
        )

        assertEquals(serverContext, serverContext.send())
    }

    @Test fun `Response helpers return correct values`() {
        val path = PathHandler(
            OnHandler("/ok") { ok() },
            OnHandler("/notFound") { notFound() },
            OnHandler("/noContent") { send(HttpServerResponse(status = NO_CONTENT)) },
            OnHandler("/accepted") { send(status = ACCEPTED) },
            OnHandler("/created") { created() },
            OnHandler("/badRequest") { badRequest() },
            OnHandler("/clientError") { clientError(FORBIDDEN) },
            OnHandler("/success") { success(NO_CONTENT) },
            OnHandler("/serverError") { serverError(BAD_GATEWAY) },
            OnHandler("/redirect") { redirect(FOUND) },
            OnHandler("/internalServerError") { internalServerError() },
            OnHandler("/internalServerErrorException") { internalServerError(RuntimeException()) },
            OnHandler("/serverErrorException") { serverError(BAD_GATEWAY, RuntimeException()) },
        )

        assertEquals(OK, path.process(HttpServerRequest(path = "/ok")).status)
        assertEquals(NOT_FOUND, path.process(HttpServerRequest(path = "/notFound")).status)
        assertEquals(NO_CONTENT, path.process(HttpServerRequest(path = "/noContent")).status)
        assertEquals(ACCEPTED, path.process(HttpServerRequest(path = "/accepted")).status)
        assertEquals(CREATED, path.process(HttpServerRequest(path = "/created")).status)
        assertEquals(BAD_REQUEST, path.process(HttpServerRequest(path = "/badRequest")).status)
        assertEquals(FORBIDDEN, path.process(HttpServerRequest(path = "/clientError")).status)
        assertEquals(NO_CONTENT, path.process(HttpServerRequest(path = "/success")).status)
        assertEquals(BAD_GATEWAY, path.process(HttpServerRequest(path = "/serverError")).status)
        assertEquals(FOUND, path.process(HttpServerRequest(path = "/redirect")).status)
        assertEquals(
            INTERNAL_SERVER_ERROR,
            path.process(HttpServerRequest(path = "/internalServerError")).status
        )

        path.process(HttpServerRequest(path = "/internalServerErrorException")).let {
            assertEquals(INTERNAL_SERVER_ERROR, it.status)
            assertTrue(it.bodyString().contains(RuntimeException::class.java.name))
            assertEquals(ContentType(PLAIN), it.contentType)
        }

        path.process(HttpServerRequest(path = "/serverErrorException")).let {
            assertEquals(BAD_GATEWAY, it.status)
            assertTrue(it.bodyString().contains(RuntimeException::class.java.name))
            assertEquals(ContentType(PLAIN), it.contentType)
        }
    }

    @Test fun `'next' executes the next handler in the chain`() {
        val context = HttpServerContext(
            Context(
                HttpServerCall(httpServerRequest(), HttpServerResponse()),
                HttpServerPredicate(),
                listOf(
                    OnHandler("*") { ok() },
                )
            )
        )

        assertEquals(OK, context.next().response.status)
    }

    @Test fun `Client errors helpers returns proper status`() {
        assertEquals(UNAUTHORIZED, HttpServerContext().unauthorized().status)
        assertEquals(FORBIDDEN, HttpServerContext().forbidden().status)
    }
}
