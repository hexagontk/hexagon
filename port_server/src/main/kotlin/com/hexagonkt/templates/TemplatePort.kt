package com.hexagonkt.templates

import java.util.*

interface TemplatePort {
    fun render (resource: String, locale: Locale, context: Map<String, *>): String
}
