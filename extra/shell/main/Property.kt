package com.hexagontk.shell

import com.hexagontk.core.text.parsedClasses
import kotlin.reflect.KClass

/**
 * TODO
 *
 * Properties passed by the command line.
 *
 * @param T
 */
sealed interface Property<T : Any> {
    val type: KClass<T>
    val names: Set<String>
    val description: String?
    val regex: Regex?
    val optional: Boolean
    val multiple: Boolean
    /**
     * TODO To group properties (for listing or alternatives). I.e.:
     *  '|alternate|' for grouping options that are exclusive or 'Common Options' to group in help
     */
    val tag: String?
    val values: List<T>

    fun addValues(value: Property<*>): Property<T>

    fun addValue(value: String): Property<T>

    fun clearValues(): Property<T>

    fun typeText(): String =
        type.simpleName ?: error("Unknown type name")

    fun check(component: String, namePattern: Regex) {
        require(type in parsedClasses) {
            val types = parsedClasses.map(KClass<*>::simpleName)
            "Type ${type.simpleName} not in allowed types: $types"
        }
        require(names.isNotEmpty()) { "$component must have a name" }
        require(names.all { it.matches(namePattern) }) {
            "Names must comply with ${namePattern.pattern} regex: $names"
        }
        require(regex == null || type == String::class) {
            "$component regex can only be used for 'string' type: ${type.simpleName}"
        }

        require(description?.isNotBlank() ?: true) { "$component description can not be blank" }

        if (regex != null)
            values.forEach {
                require(regex?.matches(it as String) ?: true) {
                    "Value should match the '${regex?.pattern}' regex: $it"
                }
            }

        if (!multiple)
            require(values.size <= 1) {
                "$component '${names.first()}' can only have one value: $values"
            }
    }

    companion object {
        val VERSION: Flag = Flag('v', "version", "Show the program's version along its description")
        val HELP: Flag = Flag('h', "help", "Display detailed information on running this command")
    }
}
