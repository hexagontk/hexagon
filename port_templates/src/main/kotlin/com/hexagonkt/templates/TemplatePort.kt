package com.hexagonkt.templates

import java.util.*

/**
 * TODO Assign different engines to templates regex. I.e.: *.peb.html or fremarker.*.html
 * TODO Add code to test templates (check unresolved variables in bundles, multilanguage, etc.)
 */
interface TemplatePort {
    fun render (resource: String, locale: Locale, context: Map<String, *>): String

    fun render (resource: String, locale: Locale, vararg context: Pair<String, *>): String =
        render (resource, locale, linkedMapOf(*context))
}
