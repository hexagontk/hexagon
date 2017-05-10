package co.there4.hexagon.template

import co.there4.hexagon.serialization.parse
import co.there4.hexagon.settings.SettingsManager
import co.there4.hexagon.helpers.resourceAsStream
import co.there4.hexagon.helpers.toDate
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
internal object PebbleRenderer {
    val basePath = "templates"
    val engine: PebbleEngine = PebbleEngine.Builder().build() ?: error("Error setting up Pebble")
    val settings = SettingsManager.settings

    var parametersCache: Map<String, Map<String, Any?>> = mapOf()

    private fun loadProps (path: String) =
        resourceAsStream("$basePath/$path.yaml")?.parse ("application/yaml") ?: mapOf<String, Any>()

    fun render (template: String, locale: Locale, context: Map<String, *>): String {
        @Suppress("UNCHECKED_CAST")
        fun loadBundle (path: String): Map<String, *> = loadProps(path).let {
            (it[locale.language] ?: if (it.isNotEmpty()) it.entries.first().value else it)
                as Map<String, *> + (it["data"] ?: mapOf<String, Any>()) as Map<String, *>
        }

        val compiledTemplate = engine.getTemplate("$basePath/$template")
        val bundlePath = template.substringBeforeLast('.')

        val key = locale.country + locale.language + bundlePath
        if (!parametersCache.containsKey(key))
            parametersCache += (key to loadBundle ("common") + loadBundle (bundlePath))

        val parameters: Map<String, Any?> = parametersCache[key] ?: emptyMap()

        val writer = StringWriter()
        val now = LocalDateTime.now().toDate()
        val defaultProperties = mapOf("_template_" to template, "_now_" to now)
        val completeContext = settings + parameters + context + defaultProperties
        val contextEntries = completeContext.map {
            it.key to
                if (it.value is LocalDateTime) (it.value as LocalDateTime).toDate()
                else it.value
        }
        compiledTemplate.evaluate(writer, contextEntries.toMap(), locale)
        return writer.toString()
    }
}
