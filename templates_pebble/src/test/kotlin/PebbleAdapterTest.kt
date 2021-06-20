package com.hexagonkt.templates.pebble

import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.Locale

internal class PebbleAdapterTest {

    private val locale = Locale.getDefault()

    @Test fun `Dates are converted properly`() {
        val context = mapOf("localDate" to LocalDateTime.of(2000, 12, 31, 23, 45))
        val html = PebbleAdapter.render("templates/test.pebble.html", locale, context)
        assert(html.contains("23:45"))
        assert(html.contains("2000"))
        assert(html.contains("31"))
    }
}
