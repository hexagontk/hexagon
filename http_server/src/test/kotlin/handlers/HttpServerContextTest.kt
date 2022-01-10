package com.hexagonkt.http.server.handlers

import com.hexagonkt.core.handlers.Context
import com.hexagonkt.core.media.TextMedia.HTML
import com.hexagonkt.core.media.TextMedia.PLAIN
import com.hexagonkt.core.disableChecks
import com.hexagonkt.core.helpers.multiMapOfLists
import com.hexagonkt.http.model.ContentType
import com.hexagonkt.http.model.ClientErrorStatus.*
import com.hexagonkt.http.model.HttpCookie
import com.hexagonkt.http.model.HttpMethod.*
import com.hexagonkt.http.model.HttpPart
import com.hexagonkt.http.model.HttpProtocol.HTTPS
import com.hexagonkt.http.model.RedirectionStatus.FOUND
import com.hexagonkt.http.model.ServerErrorStatus.BAD_GATEWAY
import com.hexagonkt.http.model.ServerErrorStatus.INTERNAL_SERVER_ERROR
import com.hexagonkt.http.model.SuccessStatus.*
import com.hexagonkt.http.patterns.TemplatePathPattern
import com.hexagonkt.http.server.model.HttpServerCall
import com.hexagonkt.http.server.model.HttpServerRequest
import com.hexagonkt.http.server.model.HttpServerResponse
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class HttpServerContextTest {

    private fun httpServerRequest(): HttpServerRequest =
        HttpServerRequest(
            method = POST,
            protocol = HTTPS,
            host = "127.0.0.1",
            port = 9999,
            path = "/path/v1",
            queryString = "k=v",
            headers = multiMapOfLists("h1" to listOf("h1v1", "h1v2")),
            body = "request",
            parts = listOf(HttpPart("n", "b")),
            formParameters = multiMapOfLists("fp1" to listOf("fp1v1", "fp1v2")),
            cookies = listOf(HttpCookie("cn", "cv")),
            contentType = ContentType(PLAIN),
            certificateChain = emptyList(),
            accept = listOf(ContentType(HTML)),
        )

    @Test fun `'allParameters' return a map with all request parameters`() {
        val requestData = HttpServerContext(
            Context(
                HttpServerCall(httpServerRequest(), HttpServerResponse()),
                HttpServerPredicate(pathPattern = TemplatePathPattern("/path/{p1}"))
            )
        )

        assertEquals(mapOf("p1" to "v1", "0" to "v1"), requestData.pathParameters)

        assertEquals(
            mapOf(
                "fp1" to listOf("fp1v1", "fp1v2"),
                "k" to listOf("v"),
                "p1" to "v1",
                "0" to "v1",
            ),
            requestData.allParameters
        )

        val emptyRequest = HttpServerContext(
            Context(
                HttpServerCall(HttpServerRequest(), HttpServerResponse()),
                HttpServerPredicate()
            )
        )

        assertEquals(emptyMap(), emptyRequest.allParameters)
        assertEquals(emptyMap(), emptyRequest.pathParameters)
    }

    @Test fun `loading path parameters fails for prefixes`() {
        val serverContext = HttpServerContext(
            Context(
                HttpServerCall(httpServerRequest(), HttpServerResponse()),
                HttpServerPredicate(pathPattern = TemplatePathPattern("/path/{p1}", true))
            )
        )

        assertFailsWith<IllegalStateException> { serverContext.allParameters }

        disableChecks = true
        assertEquals(mapOf("p1" to "v1", "0" to "v1"), serverContext.pathParameters)
        disableChecks = false
    }

    @Test fun `Send without parameters return the same response`() {
        val serverContext = HttpServerContext(
            Context(
                HttpServerCall(httpServerRequest(), HttpServerResponse()),
                HttpServerPredicate(pathPattern = TemplatePathPattern("/path/{p1}", true))
            )
        )

        assertEquals(serverContext, serverContext.send())
    }

    @Test fun `Response helpers return correct values`() = runBlocking {
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
    }

    @Test fun `'next' executes the next handler in the chain`() = runBlocking {
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
}
