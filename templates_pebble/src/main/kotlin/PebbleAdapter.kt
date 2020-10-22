package com.hexagonkt.templates.pebble

import com.hexagonkt.helpers.toDate
import com.hexagonkt.templates.TemplateEngineSettings
import com.hexagonkt.templates.TemplatePort
import com.mitchellbosecke.pebble.PebbleEngine
import com.mitchellbosecke.pebble.loader.Loader
import java.io.Reader
import java.io.StringWriter
import java.time.LocalDateTime
import java.util.*

object PebbleAdapter : TemplatePort {

    private fun engine(settings: TemplateEngineSettings): PebbleEngine =
        PebbleEngine.Builder().apply {
            cacheActive(true)
            settings.loader?.let { loader(LambdaLoader(it)) }
        }.build()

    override fun render(
        resource: String,
        locale: Locale,
        context: Map<String, *>,
        settings: TemplateEngineSettings
    ): String {
        val contextEntries = context.map {
            it.key to
                if (it.value is LocalDateTime) (it.value as LocalDateTime).toDate()
                else it.value
        }

        val writer = StringWriter()
        engine(settings).getTemplate(resource).evaluate(writer, contextEntries.toMap(), locale)
        return writer.toString()
    }
}

private class LambdaLoader(private val loadTemplate: (resource: String) -> Reader?) :
    Loader<String> {
    override fun getReader(cacheKey: String): Reader? = loadTemplate(cacheKey)!!

    override fun resourceExists(templateName: String): Boolean {
        val reader = loadTemplate(templateName)
        reader?.close()
        return reader != null
    }

    override fun setCharset(charset: String) {}

    override fun setPrefix(prefix: String) {}

    override fun setSuffix(suffix: String) {}

    override fun resolveRelativePath(relativePath: String, anchorPath: String): String? = null

    override fun createCacheKey(templateName: String): String = templateName
}
