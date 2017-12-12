package com.hexagonkt.templates.pebble

import org.testng.annotations.Test
import java.time.LocalDateTime
import java.util.*

@Test class PebbleAdapterTest {
    fun `Dates are converted properly`() {
        val context = mapOf("localDate" to LocalDateTime.of(2000, 12, 31, 23, 45))
        val html = PebbleAdapter.render("templates/test.pebble.html", Locale.getDefault(), context)
        assert(html.contains("23:45"))
        assert(html.contains("2000"))
        assert(html.contains("31"))
    }
}
