package com.hexagonkt.templates

import java.util.*

/**
 * TODO Add code to test templates (check unresolved variables in bundles, multi-language, etc.)
 */
interface TemplatePort {
    fun render(resource: String, locale: Locale, context: Map<String, *>): String
}
