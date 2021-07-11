package com.hexagonkt.templates.pebble

import com.hexagonkt.helpers.toDate
import com.hexagonkt.templates.TemplatePort
import com.mitchellbosecke.pebble.PebbleEngine
import java.io.StringWriter
import java.net.URL
import java.time.LocalDateTime
import java.util.*

object PebbleAdapter : TemplatePort {

    private val engine: PebbleEngine = PebbleEngine.Builder().cacheActive(true).build()

    override fun render(url: URL, context: Map<String, *>, locale: Locale): String {
        val contextEntries = context.map {
            it.key to
                if (it.value is LocalDateTime) (it.value as LocalDateTime).toDate()
                else it.value
        }

        val writer = StringWriter()
        val resource = url.file
        engine.getTemplate(resource).evaluate(writer, contextEntries.toMap(), locale)
        return writer.toString()
    }

    // TODO Add UrlLoader as Pebble's ClasspathLoader
}
