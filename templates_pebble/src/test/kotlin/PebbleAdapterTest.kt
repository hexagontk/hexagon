package com.hexagonkt.templates.pebble

import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

class PebbleAdapterTest {

    @Test fun `Dates are converted properly`() {
        val context = "localDate" to LocalDateTime.of(2000, 12, 31, 23, 45)
        val html = PebbleAdapter.render("templates/test.pebble.html", Locale.getDefault(), context)
        assert(html.contains("23:45"))
        assert(html.contains("2000"))
        assert(html.contains("31"))
    }
}
