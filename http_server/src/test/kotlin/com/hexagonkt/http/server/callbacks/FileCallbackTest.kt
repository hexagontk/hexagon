package com.hexagonkt.http.server.callbacks

import com.hexagonkt.core.handlers.Context
import com.hexagonkt.core.media.TextMedia
import com.hexagonkt.http.model.ContentType
import com.hexagonkt.http.model.SuccessStatus
import com.hexagonkt.http.patterns.LiteralPathPattern
import com.hexagonkt.http.patterns.TemplatePathPattern
import com.hexagonkt.http.server.handlers.HttpServerContext
import com.hexagonkt.http.server.handlers.HttpServerPredicate
import com.hexagonkt.http.server.model.HttpServerCall
import com.hexagonkt.http.server.model.HttpServerRequest
import org.junit.jupiter.api.Test
import java.io.File
import java.lang.IllegalStateException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

internal class FileCallbackTest {

    @Test fun `Invalid filter raises an error`() {
        val e = assertFailsWith<IllegalStateException> {
            val callback = FileCallback(File("."))
            val context = Context(
                HttpServerCall(),
                HttpServerPredicate(pathPattern = LiteralPathPattern("/noParam"))
            )
            callback(HttpServerContext(context))
        }

        assertEquals("File loading require a single path parameter", e.message)
    }

    @Test fun `Return file without known media type`() {
        val callback = FileCallback(File("."))
        val result = callback(
            HttpServerContext(
                Context(
                    HttpServerCall(
                        HttpServerRequest(
                            path = "/build.gradle.kts"
                        )
                    ),
                    HttpServerPredicate(
                        pathPattern = TemplatePathPattern("/*")
                    )
                )
            )
        ).response

        assertEquals(SuccessStatus.OK, result.status)
        assertNull(result.contentType)
    }

    @Test fun `Return file with known media type`() {
        val callback = FileCallback(File("."))
        val result = callback(
            HttpServerContext(
                Context(
                    HttpServerCall(
                        HttpServerRequest(
                            path = "/README.md"
                        )
                    ),
                    HttpServerPredicate(
                        pathPattern = TemplatePathPattern("/*")
                    )
                )
            )
        ).response

        assertEquals(SuccessStatus.OK, result.status)
        assertEquals(ContentType(TextMedia.MARKDOWN), result.contentType)
    }
}
