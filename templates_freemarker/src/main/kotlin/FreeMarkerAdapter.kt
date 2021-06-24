package com.hexagonkt.templates.freemarker

import com.hexagonkt.templates.TemplatePort
import freemarker.template.Configuration
import freemarker.template.Version
import java.io.StringWriter
import java.util.*

object FreeMarkerAdapter : TemplatePort {

    override fun render(resource: String, context: Map<String, *>, locale: Locale): String {
        val configuration = Configuration(Version("2.3.31")).apply {
            setClassLoaderForTemplateLoading(Thread.currentThread().contextClassLoader, "/")
            defaultEncoding = "UTF-8"
        }

        val template = configuration.getTemplate(resource)
        val writer = StringWriter()

        return writer.use {
            val env = template.createProcessingEnvironment(context, it)
            env.locale = locale
            env.process()
            it.buffer.toString()
        }
    }
}

/*
private class LambdaTemplateLoader(private val loadTemplate: (String) -> Reader?) : TemplateLoader {
    override fun findTemplateSource(name: String): Reader? = loadTemplate(name)

    override fun getLastModified(templateSource: Any?): Long = -1

    override fun getReader(templateSource: Any?, encoding: String?): Reader =
        templateSource as Reader

    override fun closeTemplateSource(templateSource: Any?) {
        (templateSource as? Reader)?.close()
    }
}
 */
