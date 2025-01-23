package com.hexagontk.http.server.callbacks

import com.hexagontk.core.media.TEXT_PLAIN
import com.hexagontk.http.model.*
import com.hexagontk.http.patterns.TemplatePathPattern
import com.hexagontk.http.handlers.HttpContext
import com.hexagontk.http.handlers.HttpPredicate
import org.junit.jupiter.api.Test
import kotlin.IllegalStateException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

internal class UrlCallbackTest {

    @Test fun `Request referencing parent directories raises an error`() {
        val e = assertFailsWith<IllegalStateException> {
            processUrlCallback("classpath:/", "/params/../1", "/params/*")
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
        val result = processUrlCallback("classpath:/", "/params/hexagontk.p12", "/params/*")

        assertEquals(OK_200, result.status)
        assertNull(result.contentType)
    }

    @Test fun `Filter with one parameter resolves to content on a subdirectory`() {
        val result = processUrlCallback("classpath:dir", "/params/file.txt", "/params/*")

        assertEquals(OK_200, result.status)
        assertEquals(ContentType(TEXT_PLAIN).text, result.contentType?.text)
    }

    @Test fun `Filter with one parameter resolves to missing content`() {
        val result = processUrlCallback("classpath:/", "/params/foo.bar", "/params/*")

        assertEquals(NOT_FOUND_404, result.status)
        assertEquals("classpath:foo.bar cannot be open", result.bodyString())
    }

    @Test fun `Filter with a directory resolves to missing content`() {
        val result = processUrlCallback("classpath:/", "/params/foo/", "/params/*")

        assertEquals(NOT_FOUND_404, result.status)
        assertEquals("foo/ not found (folder)", result.bodyString())
    }

    @Test fun `Filter with no parameter resolves to the callback's URL`() {
        val result = processUrlCallback("classpath:hexagontk.p12", "/params", "/params")

        assertEquals(OK_200, result.status)
        assertNull(result.contentType)
    }

    private fun processUrlCallback(
        url: String,
        requestPath: String,
        pathPattern: String
    ): HttpResponsePort =
        UrlCallback(url)(
            HttpContext(
                HttpCall(HttpRequest(path = requestPath)),
                HttpPredicate(pathPattern = TemplatePathPattern(pathPattern))
            )
        ).response
}
