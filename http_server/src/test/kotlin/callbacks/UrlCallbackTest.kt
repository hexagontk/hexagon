package com.hexagonkt.http.server.callbacks

import com.hexagonkt.core.handlers.Context
import com.hexagonkt.http.patterns.LiteralPathPattern
import com.hexagonkt.http.server.handlers.HttpServerContext
import com.hexagonkt.http.server.handlers.HttpServerPredicate
import com.hexagonkt.http.server.model.HttpServerCall
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.lang.IllegalStateException
import java.net.URL
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class UrlCallbackTest {

    @Test fun `Invalid filter raises an error`() = runBlocking {
        val e = assertFailsWith<IllegalStateException> {
            val callback = UrlCallback(URL("classpath:resource.txt"))
            val context = Context(
                HttpServerCall(),
                HttpServerPredicate(pathPattern = LiteralPathPattern("/noParam"))
            )
            callback(HttpServerContext(context))
        }

        assertEquals("URL loading require a single path parameter", e.message)
    }
}
