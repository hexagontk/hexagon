package com.hexagonkt.templates.rocker

import com.fizzed.rocker.Rocker
import com.fizzed.rocker.RockerModel
import com.fizzed.rocker.TemplateBindException
import com.hexagonkt.templates.TemplateEngine
import java.util.*

object RockerEngine : TemplateEngine {
    override fun render(resource: String, locale: Locale, context: Map<String, *>): String {
        val bindableRockerModel = Rocker.template(resource)

        // filter the context to only include properties declared via
        // `@args` in the template; rocker throws TemplateBindException
        // if any undeclared args are passed
        val modelArgs = getModelAgumentNames(resource, bindableRockerModel.model)
        val contextEntries = context.filterKeys { modelArgs.contains(it) }

        return bindableRockerModel
            .bind(contextEntries)
            .render().toString()
    }

    private fun getModelAgumentNames(resource: String, model: RockerModel): Array<String> {
        // based on Rocker.getModelArgumentNames()
        try {
            return model.javaClass.getField("ARGUMENT_NAMES").get(null) as Array<String>
        } catch (ex: Exception) {
            throw TemplateBindException(resource, model.javaClass.canonicalName,
                "Unable to read ARGUMENT_NAMES static field from template", ex)
        }
    }
}
