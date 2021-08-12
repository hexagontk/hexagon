package com.hexagonkt.templates

import java.net.URL
import java.util.*

internal class SampleTemplateAdapter(private val prefix: String) : TemplatePort {

    override fun render(url: URL, context: Map<String, *>, locale: Locale): String =
        "$prefix:$url"
}
