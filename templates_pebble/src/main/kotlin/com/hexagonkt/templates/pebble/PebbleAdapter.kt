package com.hexagonkt.templates.pebble

import com.hexagonkt.helpers.toDate
import com.hexagonkt.templates.TemplatePort
import com.mitchellbosecke.pebble.PebbleEngine
import java.io.StringWriter
import java.time.LocalDateTime
import java.util.*

/**
 * TODO Support different engines by subdir. Ie:
 * TODO Add code to test templates (check unresolved variables in bundles, multilanguage, etc.)
 *
 * templates/pebble/file
 * templates/freemarker/file
 * ...
 */
object PebbleAdapter : TemplatePort {
    private val engine: PebbleEngine = PebbleEngine.Builder().cacheActive(true).build()

    override fun render(resource: String, locale: Locale, context: Map<String, *>): String {
        val contextEntries = context.map {
            it.key to
                if (it.value is LocalDateTime) (it.value as LocalDateTime).toDate()
                else it.value
        }

        val writer = StringWriter()
        engine.getTemplate(resource).evaluate(writer, contextEntries.toMap(), locale)
        return writer.toString()
    }
}
