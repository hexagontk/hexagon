package com.hexagonkt.templates.freemarker

import org.testng.annotations.Test
import java.time.LocalDateTime
import java.util.*

@Test class FreemarkerAdapterTest {
    fun `Dates are converted properly`() {
        val context = "localDate" to LocalDateTime.of(2000, 12, 31, 23, 45)
        val html = FreemarkerAdapter.render("templates/test.freemarker.html", Locale.getDefault(), context)
        assert(html.contains("23:45"))
        assert(html.contains("2000"))
        assert(html.contains("31"))
    }
}
