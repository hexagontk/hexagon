package com.hexagonkt.templates.rocker

import com.fizzed.rocker.Rocker
import com.fizzed.rocker.RockerModel
import com.hexagonkt.templates.TemplateEngine
import java.util.*

object RockerEngine : TemplateEngine {
    override fun render(resource: String, locale: Locale, context: Map<String, *>): String {
        val bindableRockerModel = Rocker.template(resource)

        // filter the context to only include properties declared via
        // `@args` in the template; rocker throws TemplateBindException
        // if any undeclared args are passed
        val modelArgs = getModelAgumentNames(bindableRockerModel.model)
        val contextEntries = context.filterKeys { modelArgs.contains(it) }

        return bindableRockerModel
            .bind(contextEntries)
            .render().toString()
    }

    // based on Rocker.getModelArgumentNames() without wrapping generated exceptions
    private fun getModelAgumentNames(model: RockerModel): Array<*> =
        model.javaClass.getField("ARGUMENT_NAMES").get(null) as Array<*>
}
