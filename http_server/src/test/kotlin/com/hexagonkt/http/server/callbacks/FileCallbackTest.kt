package com.hexagonkt.http.server.callbacks

import com.hexagonkt.core.media.TextMedia
import com.hexagonkt.http.model.ClientErrorStatus
import com.hexagonkt.http.model.ContentType
import com.hexagonkt.http.model.SuccessStatus
import com.hexagonkt.http.patterns.TemplatePathPattern
import com.hexagonkt.http.server.handlers.HttpServerContext
import com.hexagonkt.http.server.handlers.HttpServerPredicate
import com.hexagonkt.http.server.model.HttpServerRequest
import com.hexagonkt.http.server.model.HttpServerResponse
import kotlin.test.Test
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

        assertEquals(SuccessStatus.OK, result.status)
        assertEquals(ContentType(TextMedia.MARKDOWN), result.contentType)
    }

    @Test fun `Filter with one parameter resolves to a missing file`() {
        val result = processFileCallback(".", "/params/foo.bar", "/params/*")

        assertEquals(ClientErrorStatus.NOT_FOUND, result.status)
        assertEquals("File 'foo.bar' not found", result.bodyString())
    }

    @Test fun `Filter with no parameter resolves to the callback's file`() {
        val result = processFileCallback("README.md", "/params", "/params")

        assertEquals(SuccessStatus.OK, result.status)
        assertEquals(ContentType(TextMedia.MARKDOWN), result.contentType)
    }

    @Test fun `Return file without known media type`() {
        val result = processFileCallback(".", "/build.gradle.kts", "/*")

        assertEquals(SuccessStatus.OK, result.status)
        assertNull(result.contentType)
    }

    @Test fun `Return file with known media type`() {
        val result = processFileCallback(".", "/README.md", "/*")

        assertEquals(SuccessStatus.OK, result.status)
        assertEquals(ContentType(TextMedia.MARKDOWN), result.contentType)
    }

    private fun processFileCallback(
        filePath: String,
        requestPath: String,
        pathPattern: String
    ): HttpServerResponse =
        FileCallback(File(filePath))(
            HttpServerContext(
                request = HttpServerRequest(path = requestPath),
                predicate = HttpServerPredicate(pathPattern = TemplatePathPattern(pathPattern)),
            )
        ).response
}
