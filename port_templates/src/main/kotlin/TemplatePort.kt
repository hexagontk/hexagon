package com.hexagonkt.templates

import com.hexagonkt.helpers.Jvm
import java.net.URL
import java.util.*

/**
 * TODO Add utilities to test templates
 */
interface TemplatePort {
    fun render(url: URL, context: Map<String, *>, locale: Locale = Jvm.locale): String
}
