package com.hexagonkt.templates.pebble

import com.hexagonkt.templates.TemplateEngineSettings
import org.junit.jupiter.api.Test
import java.io.StringReader
import java.time.LocalDateTime
import java.util.Locale

internal class PebbleAdapterTest {

    private val locale = Locale.getDefault()

    @Test fun `Dates are converted properly`() {
        val context = "localDate" to LocalDateTime.of(2000, 12, 31, 23, 45)
        val html = PebbleAdapter.render("templates/test.pebble.html", locale, context)
        assert(html.contains("23:45"))
        assert(html.contains("2000"))
        assert(html.contains("31"))
    }

    @Test fun `Templates can be provided via settings`() {
        val settings =
            TemplateEngineSettings(loader = { StringReader("Content: {{ value }}") })

        val context = mapOf("value" to "A_VALUE")
        val html = PebbleAdapter.render("resource", locale, context, settings)
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
        assert(PebbleAdapter.render("resource1", locale, context, settings) == "template1")
        assert(PebbleAdapter.render("resource2", locale, context, settings) == "template2")
    }
}
