package com.hexagonkt.templates.rocker

import com.hexagonkt.core.urlOf
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.Locale
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class RockerAdapterTest {

    private val locale = Locale.getDefault()

    @Test fun `Dates are converted properly`() {
        val context = mapOf("localDate" to LocalDateTime.of(2000, 12, 31, 23, 45))
        val resource = "classpath:templates/test.rocker.html"
        val html = RockerAdapter().render(urlOf(resource), context, locale)
        assert(html.contains("23:45"))
        assert(html.contains("2000"))
        assert(html.contains("31"))
    }

    @Test fun `Literal templates are not supported`() {
        val context = mapOf("localDate" to LocalDateTime.of(2000, 12, 31, 23, 45))
        val e = assertFailsWith<UnsupportedOperationException> {
            RockerAdapter().render("template code", context, locale)
        }
        assertEquals("Rocker does not support memory templates", e.message)
    }
}
