package com.hexagonkt.templates

import com.hexagonkt.helpers.toDate
import com.hexagonkt.helpers.Jvm
import java.net.URL
import java.time.LocalDateTime
import java.util.Locale

/**
 * The TemplateManager handles multiple templates engines.
 */
object TemplateManager {

    var adapters: Map<Regex, TemplatePort> = emptyMap()

    /**
     * Render a template with a registered template engine.
     *
     * @param url selects engine and template, i.e. "html:template.html" uses the engine registered
     * under prefix "html" to render the template "template.html"
     *
     * @throws IllegalArgumentException if no engine for prefix was found
     */
    fun render(url: URL, context: Map<String, *>, locale: Locale = Jvm.locale): String {
        return render(findAdapter(url), url, context, locale)
    }

    fun render(
        adapter: TemplatePort,
        url: URL,
        context: Map<String, *>,
        locale: Locale = Jvm.locale
    ): String {

        val now = LocalDateTime.now().toDate()
        val defaultProperties = mapOf("_template_" to url, "_now_" to now)
        return adapter.render(url, context + defaultProperties, locale)
    }

    fun render(url: URL, locale: Locale = Jvm.locale, vararg context: Pair<String, *>): String =
        render(findAdapter(url), url, locale, *context)

    fun render(
        adapter: TemplatePort,
        url: URL,
        locale: Locale = Jvm.locale,
        vararg context: Pair<String, *>
    ): String =
        render(adapter, url, linkedMapOf(*context), locale)

    private fun findAdapter(url: URL): TemplatePort =
        adapters
            .filter { it.key.matches(url.toString()) }
            .firstNotNullOfOrNull { it.value  }
            ?: throw IllegalArgumentException("No adapter found for resource: $url")
}
