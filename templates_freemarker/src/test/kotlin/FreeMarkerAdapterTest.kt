package com.hexagonkt.templates.freemarker

import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.Locale

internal class FreeMarkerAdapterTest {

    private val locale = Locale.getDefault()

    @Test fun `Templates are rendered properly`() {
        val context = emptyMap<String, Any>()
        val html = FreeMarkerAdapter.render("templates/test.freemarker.html", locale, context)
        assert(html.contains("This is a test template"))
    }

    @Test fun `Templates with context are rendered properly`() {
        val resource = "templates/test_context.freemarker.html"
        val context = mapOf(
            "testTitle" to "This is a test title",
            "testBody" to "This is a test body"
        )
        val html = FreeMarkerAdapter.render(resource, locale, context)
        assert(html.contains("<title>This is a test title</title>"))
        assert(html.contains("<body>This is a test body</body>"))
    }

    @Test fun `Dates are converted properly`() {
        val context = mapOf("localDate" to LocalDateTime.of(2000, 12, 31, 23, 45))
        val html = FreeMarkerAdapter.render("templates/test_dates.freemarker.html", locale, context)
        assert(html.contains("23:45"))
        assert(html.contains("2000"))
        assert(html.contains("31"))
    }
}
