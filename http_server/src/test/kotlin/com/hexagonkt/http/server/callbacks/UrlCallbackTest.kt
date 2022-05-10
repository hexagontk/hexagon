package com.hexagonkt.http.server.callbacks

import com.hexagonkt.core.handlers.Context
import com.hexagonkt.core.media.TextMedia.PLAIN
import com.hexagonkt.http.model.ClientErrorStatus
import com.hexagonkt.http.model.ContentType
import com.hexagonkt.http.model.SuccessStatus
import com.hexagonkt.http.patterns.TemplatePathPattern
import com.hexagonkt.http.server.handlers.HttpServerContext
import com.hexagonkt.http.server.handlers.HttpServerPredicate
import com.hexagonkt.http.server.model.HttpServerCall
import com.hexagonkt.http.server.model.HttpServerRequest
import com.hexagonkt.http.server.model.HttpServerResponse
import org.junit.jupiter.api.Test
import java.lang.IllegalStateException
import java.net.URL
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

internal class UrlCallbackTest {

    @Test fun `Request referencing parent directories raises an error`() {
        val e = assertFailsWith<IllegalStateException> {
            processUrlCallback("classpath:", "/params/../1", "/params/*")
        }

        assertEquals("Requested path cannot contain '..': ../1", e.message)
    }

    @Test fun `Invalid filter raises an error`() {
        val e = assertFailsWith<IllegalStateException> {
            processUrlCallback("classpath:resource.txt", "/noParam/1/2", "/noParam/{a}/{b}")
        }

        assertEquals("URL loading require a single path parameter or none", e.message)
    }

    @Test fun `Filter with one parameter resolves to content`() {
        val result = processUrlCallback("classpath:", "/params/hexagonkt.p12", "/params/*")

        assertEquals(SuccessStatus.OK, result.status)
        assertNull(result.contentType)
    }

    @Test fun `Filter with one parameter resolves to content on a subdirectory`() {
        val result = processUrlCallback("classpath:dir", "/params/file.txt", "/params/*")

        assertEquals(SuccessStatus.OK, result.status)
        assertEquals(ContentType(PLAIN), result.contentType)
    }

    @Test fun `Filter with one parameter resolves to missing content`() {
        val result = processUrlCallback("classpath:", "/params/foo.bar", "/params/*")

        assertEquals(ClientErrorStatus.NOT_FOUND, result.status)
        assertEquals("classpath:foo.bar cannot be open", result.bodyString())
    }

    @Test fun `Filter with a directory resolves to missing content`() {
        val result = processUrlCallback("classpath:", "/params/foo/", "/params/*")

        assertEquals(ClientErrorStatus.NOT_FOUND, result.status)
        assertEquals("foo/ not found (folder)", result.bodyString())
    }

    @Test fun `Filter with no parameter resolves to the callback's URL`() {
        val result = processUrlCallback("classpath:hexagonkt.p12", "/params", "/params")

        assertEquals(SuccessStatus.OK, result.status)
        assertNull(result.contentType)
    }

    private fun processUrlCallback(
        url: String,
        requestPath: String,
        pathPattern: String
    ): HttpServerResponse =
        UrlCallback(URL(url))(
            HttpServerContext(
                Context(
                    HttpServerCall(HttpServerRequest(path = requestPath)),
                    HttpServerPredicate(pathPattern = TemplatePathPattern(pathPattern))
                )
            )
        ).response
}
