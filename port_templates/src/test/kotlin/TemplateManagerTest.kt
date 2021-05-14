package com.hexagonkt.templates

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Locale

internal class TemplateManagerTest {

    private class ResourceTemplateAdapter(private val marker: String) : TemplatePort {
        override fun render(
            resource: String,
            locale: Locale,
            context: Map<String, *>,
            settings: TemplateEngineSettings
        ): String =
            "$marker:$resource"
    }

    @Test fun `Use TemplateManager to handle multiple template engines`() {
        val htmlTemplateAdapter = ResourceTemplateAdapter("HTML")
        val plainTextTemplateAdapter = ResourceTemplateAdapter("PLAIN")

        val locale = Locale.getDefault()
        val context = mapOf<String, Any>()

        // templateEngineRegistration
        TemplateManager.register("html", TemplateEngine(htmlTemplateAdapter))
        TemplateManager.register("plain", TemplateEngine(plainTextTemplateAdapter))

        val html = TemplateManager.render("html:template.html", locale, context)
        val plain = TemplateManager.render("plain:template.txt", locale, context)
        // templateEngineRegistration

        assertEquals("HTML:template.html", html)
        assertEquals("PLAIN:template.txt", plain)

    }

    @Test fun `Throws IllegalArgumentException when no adapter is found for prefix`() {
        val locale = Locale.getDefault()
        val prefixedResource = "unknown:test.pebble.html"

        assertThrows<IllegalArgumentException> {
            TemplateManager.render(prefixedResource, locale, mapOf<String, Any>())
        }
    }
}
