package co.there4.hexagon.templates

import co.there4.hexagon.helpers.resourceAsStream
import co.there4.hexagon.helpers.toDate
import co.there4.hexagon.serialization.parse
import java.time.LocalDateTime
import java.util.*

object TemplateManager {
    var basePath = "templates"

    private var parametersCache: Map<String, Map<String, Any?>> = mapOf()

    private fun loadProps (path: String) =
        resourceAsStream("$basePath/$path.yaml")?.parse ("application/yaml") ?: mapOf<String, Any>()

    @Suppress("UNCHECKED_CAST")
    private fun loadBundle (path: String, locale: Locale): Map<String, *> = loadProps(path).let {
        (it[locale.language] ?: if (it.isNotEmpty()) it.entries.first().value else it)
            as Map<String, *> + (it["data"] ?: mapOf<String, Any>()) as Map<String, *>
    }

    private fun context(resource: String, locale: Locale, context: Map<String, *>): Map<String, *> {
        val bundlePath = resource.substringBefore('.')

        val key = locale.country + locale.language + bundlePath
        if (!parametersCache.containsKey(key)) {
            val commonBundle = loadBundle("common", locale)
            val templateBundle = loadBundle(bundlePath, locale)
            parametersCache += (key to commonBundle + templateBundle)
        }

        val parameters: Map<String, Any?> = parametersCache[key] ?: emptyMap()

        val now = LocalDateTime.now().toDate()
        val defaultProperties = mapOf("_template_" to resource, "_now_" to now)
        return parameters + context + defaultProperties
    }

    fun render(
        engine: TemplateEngine, resource: String, locale: Locale, context: Map<String, *>): String =
            engine.render("$basePath/$resource", locale, context(resource, locale, context))
}
