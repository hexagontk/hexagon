package com.hexagonkt.templates.freemarker

import com.hexagonkt.templates.TemplateEngineSettings
import com.hexagonkt.templates.TemplatePort
import freemarker.cache.TemplateLoader
import freemarker.template.Configuration
import freemarker.template.Version
import java.io.Reader
import java.io.StringWriter
import java.util.*

object FreeMarkerAdapter : TemplatePort {

    override fun render(
        resource: String,
        locale: Locale,
        context: Map<String, *>,
        settings: TemplateEngineSettings
    ): String {
        val template = config(settings).getTemplate(resource)
        val writer = StringWriter()

        return writer.use {
            val env = template.createProcessingEnvironment(context, it)
            env.locale = locale
            env.process()
            it.buffer.toString()
        }
    }

    private fun config(templateEngineSettings: TemplateEngineSettings) =
        Configuration(Version("2.3.30")).apply {
            setClassLoaderForTemplateLoading(Thread.currentThread().contextClassLoader, "/")
            defaultEncoding = "UTF-8"
            templateEngineSettings.loader?.let { it ->
                templateLoader = LambdaTemplateLoader(it)
            }
        }
}

private class LambdaTemplateLoader(private val loadTemplate: (String) -> Reader?) : TemplateLoader {
    override fun findTemplateSource(name: String): Reader? = loadTemplate(name)

    override fun getLastModified(templateSource: Any?): Long = -1

    override fun getReader(templateSource: Any?, encoding: String?): Reader =
        templateSource as Reader

    override fun closeTemplateSource(templateSource: Any?) {
        (templateSource as? Reader)?.close()
    }
}
