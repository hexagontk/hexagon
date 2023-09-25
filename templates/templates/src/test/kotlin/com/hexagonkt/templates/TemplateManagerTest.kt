package com.hexagonkt.templates

import com.hexagonkt.core.Glob
import com.hexagonkt.core.urlOf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFailsWith

internal class TemplateManagerTest {

    @Test fun `Use a single default engine`() {

        val context = mapOf<String, Any>()

        TemplateManager.adapters = mapOf(Regex(".*") to SampleTemplateAdapter("default"))

        val html = TemplateManager.render(urlOf("classpath:template.html"), context)
        val plain = TemplateManager.render(urlOf("classpath:template.txt"), context)

        assertEquals("default:classpath:template.html", html)
        assertEquals("default:classpath:template.txt", plain)
    }

    @Test fun `Use TemplateManager to handle a single template engine`() {

        val context = mapOf<String, Any>()

        TemplateManager.adapters = mapOf(Regex(".*\\.html") to SampleTemplateAdapter("html"))

        val html = TemplateManager.render(urlOf("classpath:template.html"), context)
        assertEquals("html:classpath:template.html", html)

        assertFailsWith<IllegalStateException> {
            TemplateManager.render(urlOf("classpath:template.txt"), context)
        }
    }

    @Test fun `Use TemplateManager to handle multiple template engines`() {

        val context = mapOf<String, Any>()

        TemplateManager.adapters = mapOf(
            Regex(".*\\.html") to SampleTemplateAdapter("html"),
            Regex(".*\\.txt") to SampleTemplateAdapter("text")
        )

        val html = TemplateManager.render(urlOf("classpath:template.html"), context)
        val plain = TemplateManager.render(urlOf("classpath:template.txt"), context)

        assertEquals("html:classpath:template.html", html)
        assertEquals("text:classpath:template.txt", plain)

        assertFailsWith<IllegalStateException> {
            TemplateManager.render(urlOf("classpath:template.svg"), context)
        }
    }

    @Test fun `Use overlapping patterns`() {

        val context = mapOf<String, Any>()

        TemplateManager.adapters = mapOf(
            Glob("*.txt").regex to SampleTemplateAdapter("txt"),
            Regex(".*") to SampleTemplateAdapter("*"),
        )

        val html = TemplateManager.render(urlOf("classpath:template.txt"), context)
        val plain = TemplateManager.render(urlOf("classpath:template.txz"), context)

        assertEquals("txt:classpath:template.txt", html)
        assertEquals("*:classpath:template.txz", plain)

        TemplateManager.adapters = mapOf(
            Glob("*").regex to SampleTemplateAdapter("*"),
            Glob("*.txt").regex to SampleTemplateAdapter("txt"),
        )

        val render = TemplateManager.render(urlOf("classpath:template.txt"), context)
        assertEquals("*:classpath:template.txt", render)
    }

    @Test fun `Throws IllegalArgumentException when no adapter is found for prefix`() {
        TemplateManager.adapters = emptyMap()
        val resource = "classpath:test.pebble.html"

        assertThrows<IllegalStateException> {
            TemplateManager.render(urlOf(resource), mapOf<String, Any>())
        }
    }
}
