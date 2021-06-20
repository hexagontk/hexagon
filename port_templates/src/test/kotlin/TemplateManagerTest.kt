package com.hexagonkt.templates

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Locale

internal class TemplateManagerTest {

    private class TestTemplateAdapter(val prefix: String) : TemplatePort {
        override fun render(resource: String, locale: Locale, context: Map<String, *>): String =
            "$prefix:$resource"
    }

    @Test fun `Use TemplateManager to handle multiple template engines`() {

        val locale = Locale.getDefault()
        val context = mapOf<String, Any>()

        // templateEngineRegistration
        TemplateManager.adapters = mapOf(
            Regex(".*\\.html") to TestTemplateAdapter("html"),
            Regex(".*\\.txt") to TestTemplateAdapter("text")
        )

        val html = TemplateManager.render("template.html", locale, context)
        val plain = TemplateManager.render("template.txt", locale, context)
        // templateEngineRegistration

        assertEquals("html:template.html", html)
        assertEquals("text:template.txt", plain)
    }

    @Test fun `Throws IllegalArgumentException when no adapter is found for prefix`() {
        TemplateManager.adapters = emptyMap()
        val locale = Locale.getDefault()
        val prefixedResource = "test.pebble.html"

        assertThrows<IllegalArgumentException> {
            TemplateManager.render(prefixedResource, locale, mapOf<String, Any>())
        }
    }
}
