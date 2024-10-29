package com.hexagontk.templates.jte

import com.hexagontk.core.media.TEXT_CSS
import com.hexagontk.core.media.TEXT_HTML
import com.hexagontk.core.urlOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledInNativeImage
import java.time.LocalDateTime
import java.util.Locale
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class JteTest {

    private val locale = Locale.getDefault()

    @Test
    @DisabledInNativeImage
    fun `Dates are converted properly`() {
        val context = mapOf("localDate" to LocalDateTime.of(2000, 12, 31, 23, 45))
        val resource = "classpath:templates/test.jte"
        val html = Jte(TEXT_HTML).render(urlOf(resource), context, locale)
        assert(html.contains("23:45"))
        assert(html.contains("2000"))
        assert(html.contains("31"))
    }

    @Test fun `Dates are converted properly with precompiled templates`() {
        val context = mapOf("localDate" to LocalDateTime.of(2000, 12, 31, 23, 45))
        val resource = "classpath:test.jte"
        val adapter = Jte(TEXT_HTML, precompiled = true)
        val html = adapter.render(urlOf(resource), context, locale)
        assert(html.contains("23:45"))
        assert(html.contains("2000"))
        assert(html.contains("31"))
    }

    @Test fun `Literal templates are not supported`() {
        val context = mapOf("localDate" to LocalDateTime.of(2000, 12, 31, 23, 45))
        val e = assertFailsWith<UnsupportedOperationException> {
            Jte(TEXT_HTML).render("template code", context, locale)
        }
        assertEquals("jte does not support memory templates", e.message)
    }

    @Test fun `Invalid jte adapters throw exceptions on creation`() {
        assertIllegalState("Unsupported media type not in: text/html, text/plain (text/css)") {
            Jte(TEXT_CSS)
        }
        assertIllegalState("Invalid base schema not in: classpath, file (http)") {
            Jte(TEXT_HTML, urlOf("http://example.com"))
        }
        assertIllegalState("Precompiled base must be classpath URLs (file)") {
            Jte(TEXT_HTML, urlOf("file://example.com"), true)
        }
    }

    private inline fun assertIllegalState(message: String, block: () -> Unit) {
        assertEquals(message, assertFailsWith(IllegalStateException::class, block).message)
    }
}
