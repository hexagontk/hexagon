package com.hexagonkt.templates.freemarker

import com.hexagonkt.ResourceNotFoundException
import com.hexagonkt.templates.TemplatePort
import freemarker.cache.TemplateLookupContext
import freemarker.cache.TemplateLookupResult
import freemarker.cache.TemplateLookupStrategy
import freemarker.cache.URLTemplateLoader
import freemarker.template.Configuration
import freemarker.template.Version
import java.io.StringWriter
import java.net.URL
import java.util.*

object FreeMarkerAdapter : TemplatePort {

    override fun render(url: URL, context: Map<String, *>, locale: Locale): String {
        val configuration = Configuration(Version("2.3.31")).apply {
            defaultEncoding = "UTF-8"
            templateLookupStrategy = object : TemplateLookupStrategy() {
                override fun lookup(ctx: TemplateLookupContext): TemplateLookupResult =
                    try {
                        val templateName = ctx.templateName
                        URL(templateName)
                        ctx.lookupWithAcquisitionStrategy(templateName)
                    }
                    catch (e: ResourceNotFoundException) {
                        ctx.createNegativeLookupResult()
                    }
            }
            templateLoader = object : URLTemplateLoader() {
                override fun getURL(name: String): URL =
                    URL(name)
            }
        }

        val template = configuration.getTemplate(url.toString())
        val writer = StringWriter()

        return writer.use {
            val env = template.createProcessingEnvironment(context, it)
            env.locale = locale
            env.process()
            it.buffer.toString()
        }
    }
}
