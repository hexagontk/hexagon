package com.hexagontk.shell

import com.hexagontk.helpers.requireNotBlank

/**
 * A program can have multiple commands with their own set of options and positional parameters.
 *
 * TODO Support aliases
 */
class Command(
    val name: String,
    val title: String? = null,
    val description: String? = null,
    val properties: Set<Property<*>> = emptySet(),
    val subcommands: Set<Command> = emptySet(),
) {
    val flags: Set<Flag> =
        properties.filterIsInstance<Flag>().toSet()

    val options: Set<Option<*>> =
        properties.filterIsInstance<Option<*>>().toSet()

    val parameters: Set<Parameter<*>> =
        properties.filterIsInstance<Parameter<*>>().toSet()

    val propertiesMap: Map<String, Property<*>> =
        properties
            .flatMap { p -> p.names.map { it to p } }
            .toMap()

    val optionsMap: Map<String, Option<*>> =
        propertiesMap
            .filterValues { it is Option<*> }
            .mapValues { it.value as Option<*> }

    val parametersMap: Map<String, Parameter<*>> =
        propertiesMap
            .filterValues { it is Parameter<*> }
            .mapValues { it.value as Parameter<*> }

    val subcommandsMap: Map<String, Command> =
        nestedSubcommands().associateBy { it.name }

    private val emptyPropertiesMap: Map<String, Property<*>> =
        propertiesMap.mapValues { (_, v) -> v.clearValues() }

    private val emptyParametersList: List<Parameter<*>> by lazy {
        parameters.map { it.clearValues() }
    }

    init {
        requireNotBlank(Command::name)
        requireNotBlank(Command::title)
        requireNotBlank(Command::description)

        if (parametersMap.isNotEmpty()) {
            val parameters = parametersMap.values.reversed().drop(1)
            require(parameters.all { !it.multiple }) {
                "Only the last positional parameter can be multiple"
            }
        }
    }

    fun findCommand(args: Iterable<String>): Command {
        val line = args.joinToString(" ")
        return subcommandsMap
            .mapKeys { it.key.removePrefix("$name ") }
            .entries
            .sortedByDescending { it.key.count { c -> c == ' ' } }
            .find { line.contains(it.key) }
            ?.let { (k, v) -> Command(k, v.title, v.description, v.properties, v.subcommands) }
            ?: this
    }

    fun parse(args: List<String>): Command {
        val argsIterator = args.iterator()
        var parsedProperties = emptyList<Property<*>>()
        var parsedParameter = 0

        argsIterator.forEach { value ->
            parsedProperties = when {
                value.startsWith("--") ->
                    parsedProperties + parseOption(value.removePrefix("--"), argsIterator)

                value.startsWith('-') ->
                    parsedProperties + parseOptions(value.removePrefix("-"), argsIterator)

                else ->
                    parsedProperties + parseParameter(value, ++parsedParameter)
            }
        }

        val groupedProperties = addDefaultProperties(parsedProperties.groupValues())
        checkMandatoryProperties(groupedProperties)
        return Command(name, title, description, groupedProperties.toSet(), subcommands)
    }

    private fun addDefaultProperties(groupedProperties: List<Property<*>>): List<Property<*>> =
        groupedProperties + properties
            .filter { it.optional && it.values.isNotEmpty() }
            .filterNot { it.names.any { n -> n in groupedProperties.flatMap { gp -> gp.names } } }

    @Suppress("UNCHECKED_CAST") // Types checked at runtime
    fun <T : Any> propertyValues(name: String): List<T> =
        propertiesMap[name]?.values?.mapNotNull { it as? T } ?: emptyList()

    fun <T : Any> propertyValueOrNull(name: String): T? =
        propertyValues<T>(name).firstOrNull()

    fun <T : Any> propertyValue(name: String): T {
        return propertyValueOrNull(name) ?: error("Property '$name' does not have a value")
    }

    private fun checkMandatoryProperties(parsedProperties: List<Property<*>>) {
        val mandatoryProperties = properties.filterNot { it.optional }
        val names = parsedProperties.flatMap { it.names }
        val missingProperties = mandatoryProperties.filterNot { it.names.any { n -> n in names } }
        check(missingProperties.isEmpty()) {
            val missingNames = missingProperties.joinToString(", ") { "'${it.names.first()}'" }
            "Missing properties: $missingNames"
        }
    }

    private fun List<Property<*>>.groupValues(): List<Property<*>> =
        groupBy { it.names }
            .map { (_, v) ->
                v.reduceIndexed { i, a, b ->
                    if (a.multiple) a.addValues(b)
                    else error("Unknown argument at position ${i + 1}: ${b.values.first()}")
                }
            }

    private fun parseParameter(value: String, parsedParameter: Int): Property<*> =
        (emptyParametersList.getOrNull(parsedParameter) ?: emptyParametersList.lastOrNull())
            ?.addValue(value)
            ?: error("No parameters")

    private fun parseOptions(
        names: String, argsIterator: Iterator<String>
    ): Collection<Property<*>> {
        val namesIterator = names.iterator()
        var result = emptyList<Property<*>>()

        namesIterator.forEach {
            val name = it.toString()
            val isOption = optionsMap.contains(name)
            val option = if (isOption && namesIterator.hasNext()) {
                val firstValueChar = namesIterator.next()
                val valueStart = if (firstValueChar != '=') "=$firstValueChar" else firstValueChar
                val buffer = StringBuffer(name + valueStart)

                namesIterator.forEachRemaining(buffer::append)
                buffer.toString()
            }
            else name

            result = result + parseOption(option, argsIterator)
        }

        return result
    }

    private fun parseOption(option: String, propertiesIterator: Iterator<String>): Property<*> {
        val nameValue = option.split('=', limit = 2)
        val name = nameValue.first()
        val property = emptyPropertiesMap[name] ?: error("Option '$name' not found")
        val value =
            if (property is Option<*>) nameValue.getOrNull(1) ?: propertiesIterator.next()
            else "true"

        return property.addValue(value)
    }

    private fun nestedSubcommands(): Set<Command> =
        subcommands
            .map {
                Command("$name ${it.name}", it.title, it.description, it.properties, it.subcommands)
            }
            .let { c -> c + c.flatMap { it.nestedSubcommands() } }
            .toSet()

    fun contains(flag: Flag, args: Iterable<String>): Boolean =
        flags
            .flatMap { it.names }
            .any { it in flag.names }
            && args
                .map { it.dropWhile { c -> c == '-' } }
                .any { it in flag.names }

    // TODO Only used in tests
    fun copy(
        name: String = this.name,
        title: String? = this.title,
        description: String? = this.description,
        properties: Set<Property<*>> = this.properties,
        subcommands: Set<Command> = this.subcommands,
    ): Command =
        Command(name, title, description, properties, subcommands)

    // TODO Only used in tests
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Command

        if (name != other.name) return false
        if (title != other.title) return false
        if (description != other.description) return false
        if (properties != other.properties) return false
        if (subcommands != other.subcommands) return false

        return true
    }

    // TODO Only used in tests
    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + properties.hashCode()
        result = 31 * result + subcommands.hashCode()
        return result
    }
}
