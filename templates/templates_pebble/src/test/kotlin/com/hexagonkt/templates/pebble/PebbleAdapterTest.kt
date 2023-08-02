package com.hexagonkt.templates.pebble

import com.hexagonkt.core.urlOf
import kotlin.test.Test
import java.time.LocalDateTime
import java.util.Locale

internal class PebbleAdapterTest {

    private val locale = Locale.getDefault()

    @Test fun `Dates are converted properly`() {
        val context = mapOf("localDate" to LocalDateTime.of(2000, 12, 31, 23, 45))
        val resource = "classpath:templates/test.pebble.html"
        val html = PebbleAdapter().render(urlOf(resource), context, locale)
        assert(html.contains("23:45"))
        assert(html.contains("2000"))
        assert(html.contains("31"))
    }

    @Test fun `Templates can be extended`() {
        val context = mapOf("localDate" to LocalDateTime.of(2000, 12, 31, 23, 45))
        val resource = "classpath:templates/index.pebble.html"
        val html = PebbleAdapter().render(urlOf(resource), context, locale)
        assert(html.contains("23:45"))
        assert(html.contains("2000"))
        assert(html.contains("31"))
        assert(html.contains("<head>"))
    }

    @Test fun `Templates can be loaded from custom sources`() {
        val context = mapOf("localDate" to LocalDateTime.of(2000, 12, 31, 23, 45))
        val resources = listOf(
            urlOf("classpath:templates/index.pebble.html"),
            urlOf("classpath:templates/main.pebble.html"),
            urlOf("classpath:templates/test.pebble.html"),
        )
        val name = resources.first().path
        val templates = resources.map { it.path to it.readText() }
        val html = PebbleAdapter().render(name, templates.toMap(), context, locale)
        assert(html.contains("23:45"))
        assert(html.contains("2000"))
        assert(html.contains("31"))
        assert(html.contains("<head>"))
    }

    @Test fun `Template code can be processed directly`() {
        val context = mapOf("localDate" to LocalDateTime.of(2000, 12, 31, 23, 45))
        val text = PebbleAdapter().render("Template {{ localDate }}", context, locale)
        assert(text.contains("Template"))
        assert(text.contains("23:45"))
        assert(text.contains("2000"))
        assert(text.contains("31"))
    }
}
