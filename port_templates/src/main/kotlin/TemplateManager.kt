package com.hexagonkt.templates

import com.hexagonkt.helpers.Jvm
import java.net.URL
import java.time.LocalDateTime
import java.util.Locale

/**
 * The TemplateManager handles multiple templates adapters.
 */
object TemplateManager {

    var adapters: Map<Regex, TemplatePort> = emptyMap()
        set(value) {
            field = value
            defaultAdapter =
                if (value.size == 1) {
                    val first = value.entries.first()
                    if (first.key.pattern == ".*") first.value else null
                }
                else {
                    null
                }
        }

    private var defaultAdapter: TemplatePort? = null

    /**
     * Render a template with a registered template engine.
     *
     * @param url Location of the template.
     * @param context Data to use when the template is processed.
     * @param locale Locale used to process the template. If not passed, system's locale is used.
     *
     * @throws IllegalStateException Thrown when no engine for URL was found.
     */
    fun render(url: URL, context: Map<String, *>, locale: Locale = Jvm.locale): String {
        val now = LocalDateTime.now()
        val defaultProperties = mapOf("_template_" to url, "_now_" to now)
        return findAdapter(url).render(url, context + defaultProperties, locale)
    }

    private fun findAdapter(url: URL): TemplatePort =
        defaultAdapter
            ?: adapters
                .filter { it.key.matches(url.toString()) }
                .firstNotNullOfOrNull { it.value  }
                ?: error("No adapter found for resource: $url")
}
