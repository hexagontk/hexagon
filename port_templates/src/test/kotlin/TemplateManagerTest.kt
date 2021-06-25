package com.hexagonkt.templates

import com.hexagonkt.helpers.Glob
import com.hexagonkt.templates.TemplateManager.injectTemplateAdapters
import com.hexagonkt.injection.InjectionManager
import com.hexagonkt.injection.forceBind
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalStateException
import java.util.Locale
import kotlin.test.assertFailsWith

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
    @Test fun `Adapters are injected correctly`() {
        InjectionManager.module.forceBind<TemplatePort>(".*.html", VoidTemplateAdapter)
        assert(injectTemplateAdapters().map { it.key.pattern }.contains(".*.html"))
        InjectionManager.module.forceBind<TemplatePort>(Glob("*.md"), VoidTemplateAdapter)
        assert(injectTemplateAdapters().map { it.key.pattern }.contains(Glob("*.md").regex.pattern))
        InjectionManager.module.forceBind<TemplatePort>(Regex(".*.txt"), VoidTemplateAdapter)
        assert(injectTemplateAdapters().map { it.key.pattern }.contains(Regex(".*.txt").pattern))
        InjectionManager.module.forceBind<TemplatePort>(0, VoidTemplateAdapter)
        assertFailsWith<IllegalStateException> { injectTemplateAdapters() }

        InjectionManager.module.clear()
    }

    object VoidTemplateAdapter : TemplatePort {
        override fun render(resource: String, context: Map<String, *>, locale: Locale): String =
            "Not implemented"
    }
}
