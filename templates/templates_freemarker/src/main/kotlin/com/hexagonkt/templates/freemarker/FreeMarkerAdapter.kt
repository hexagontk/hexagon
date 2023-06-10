package com.hexagonkt.templates.freemarker

import com.hexagonkt.core.ResourceNotFoundException
import com.hexagonkt.templates.TemplatePort
import freemarker.cache.StringTemplateLoader
import freemarker.cache.TemplateLookupContext
import freemarker.cache.TemplateLookupResult
import freemarker.cache.TemplateLookupStrategy
import freemarker.cache.URLTemplateLoader
import freemarker.template.Configuration
import freemarker.template.Template
import freemarker.template.Version
import java.io.StringWriter
import java.net.URL
import java.util.*

class FreeMarkerAdapter : TemplatePort {

    private val freeMarkerVersion = Version("2.3.32")

    object AdapterTemplateLookupStrategy : TemplateLookupStrategy() {
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

    object AdapterTemplateLoader : URLTemplateLoader() {
        override fun getURL(name: String): URL =
            URL(name)
    }

    override fun render(url: URL, context: Map<String, *>, locale: Locale): String {
        val configuration = Configuration(freeMarkerVersion).apply {
            defaultEncoding = "UTF-8"
            templateLookupStrategy = AdapterTemplateLookupStrategy
            templateLoader = AdapterTemplateLoader
        }

        val template = configuration.getTemplate(url.toString())
        return processTemplate(template, context, locale)
    }

    override fun render(
        name: String, templates: Map<String, String>, context: Map<String, *>, locale: Locale
    ): String {
        val configuration = Configuration(freeMarkerVersion).apply {
            defaultEncoding = "UTF-8"
            templateLoader = StringTemplateLoader().apply {
                templates.forEach { (k, v) -> putTemplate(k, v) }
            }
        }

        val template = configuration.getTemplate(name)
        return processTemplate(template, context, locale)
    }

    private fun processTemplate(
        template: Template,
        context: Map<String, *>,
        locale: Locale
    ): String =
        StringWriter().use {
            val templateUrl = context["_template_"]
            val freemarkerContext = context + ("_template_" to templateUrl.toString())
            val env = template.createProcessingEnvironment(freemarkerContext, it)
            env.locale = locale
            env.process()
            it.buffer.toString()
        }
}
