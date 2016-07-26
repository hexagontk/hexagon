package co.there4.hexagon.template

import co.there4.hexagon.serialization.parse
import com.mitchellbosecke.pebble.PebbleEngine
import java.lang.ClassLoader.getSystemResourceAsStream as resourceAsStream
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
object PebbleRenderer {
    val basePath = "templates"
    val engine = PebbleEngine.Builder().build()
    val global = loadProps ("global")

    private fun loadProps (path: String) = resourceAsStream("$basePath/$path.yaml").let {
        @Suppress("UNCHECKED_CAST")
        if (it != null) it.parse (Map::class, "application/yaml") as Map<String, Any>
        else mapOf<String, Any>()
    }

    fun render (template: String, locale: Locale, context: Map<String, *>): String {
        @Suppress("UNCHECKED_CAST")
        fun loadBundle (path: String): Map<String, *> = loadProps(path).let {
            (it[locale.language] ?: if (it.size > 0) it.entries.first().value else it)
                as Map<String, *> + (it["data"] ?: mapOf<String, Any>()) as Map<String, *>
        }

        val compiledTemplate = engine.getTemplate("$basePath/$template")
        val bundlePath = template.substring(0, template.lastIndexOf('.'))

        val texts = loadBundle (bundlePath)
        val common = loadBundle ("common")

        val writer = StringWriter()
        val now = LocalDateTime.now()
        val defaultProperties = mapOf(
            "_template_" to template,
            "_year_" to now.year,
            "_month_" to now.monthValue,
            "_day_" to now.dayOfMonth,
            "_hour_" to now.hour,
            "_minutes_" to now.minute
        )
        val context = global + common + texts + context + defaultProperties
        compiledTemplate.evaluate(writer, context, locale)
        return writer.toString()
    }
}
