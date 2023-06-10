package com.hexagonkt.templates

import com.hexagonkt.core.Jvm
import java.net.URL
import java.util.*

interface TemplatePort {

    fun render(url: URL, context: Map<String, *>, locale: Locale = Jvm.locale): String

    fun render(
        name: String,
        templates: Map<String, String>,
        context: Map<String, *>,
        locale: Locale = Jvm.locale
    ): String

    fun render(template: String, context: Map<String, *>, locale: Locale = Jvm.locale): String =
        render("_template_", mapOf("_template_" to template), context, locale)
}
