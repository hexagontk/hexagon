package com.hexagonkt.templates

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.URL
import java.util.Locale

internal class TemplateManagerTest {

    private class TestTemplateAdapter(val prefix: String) : TemplatePort {
        override fun render(url: URL, context: Map<String, *>, locale: Locale): String =
            "$prefix:$url"
    }

    @Test fun `Use TemplateManager to handle multiple template engines`() {

        val locale = Locale.getDefault()
        val context = mapOf<String, Any>()

        // templateAdapterRegistration
        TemplateManager.adapters = mapOf(
            Regex(".*\\.html") to TestTemplateAdapter("html"),
            Regex(".*\\.txt") to TestTemplateAdapter("text")
        )

        val html = TemplateManager.render(URL("classpath:template.html"), context, locale)
        val plain = TemplateManager.render(URL("classpath:template.txt"), context, locale)
        // templateAdapterRegistration

        assertEquals("html:classpath:template.html", html)
        assertEquals("text:classpath:template.txt", plain)
    }

    @Test fun `Throws IllegalArgumentException when no adapter is found for prefix`() {
        TemplateManager.adapters = emptyMap()
        val locale = Locale.getDefault()
        val resource = "classpath:test.pebble.html"

        assertThrows<IllegalArgumentException> {
            TemplateManager.render(URL(resource), mapOf<String, Any>(), locale)
        }
    }
}
