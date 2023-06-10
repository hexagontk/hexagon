package com.hexagonkt.templates

import java.net.URL
import java.util.*

internal class SampleTemplateAdapter(private val prefix: String) : TemplatePort {

    override fun render(url: URL, context: Map<String, *>, locale: Locale): String =
        "$prefix:$url"

    override fun render(
        name: String, templates: Map<String, String>, context: Map<String, *>, locale: Locale
    ): String =
        prefix + (templates[name] ?: "empty")
}
