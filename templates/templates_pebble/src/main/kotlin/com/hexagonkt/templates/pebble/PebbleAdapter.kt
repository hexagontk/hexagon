package com.hexagonkt.templates.pebble

import com.hexagonkt.templates.TemplatePort
import io.pebbletemplates.pebble.PebbleEngine
import io.pebbletemplates.pebble.loader.MemoryLoader
import java.io.StringWriter
import java.net.URL
import java.util.*

class PebbleAdapter(
    private val cache: Boolean = true,
    private val maxRenderedSize: Int = -1
) : TemplatePort {

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

    override fun render(
        name: String, templates: Map<String, String>, context: Map<String, *>, locale: Locale
    ): String {

        val writer = StringWriter()
        val memoryLoader = MemoryLoader().apply {
            templates.forEach { (k, v) -> addTemplate(k, v) }
        }

        PebbleEngine.Builder()
            .cacheActive(cache)
            .loader(memoryLoader)
            .maxRenderedSize(maxRenderedSize)
            .build()
            .getTemplate(name)
            .evaluate(writer, context, locale)

        return writer.toString()
    }
}
