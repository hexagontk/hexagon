package com.hexagonkt.templates

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Locale

internal class TemplateManagerTest {

    private class TestTemplateAdapter(val prefix: String) : TemplatePort {
        override fun render(resource: String, context: Map<String, *>, locale: Locale): String =
            "$prefix:$resource"
    }

    @Test fun `Use TemplateManager to handle multiple template engines`() {

        val locale = Locale.getDefault()
        val context = mapOf<String, Any>()

        // templateAdapterRegistration
        TemplateManager.adapters = mapOf(
            Regex(".*\\.html") to TestTemplateAdapter("html"),
            Regex(".*\\.txt") to TestTemplateAdapter("text")
        )

        val html = TemplateManager.render("template.html", context, locale)
        val plain = TemplateManager.render("template.txt", context, locale)
        // templateAdapterRegistration

        assertEquals("html:template.html", html)
        assertEquals("text:template.txt", plain)
    }

    @Test fun `Throws IllegalArgumentException when no adapter is found for prefix`() {
        TemplateManager.adapters = emptyMap()
        val locale = Locale.getDefault()
        val prefixedResource = "test.pebble.html"

        assertThrows<IllegalArgumentException> {
            TemplateManager.render(prefixedResource, mapOf<String, Any>(), locale)
        }
    }
}
