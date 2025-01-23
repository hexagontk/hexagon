package com.hexagontk.templates.examples

import com.hexagontk.core.text.Glob
import com.hexagontk.core.urlOf
import com.hexagontk.templates.SampleTemplateAdapter
import com.hexagontk.templates.TemplateManager
import com.hexagontk.templates.TemplatePort
import org.junit.jupiter.api.Test
import java.net.URL
import java.util.*
import kotlin.test.assertEquals

class TemplatesTest {

    @Test fun registerEngine() {

        // templateRegex
        TemplateManager.adapters = mapOf(
            Regex(".*\\.html") to SampleTemplateAdapter("html"),
            Regex(".*\\.txt") to SampleTemplateAdapter("text"),
            Regex(".*") to SampleTemplateAdapter("others"),
        )
        // templateRegex

        checkRegisteredAdapters()

        // templateGlob
        TemplateManager.adapters = mapOf(
            Glob("*.html").regex to SampleTemplateAdapter("html"),
            Glob("*.txt").regex to SampleTemplateAdapter("text"),
            Glob("*").regex to SampleTemplateAdapter("others"),
        )
        // templateGlob

        checkRegisteredAdapters()
    }

    @Test fun renderPage() {
        TemplateManager.adapters = mapOf(Glob("*").regex to EchoTemplateAdapter)

        // templateUsage
        val context = mapOf("key1" to "value1", "key2" to "value2")
        val rendered = TemplateManager.render(urlOf("classpath:template.txt"), context)
        // templateUsage

        assert(rendered.startsWith("classpath:template.txt {key1=value1, key2=value2"))
        assert(rendered.contains("_template_"))
        assert(rendered.contains("_now_"))
    }

    private fun checkRegisteredAdapters() {
        val context = mapOf<String, Any>()
        val html = TemplateManager.render(urlOf("classpath:template.html"), context)
        val plain = TemplateManager.render(urlOf("classpath:template.txt"), context)
        val others = TemplateManager.render(urlOf("classpath:template.other"), context)

        assertEquals("html:classpath:template.html", html)
        assertEquals("text:classpath:template.txt", plain)
        assertEquals("others:classpath:template.other", others)
    }

    private object EchoTemplateAdapter : TemplatePort {

        override fun render(url: URL, context: Map<String, *>, locale: Locale): String =
            "$url $context"

        override fun render(
            name: String, templates: Map<String, String>, context: Map<String, *>, locale: Locale
        ): String =
            throw UnsupportedOperationException()
    }
}
