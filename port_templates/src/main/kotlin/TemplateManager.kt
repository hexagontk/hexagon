package com.hexagonkt.templates

import com.hexagonkt.helpers.toDate
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
     * @param resource selects engine and template, i.e. "html:template.html" uses the
     * engine registered under prefix "html" to render the template "template.html"
     *
     * @throws IllegalArgumentException if no engine for prefix was found
     */
    fun render(resource: String, locale: Locale, context: Map<String, *>): String {
        val adapter: TemplatePort = adapters
            .filter { it.key.matches(resource) }
            .firstNotNullOfOrNull { it.value  }
            ?: throw IllegalArgumentException("No adapter found for resource: $resource")

        val now = LocalDateTime.now().toDate()
        val defaultProperties = mapOf("_template_" to resource, "_now_" to now)
        return adapter.render(resource, locale, context + defaultProperties)
    }

    fun render(
        resource: String,
        locale: Locale = Locale.getDefault(),
        vararg context: Pair<String, *>
    ): String =
        render(resource, locale, linkedMapOf(*context))
}
