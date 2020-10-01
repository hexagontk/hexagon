package com.hexagonkt.templates

import java.util.Locale

/**
 * The TemplateManager handles multiple templates engines.
 */
object TemplateManager {

    private val engines: MutableMap<String, TemplateEngine> = mutableMapOf()

    private const val PREFIX_DELIMITER = ":"

    /**
     * Register a template engine under a prefix.
     */
    fun register(prefix: String, engine: TemplateEngine) {
        engines[prefix] = engine
    }

    /**
     * Render a template with a registered template engine.
     *
     * @param prefixedResource selects engine and template, i.e. "html:template.html" uses the
     * engine registered under prefix "html" to render the template "template.html"
     *
     * @throws IllegalArgumentException if no engine for prefix was found
     */
    fun render(prefixedResource: String, locale: Locale, context: Map<String, *>): String {
        val (prefix: String, engine: TemplateEngine) = engine(prefixedResource)
            ?: throw IllegalArgumentException("No adapter found for resource: $prefixedResource")

        return engine.render(plainResource(prefixedResource, prefix), locale, context)
    }

    private fun engine(prefixedResource: String): Map.Entry<String, TemplateEngine>? =
        engines.filterKeys { prefix -> prefixedResource.startsWith(prefix + PREFIX_DELIMITER) }
            .entries
            .firstOrNull()

    private fun plainResource(prefixedResource: String, prefix: String): String =
        prefixedResource.removePrefix(prefix + PREFIX_DELIMITER)
}
