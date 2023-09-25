package com.hexagonkt.http.server.callbacks

import com.hexagonkt.core.media.TEXT_MARKDOWN
import com.hexagonkt.http.model.*
import com.hexagonkt.http.patterns.TemplatePathPattern
import com.hexagonkt.http.handlers.HttpContext
import com.hexagonkt.http.handlers.HttpPredicate
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.IllegalStateException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

internal class FileCallbackTest {

    @Test fun `Request referencing parent directories raises an error`() {
        val e = assertFailsWith<IllegalStateException> {
            processFileCallback(".", "/params/../1", "/params/*")
        }

        assertEquals("Requested path cannot contain '..': ../1", e.message)
    }

    @Test fun `Invalid filter raises an error`() {
        val e = assertFailsWith<IllegalStateException> {
            processFileCallback(".", "/params/1/2", "/params/{a}/{b}")
        }

        assertEquals("File loading require a single path parameter or none", e.message)
    }

    @Test fun `Filter with one parameter resolves to a file`() {
        val result = processFileCallback(".", "/params/README.md", "/params/*")

        assertEquals(OK_200, result.status)
        assertEquals(ContentType(TEXT_MARKDOWN), result.contentType)
    }

    @Test fun `Filter with one parameter resolves to a missing file`() {
        val result = processFileCallback(".", "/params/foo.bar", "/params/*")

        assertEquals(NOT_FOUND_404, result.status)
        assertEquals("File 'foo.bar' not found", result.bodyString())
    }

    @Test fun `Filter with no parameter resolves to the callback's file`() {
        val result = processFileCallback("README.md", "/params", "/params")

        assertEquals(OK_200, result.status)
        assertEquals(ContentType(TEXT_MARKDOWN), result.contentType)
    }

    @Test fun `Return file without known media type`() {
        val result = processFileCallback(".", "/build.gradle.kts", "/*")

        assertEquals(OK_200, result.status)
        assertNull(result.contentType)
    }

    @Test fun `Return file with known media type`() {
        val result = processFileCallback(".", "/README.md", "/*")

        assertEquals(OK_200, result.status)
        assertEquals(ContentType(TEXT_MARKDOWN), result.contentType)
    }

    private fun processFileCallback(
        filePath: String,
        requestPath: String,
        pathPattern: String
    ): HttpResponsePort =
        FileCallback(File(filePath))(
            HttpContext(
                request = HttpRequest(path = requestPath),
                predicate = HttpPredicate(pathPattern = TemplatePathPattern(pathPattern)),
            )
        ).response
}
