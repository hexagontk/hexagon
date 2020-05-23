package com.hexagonkt.templates.freemarker

import org.testng.annotations.Test
import java.time.LocalDateTime
import java.util.*

@Test class FreemarkerAdapterTest {

    fun `Templates are rendered properly`() {
        val html = FreemarkerAdapter.render("templates/test.freemarker.html", Locale.getDefault(), hashMapOf<String, Any>())
        print(html)
        assert(html.contains("This is a test template"))
    }

    fun `Templates with context are rendered properly`() {
        val context = mapOf(
            "testTitle" to "This is a test title",
            "testBody" to "This is a test body"
        )
        val html = FreemarkerAdapter.render("templates/test_context.freemarker.html", Locale.getDefault(), context)
        assert(html.contains("<title>This is a test title</title>"))
        assert(html.contains("<body>This is a test body</body>"))
    }

    fun `Dates are converted properly`() {
        val context = "localDate" to LocalDateTime.of(2000, 12, 31, 23, 45)
        val html = FreemarkerAdapter.render("templates/test_dates.freemarker.html", Locale.getDefault(), context)
        assert(html.contains("23:45"))
        assert(html.contains("2000"))
        assert(html.contains("31"))
    }
}
