package com.hexagonkt.http.server.callbacks

import com.hexagonkt.core.handlers.Context
import com.hexagonkt.http.patterns.LiteralPathPattern
import com.hexagonkt.http.server.handlers.HttpServerContext
import com.hexagonkt.http.server.handlers.HttpServerPredicate
import com.hexagonkt.http.server.model.HttpServerCall
import org.junit.jupiter.api.Test
import java.io.File
import java.lang.IllegalStateException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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
}
