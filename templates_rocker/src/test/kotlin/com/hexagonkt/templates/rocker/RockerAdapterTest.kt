package com.hexagonkt.templates.rocker

import kotlin.test.Test
import java.net.URL
import java.time.LocalDateTime
import java.util.Locale

internal class RockerAdapterTest {

    private val locale = Locale.getDefault()

    @Test fun `Dates are converted properly`() {
        val context = mapOf("localDate" to LocalDateTime.of(2000, 12, 31, 23, 45))
        val resource = "classpath:templates/test.rocker.html"
        val html = RockerAdapter().render(URL(resource), context, locale)
        assert(html.contains("23:45"))
        assert(html.contains("2000"))
        assert(html.contains("31"))
    }
}
