package com.hexagonkt.templates.freemarker

import com.hexagonkt.core.urlOf
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.Locale

internal class FreeMarkerAdapterTest {

    private val locale = Locale.getDefault()

    @Test fun `Templates are rendered properly`() {
        val context = mapOf<String, Any>("_now_" to LocalDateTime.now())
        val url = urlOf("classpath:templates/test.freemarker.html")
        val html = FreeMarkerAdapter().render(url, context, locale)
        assert(html.contains("This is a test template"))
    }

    @Test fun `Templates with context are rendered properly`() {
        val resource = "classpath:templates/test_context.freemarker.html"
        val context = mapOf(
            "testTitle" to "This is a test title",
            "testBody" to "This is a test body"
        )
        val html = FreeMarkerAdapter().render(urlOf(resource), context, locale)
        assert(html.contains("<title>This is a test title</title>"))
        assert(html.contains("<body>This is a test body</body>"))
    }

    @Test fun `Dates are converted properly`() {
        val context = mapOf("localDate" to LocalDateTime.of(2000, 12, 31, 23, 45))
        val url = urlOf("classpath:templates/test_dates.freemarker.html")
        val html = FreeMarkerAdapter().render(url, context, locale)
        assert(html.contains("23:45"))
        assert(html.contains("2000"))
        assert(html.contains("31"))
    }

    @Test fun `Template code can be processed directly`() {
        val context = mapOf("localDate" to LocalDateTime.of(2000, 12, 31, 23, 45))
        val text = FreeMarkerAdapter().render("Template \${localDate}", context, locale)
        assert(text.contains("Template"))
        assert(text.contains("23:45"))
        assert(text.contains("2000"))
        assert(text.contains("31"))
    }
}
