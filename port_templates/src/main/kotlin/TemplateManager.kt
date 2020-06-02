package com.hexagonkt.templates

import com.hexagonkt.helpers.Resource
import com.hexagonkt.helpers.toDate
import com.hexagonkt.serialization.parse
import java.time.LocalDateTime
import java.util.*

object TemplateManager {
    private var basePath = "templates"

    private var parametersCache: Map<String, Map<String, Any?>> = mapOf()

    private fun loadProps (path: String): Map<String, Any> =
        try {
            Resource("$basePath/$path.yaml").url()?.parse () ?: mapOf()
        }
        catch (e: Exception) {
            mapOf()
        }

    @Suppress("UNCHECKED_CAST")
    private fun loadBundle (path: String, locale: Locale): Map<String, *> = loadProps(path).let {
        (it[locale.language] ?: if (it.isNotEmpty()) it.entries.first().value else it)
            as Map<String, *> + (it["data"] ?: mapOf<String, Any>()) as Map<String, *>
    }

    private fun context(resource: String, locale: Locale, context: Map<String, *>): Map<String, *> {
        val bundlePath = resource.substringBeforeLast('.')

        val key = locale.country + locale.language + bundlePath
        if (!parametersCache.containsKey(key)) {
            val commonBundle = loadBundle("common", locale)
            val templateBundle = loadBundle(bundlePath, locale)
            parametersCache = parametersCache + (key to commonBundle + templateBundle)
        }

        val parameters: Map<String, Any?> = parametersCache[key] ?: emptyMap()

        val now = LocalDateTime.now().toDate()
        val defaultProperties = mapOf("_template_" to resource, "_now_" to now)
        return parameters + context + defaultProperties
    }

    fun render(
        engine: TemplatePort, resource: String, locale: Locale, context: Map<String, *>): String =
            engine.render("$basePath/$resource", locale, context(resource, locale, context))
}
