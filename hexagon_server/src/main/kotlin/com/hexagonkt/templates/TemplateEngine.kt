package com.hexagonkt.templates

import java.util.*

interface TemplateEngine {
    fun render (resource: String, locale: Locale, context: Map<String, *>): String
}
