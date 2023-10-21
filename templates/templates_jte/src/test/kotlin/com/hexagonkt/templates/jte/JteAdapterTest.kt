package com.hexagonkt.templates.jte

import com.hexagonkt.core.media.TEXT_CSS
import com.hexagonkt.core.media.TEXT_HTML
import com.hexagonkt.core.urlOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledInNativeImage
import java.time.LocalDateTime
import java.util.Locale
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class JteAdapterTest {

    private val locale = Locale.getDefault()

    @Test
    @DisabledInNativeImage
    fun `Dates are converted properly`() {
        val context = mapOf("localDate" to LocalDateTime.of(2000, 12, 31, 23, 45))
        val resource = "classpath:templates/test.jte"
        val html = JteAdapter(TEXT_HTML).render(urlOf(resource), context, locale)
        assert(html.contains("23:45"))
        assert(html.contains("2000"))
        assert(html.contains("31"))
    }

    @Test fun `Dates are converted properly with precompiled templates`() {
        val context = mapOf("localDate" to LocalDateTime.of(2000, 12, 31, 23, 45))
        val resource = "classpath:test.jte"
        val adapter = JteAdapter(TEXT_HTML, precompiled = true)
        val html = adapter.render(urlOf(resource), context, locale)
        assert(html.contains("23:45"))
        assert(html.contains("2000"))
        assert(html.contains("31"))
    }

    @Test fun `Literal templates are not supported`() {
        val context = mapOf("localDate" to LocalDateTime.of(2000, 12, 31, 23, 45))
        val e = assertFailsWith<UnsupportedOperationException> {
            JteAdapter(TEXT_HTML).render("template code", context, locale)
        }
        assertEquals("jte does not support memory templates", e.message)
    }

    @Test fun `Invalid jte adapters throw exceptions on creation`() {
        assertIllegalState("Unsupported media type not in: text/html, text/plain (text/css)") {
            JteAdapter(TEXT_CSS)
        }
        assertIllegalState("Invalid base schema not in: classpath, file (http)") {
            JteAdapter(TEXT_HTML, urlOf("http://example.com"))
        }
        assertIllegalState("Precompiled base must be classpath URLs (file)") {
            JteAdapter(TEXT_HTML, urlOf("file://example.com"), true)
        }
    }

    private inline fun assertIllegalState(message: String, block: () -> Unit) {
        assertEquals(message, assertFailsWith(IllegalStateException::class, block).message)
    }
}
