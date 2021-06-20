package com.hexagonkt.templates

import java.util.*

/**
 * TODO
 *  - Add utilities to test templates
 *  - Replace resource type from String to URL
 */
interface TemplatePort {
    fun render(resource: String, locale: Locale, context: Map<String, *>): String
}
