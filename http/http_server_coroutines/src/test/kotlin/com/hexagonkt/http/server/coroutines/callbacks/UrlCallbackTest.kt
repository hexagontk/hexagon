package com.hexagonkt.http.server.coroutines.callbacks

import com.hexagonkt.core.media.TEXT_PLAIN
import com.hexagonkt.core.urlOf
import com.hexagonkt.http.model.*
import com.hexagonkt.http.patterns.TemplatePathPattern
import com.hexagonkt.http.handlers.coroutines.HttpContext
import com.hexagonkt.http.handlers.coroutines.HttpPredicate
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.IllegalStateException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

internal class UrlCallbackTest {

    @Test fun `Request referencing parent directories raises an error`() = runBlocking {
        val e = assertFailsWith<IllegalStateException> {
            processUrlCallback("classpath:/", "/params/../1", "/params/*")
        }

        assertEquals("Requested path cannot contain '..': ../1", e.message)
    }

    @Test fun `Invalid filter raises an error`() = runBlocking {
        val e = assertFailsWith<IllegalStateException> {
            processUrlCallback("classpath:resource.txt", "/noParam/1/2", "/noParam/{a}/{b}")
        }

        assertEquals("URL loading require a single path parameter or none", e.message)
    }

    @Test fun `Filter with one parameter resolves to content`() = runBlocking {
        val result = processUrlCallback("classpath:/", "/params/hexagonkt.p12", "/params/*")

        assertEquals(OK_200, result.status)
        assertNull(result.contentType)
    }

    @Test fun `Filter with one parameter resolves to content on a subdirectory`() = runBlocking {
        val result = processUrlCallback("classpath:dir", "/params/file.txt", "/params/*")

        assertEquals(OK_200, result.status)
        assertEquals(ContentType(TEXT_PLAIN), result.contentType)
    }

    @Test fun `Filter with one parameter resolves to missing content`() = runBlocking {
        val result = processUrlCallback("classpath:/", "/params/foo.bar", "/params/*")

        assertEquals(NOT_FOUND_404, result.status)
        assertEquals("classpath:foo.bar cannot be open", result.bodyString())
    }

    @Test fun `Filter with a directory resolves to missing content`() = runBlocking {
        val result = processUrlCallback("classpath:/", "/params/foo/", "/params/*")

        assertEquals(NOT_FOUND_404, result.status)
        assertEquals("foo/ not found (folder)", result.bodyString())
    }

    @Test fun `Filter with no parameter resolves to the callback's URL`() = runBlocking {
        val result = processUrlCallback("classpath:hexagonkt.p12", "/params", "/params")

        assertEquals(OK_200, result.status)
        assertNull(result.contentType)
    }

    private suspend fun processUrlCallback(
        url: String,
        requestPath: String,
        pathPattern: String
    ): HttpResponsePort =
        UrlCallback(urlOf(url))(
            HttpContext(
                HttpCall(HttpRequest(path = requestPath)),
                HttpPredicate(pathPattern = TemplatePathPattern(pathPattern))
            )
        ).response
}
