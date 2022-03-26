package com.hexagonkt.templates

import com.hexagonkt.core.Jvm
import java.net.URL
import java.util.*

interface TemplatePort {
    fun render(url: URL, context: Map<String, *>, locale: Locale = Jvm.locale): String
}
