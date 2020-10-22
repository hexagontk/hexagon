package com.hexagonkt.templates

import java.util.*

/**
 * TODO Add code to test templates (check unresolved variables in bundles, multi-language, etc.)
 */
interface TemplatePort {
    fun render (resource: String, locale: Locale, context: Map<String, *>): String =
        render(resource, locale, context, TemplateEngineSettings())

    fun render (resource: String, locale: Locale, vararg context: Pair<String, *>): String =
        render(resource, locale, linkedMapOf(*context))

    fun render(
        resource: String,
        locale: Locale,
        context: Map<String, *>,
        settings: TemplateEngineSettings
    ): String
}
