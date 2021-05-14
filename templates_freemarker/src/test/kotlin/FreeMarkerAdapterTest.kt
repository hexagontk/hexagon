package com.hexagonkt.templates.freemarker

import com.hexagonkt.templates.TemplateEngineSettings
import org.junit.jupiter.api.Test
import java.io.StringReader
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
        val context = "localDate" to LocalDateTime.of(2000, 12, 31, 23, 45)
        val html = FreeMarkerAdapter.render("templates/test_dates.freemarker.html", locale, context)
        assert(html.contains("23:45"))
        assert(html.contains("2000"))
        assert(html.contains("31"))
    }

    @Test fun `Templates can be provided via settings`() {
        val settings =
            TemplateEngineSettings(loader = { StringReader("Content: \${value}") })

        val context = mapOf("value" to "A_VALUE")
        val html = FreeMarkerAdapter.render("resource", locale, context, settings)
        assert(html == "Content: A_VALUE")
    }

    @Test fun `Multiple templates can be defined`() {
        val settings = TemplateEngineSettings(loader = {
            when (it) {
                "resource1" -> StringReader("template1")
                "resource2" -> StringReader("template2")
                else -> null
            }
        })

        val context = emptyMap<String, Any>()
        assert(FreeMarkerAdapter.render("resource1", locale, context, settings) == "template1")
        assert(FreeMarkerAdapter.render("resource2", locale, context, settings) == "template2")
    }
}
