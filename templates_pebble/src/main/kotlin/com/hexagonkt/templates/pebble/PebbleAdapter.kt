package com.hexagonkt.templates.pebble

import com.hexagonkt.templates.TemplatePort
import com.mitchellbosecke.pebble.PebbleEngine
import java.io.StringWriter
import java.net.URL
import java.util.*

class PebbleAdapter(cache: Boolean = true, maxRenderedSize: Int = -1) : TemplatePort {

    private val engine: PebbleEngine = PebbleEngine.Builder()
        .cacheActive(cache)
        .maxRenderedSize(maxRenderedSize)
        .build()

    override fun render(url: URL, context: Map<String, *>, locale: Locale): String {
        val writer = StringWriter()
        val resource = url.file
        engine.getTemplate(resource).evaluate(writer, context, locale)
        return writer.toString()
    }
}
