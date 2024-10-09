package com.hexagontk.args.formatter

import com.hexagontk.args.Flag
import com.hexagontk.args.Option
import com.hexagontk.args.Parameter
import com.hexagontk.args.Property
import com.hexagontk.helpers.text.camelToSnake

data class PropertyFormatter(
    val namesSeparator: String = ", ",
    val expandRegex: Boolean = false,
    val fieldSeparator: String = ". ",
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
