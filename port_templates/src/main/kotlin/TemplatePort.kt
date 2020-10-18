package com.hexagonkt.templates

import java.util.*

/**
 * TODO Assign different engines to templates regex. I.e.: *.peb.html or FreeMarker.*.html
 * TODO Add code to test templates (check unresolved variables in bundles, multi-language, etc.)
 */
interface TemplatePort {
    fun render (resource: String, locale: Locale, context: Map<String, *>): String

    fun render (resource: String, locale: Locale, vararg context: Pair<String, *>): String =
        render (resource, locale, linkedMapOf(*context))
}
