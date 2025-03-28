package com.hexagontk.shell.formatter

import com.hexagontk.shell.Flag
import com.hexagontk.shell.Option
import com.hexagontk.shell.Parameter
import com.hexagontk.shell.Property
import com.hexagontk.helpers.text.camelToSnake

class PropertyFormatter(
    private val namesSeparator: String = ", ",
    private val expandRegex: Boolean = false,
    private val fieldSeparator: String = ". ",
) : Formatter<Property<*>> {

    override fun summary(component: Property<*>): String =
        when (component) {
            is Option<*>, is Flag -> optionSummary(component)
            is Parameter<*> -> component.format(definition(component))
        }

    override fun definition(component: Property<*>): String =
        when (component) {
            is Option<*>, is Flag -> optionDefinition(component)
            is Parameter<*> -> "<${component.name}>"
        }

    override fun detail(component: Property<*>): String =
        component.let { c ->
            listOfNotNull(
                c.description,
                if (component is Flag) null
                else (c.regex?.pattern ?: c.typeName())?.let { "Type: " + c.format(it) },
                c.values
                    .ifEmpty { null }
                    ?.map(Any::toString)
                    ?.let { "Default: " + if (c.multiple) c.values else c.values.first() },
            )
        }
        .joinToString(fieldSeparator)

    private fun optionSummary(component: Property<*>): String =
        component.format(
            component.aliases()
                .map { if (component.hasValue()) "$it ${component.typeName()}" else it }
                .first()
        )

    private fun optionDefinition(component: Property<*>): String =
        component.aliases().joinToString(namesSeparator).let {
            if (component.hasValue()) "$it ${component.typeName()}" else it
        }

    private fun Property<*>.hasValue(): Boolean =
        type != Boolean::class

    private fun Property<*>.aliases() =
        names.map { if (it.length == 1) "-$it" else "--$it" }

    private fun Property<*>.format(text: String): String =
        when {
            optional && multiple -> "[$text]..."
            optional -> "[$text]"
            multiple -> "$text..."
            else -> text
        }

    private fun Property<*>.typeName(): String? =
        if (regex != null) if (expandRegex) "{${regex?.pattern}}" else "REGEX"
        else type.simpleName?.camelToSnake()?.uppercase()
}
