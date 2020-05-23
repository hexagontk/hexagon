package com.hexagonkt.templates.freemarker

import com.hexagonkt.templates.TemplatePort
import freemarker.template.Configuration
import freemarker.template.Version
import java.io.StringWriter
import java.util.*

object FreemarkerAdapter : TemplatePort {

    private val config = Configuration(Version("2.3.30"))

    init {
        config.setClassLoaderForTemplateLoading(Thread.currentThread().contextClassLoader, "/")
        config.defaultEncoding = "UTF-8"
    }

    override fun render(resource: String, locale: Locale, context: Map<String, *>): String {
        val template = config.getTemplate(resource)
        val writer = StringWriter()

        return writer.use {
            val env = template.createProcessingEnvironment(context, it)
            env.locale = locale
            env.process()
            it.buffer.toString()
        }
    }

}
