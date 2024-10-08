package com.hexagonkt.args.formatter

import com.hexagonkt.args.Command
import com.hexagonkt.args.Property

/**
 * [summary] name, title and description
 * [definition] Usage
 * [detail] Commands, options and parameters
 *
 * @property propertyFormatter
 */
data class CommandFormatter(
    val indent: String = "  ",
    val propertyFormatter: Formatter<Property<*>> = PropertyFormatter()
) : Formatter<Command> {

    override fun summary(component: Command): String {
        val title = listOfNotNull(
            component.name,
            component.title?.let { "- $it" },
        )
        .joinToString(" ")

        return "$title\n\n${component.description ?: ""}".trim()
    }

    override fun definition(component: Command): String {
        val options = component.properties.joinToString(" ") { propertyFormatter.summary(it) }
        val componentSubcommands = component.subcommands
        val subcommands =
            if (componentSubcommands.isEmpty()) ""
            else componentSubcommands.joinToString("|", " ") { it.name }

        return "${component.name} $options$subcommands"
    }

    override fun detail(component: Command): String {
        val commands = component.subcommands.dl("COMMANDS") { it.name to (it.title ?: "") }
        val parameters = component.parameters.dl("PARAMETERS")
        val options = component.options.dl("OPTIONS")
        val flags = component.flags.dl("FLAGS")

        return "$commands$parameters$options$flags".trim()
    }

    private fun <T : Any> Collection<T>.dl(
        title: String, block: (T) -> Pair<String, String>
    ) : String {

        if (isEmpty())
            return ""

        return map(block)
            .let { p ->
                val m = p.maxOf { it.first.length }
                p.map { (k, v) -> k.padEnd(m + 3, ' ') + v }
            }
            .joinToString("\n", "$title\n", "\n\n") { it.trim().prependIndent(indent) }
    }

    private fun Collection<Property<*>>.dl(title: String) : String =
        dl(title) { propertyFormatter.definition(it) to propertyFormatter.detail(it) }
}
