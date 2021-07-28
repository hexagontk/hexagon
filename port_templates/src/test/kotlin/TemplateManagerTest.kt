package com.hexagonkt.templates

import com.hexagonkt.helpers.Glob
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.URL
import java.util.Locale
import kotlin.test.assertFailsWith

internal class TemplateManagerTest {

    private class TestTemplateAdapter(val prefix: String) : TemplatePort {
        override fun render(url: URL, context: Map<String, *>, locale: Locale): String =
            "$prefix:$url"
    }

    @Test fun `Use a single default engine`() {

        val context = mapOf<String, Any>()

        TemplateManager.adapters = mapOf(Regex(".*") to TestTemplateAdapter("default"))

        val html = TemplateManager.render(URL("classpath:template.html"), context)
        val plain = TemplateManager.render(URL("classpath:template.txt"), context)

        assertEquals("default:classpath:template.html", html)
        assertEquals("default:classpath:template.txt", plain)
    }

    @Test fun `Use TemplateManager to handle a single template engine`() {

        val context = mapOf<String, Any>()

        TemplateManager.adapters = mapOf(Regex(".*\\.html") to TestTemplateAdapter("html"))

        val html = TemplateManager.render(URL("classpath:template.html"), context)
        assertEquals("html:classpath:template.html", html)

        assertFailsWith<IllegalStateException> {
            TemplateManager.render(URL("classpath:template.txt"), context)
        }
    }

    @Test fun `Use TemplateManager to handle multiple template engines`() {

        val context = mapOf<String, Any>()

        TemplateManager.adapters = mapOf(
            Regex(".*\\.html") to TestTemplateAdapter("html"),
            Regex(".*\\.txt") to TestTemplateAdapter("text")
        )

        val html = TemplateManager.render(URL("classpath:template.html"), context)
        val plain = TemplateManager.render(URL("classpath:template.txt"), context)

        assertEquals("html:classpath:template.html", html)
        assertEquals("text:classpath:template.txt", plain)

        assertFailsWith<IllegalStateException> {
            TemplateManager.render(URL("classpath:template.svg"), context)
        }
    }

    @Test fun `Use overlapping patterns`() {

        val context = mapOf<String, Any>()

        TemplateManager.adapters = mapOf(
            Glob("*.txt").regex to TestTemplateAdapter("txt"),
            Regex(".*") to TestTemplateAdapter("*"),
        )

        val html = TemplateManager.render(URL("classpath:template.txt"), context)
        val plain = TemplateManager.render(URL("classpath:template.txz"), context)

        assertEquals("txt:classpath:template.txt", html)
        assertEquals("*:classpath:template.txz", plain)

        TemplateManager.adapters = mapOf(
            Glob("*").regex to TestTemplateAdapter("*"),
            Glob("*.txt").regex to TestTemplateAdapter("txt"),
        )

        val render = TemplateManager.render(URL("classpath:template.txt"), context)
        assertEquals("*:classpath:template.txt", render)
    }

    @Test fun `Throws IllegalArgumentException when no adapter is found for prefix`() {
        TemplateManager.adapters = emptyMap()
        val resource = "classpath:test.pebble.html"

        assertThrows<IllegalStateException> {
            TemplateManager.render(URL(resource), mapOf<String, Any>())
        }
    }
}
