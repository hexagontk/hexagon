package com.hexagonkt.templates

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.util.Locale
import kotlin.test.assertFails

@TestInstance(PER_CLASS)
abstract class TemplateAdapterTest(private val resource: String, val adapter: () -> TemplatePort) {

    @Test fun `A static template is rendered properly`() {
        val engine = adapter()

        // templateAdapterUsage
        val context = mapOf("key1" to "value1", "key2" to "value2")
        val locale = Locale.getDefault()
        val rendered = engine.render(resource, context, locale)
        // templateAdapterUsage

        assert(rendered.contains("value1"))
        assert(rendered.contains("value2"))
        assert(rendered.contains("a"))
        assert(rendered.contains("localDate"))
    }

    @Test fun `A static template with a basePath is rendered properly `() {
        val engine = adapter()

        val context = emptyMap<String, Any>()
        val locale = Locale.getDefault()
        val rendered = engine.render(resource, context, locale)

        assert(rendered.contains("key1"))
        assert(rendered.contains("key2"))
        assert(rendered.contains("a"))
        assert(rendered.contains("localDate"))
    }

    @Test fun `Template with not proper properties is rendered`() {
        val engine = adapter()

        val locale = Locale.getDefault()
        val context = mapOf("a" to "b")

        val rendered = engine.render(resource, context, locale)

        assert(rendered.contains("key1"))
        assert(rendered.contains("key2"))
        assert(rendered.contains("b"))
        assert(rendered.contains("localDate"))
    }

    @Test fun `Invalid resource path will return empty map`() {
        val engine = adapter()
        val locale = Locale.getDefault()
        val context = emptyMap<String, Any>()
        // TODO Decide if return a Toolkit exception, or leave the template engine handle it
        assertFails {
            engine.render("invalid.html", context, locale)
        }
    }
}
