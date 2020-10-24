package com.hexagonkt.templates

import com.hexagonkt.helpers.toDate
import com.hexagonkt.injection.InjectionManager
import com.hexagonkt.serialization.parse
import java.net.URL
import java.time.LocalDateTime
import java.util.Locale

/**
 * A TemplateEngine can be used to render templates
 */
class TemplateEngine(
    private val adapter: TemplatePort = InjectionManager.inject(),
    private val settings: TemplateEngineSettings = TemplateEngineSettings()
) {

    private var parametersCache: Map<String, Map<String, Any?>> = mapOf()

    constructor(settings: TemplateEngineSettings = TemplateEngineSettings()) : this(
        InjectionManager.inject(),
        settings
    )

    fun render(resource: String, locale: Locale, context: Map<String, *>): String =
        adapter.render(resourcePath(resource), locale, context(resource, locale, context), settings)

    private fun resourcePath(resource: String) =
        settings.basePath?.let { "$it/$resource" } ?: resource

    fun render(resource: String, locale: Locale, vararg context: Pair<String, *>): String =
        render(resource, locale, linkedMapOf(*context))

    private fun loadProps(path: String): Map<String, Any> =
        try {
            URL("classpath:${settings.basePath}/$path.yml").parse()
        }
        catch (e: Exception) {
            mapOf()
        }

    @Suppress("UNCHECKED_CAST")
    private fun loadBundle(path: String, locale: Locale): Map<String, *> = loadProps(path).let {
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
}
