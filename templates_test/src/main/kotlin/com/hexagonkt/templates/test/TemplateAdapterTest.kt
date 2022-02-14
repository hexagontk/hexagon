package com.hexagonkt.templates.test

import com.hexagonkt.templates.TemplateManager
import com.hexagonkt.templates.TemplatePort
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_DATE
import kotlin.test.assertFails

@TestInstance(PER_CLASS)
abstract class TemplateAdapterTest(private val url: URL, private val adapter: TemplatePort) {

    @BeforeAll fun `Set up adapter`() {
        TemplateManager.adapters = mapOf(Regex(".*") to adapter)
    }

    @Test fun `A static template is rendered properly`() {
        val context = mapOf("key1" to "value1", "key2" to "value2")
        val date = LocalDate.now().format(ISO_DATE)
        val rendered = TemplateManager.render(url, context)

        assert(rendered.contains("value1"))
        assert(rendered.contains("value2"))
        assert(rendered.contains("a"))
        assert(rendered.contains("localDate"))
        assert(rendered.contains(url.toString()))
        assert(rendered.contains(date))
    }

    @Test fun `A static template with missing properties is rendered properly`() {
        val context = emptyMap<String, Any>()
        val date = LocalDate.now().format(ISO_DATE)
        val rendered = TemplateManager.render(url, context)

        assert(rendered.contains("key1"))
        assert(rendered.contains("key2"))
        assert(rendered.contains("a"))
        assert(rendered.contains("localDate"))
        assert(rendered.contains(url.toString()))
        assert(rendered.contains(date))
    }

    @Test fun `Template with not proper properties is rendered`() {
        val context = mapOf("a" to "b")

        val date = LocalDate.now().format(ISO_DATE)
        val rendered = TemplateManager.render(url, context)

        assert(rendered.contains("key1"))
        assert(rendered.contains("key2"))
        assert(rendered.contains("b"))
        assert(rendered.contains("localDate"))
        assert(rendered.contains(url.toString()))
        assert(rendered.contains(date))
    }

    @Test fun `Invalid resource path will return empty map`() {
        val context = emptyMap<String, Any>()

        // TODO Decide if return a Toolkit exception, or leave the template engine handle it
        assertFails {
            TemplateManager.render(URL("classpath:invalid.html"), context)
        }
    }
}
