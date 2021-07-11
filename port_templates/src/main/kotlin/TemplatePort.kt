package com.hexagonkt.templates

import com.hexagonkt.helpers.Jvm
import java.net.URL
import java.util.*

/**
 * TODO
 *  - Add utilities to test templates
 *  - Replace resource type from String to URL
 */
interface TemplatePort {
    fun render(url: URL, context: Map<String, *>, locale: Locale = Jvm.locale): String
}
