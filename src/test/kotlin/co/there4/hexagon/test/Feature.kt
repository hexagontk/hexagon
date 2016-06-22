package co.there4.hexagon.test

import co.there4.hexagon.util.EOL

fun StringBuilder.appendLn(line: String = "") = this.append(line).append(EOL)

abstract class Feature {
    protected open fun tags(): List<String> = listOf ()
    protected open fun narrative(): String? = null

    init {
        val builder = StringBuilder()

        val tags = tags()
        if (tags.isNotEmpty())
            builder.appendLn (tags.map { "@$it" }.joinToString(" "))

        builder.appendLn("Feature:")

        val description = narrative()
        if (description != null)
            builder.appendLn(description.trimIndent().prependIndent("  "))

        println (builder)
    }
}

class Scenario (
    val tags: List<String>,
    val description: String? = null,
    val spec: () -> Unit = {}) {

    constructor(description: String?, spec: () -> Unit = {}) : this (listOf(), description, spec)

    init {
        val builder = StringBuilder()
        builder.appendLn()

        if (tags.isNotEmpty())
            builder.appendLn (tags.map { "@$it" }.joinToString(" "))

        builder.append ("Scenario:".prependIndent("  "))

        if (description != null)
            builder.appendLn (" " + description.trimIndent())
        else
            builder.appendLn ()

        spec()

        println (builder)
    }
}

class Examples (vararg val vars: Pair<String, List<*>>, val spec: (Map<String, *>) -> Unit = {}) {
    init {
        val builder = StringBuilder()

        val pairs: List<List<Pair<String, *>>> = vars.map { pair ->
            pair.second.map { pair.first to it }
        }

        val maps: List<Map<String, *>> = (0..pairs.first().size - 1).map { ii ->
            pairs.map { it[ii] }.toMap()
        }

        maps.forEach { spec (it) }

        builder.appendLn ()
        builder.appendLn ("Examples:".prependIndent("    "))

        println (builder)
    }
}

val AND = EOL + "And".padStart(9, ' ') + " "

abstract class Action (val description: List<String>, val spec: () -> Unit) {
    init {
        val builder = StringBuilder()

        val actionType = javaClass.simpleName.padStart(9, ' ')
        builder.appendLn (actionType + " " + description.joinToString(AND))
        spec()

        println (builder)
    }
}

class Given (vararg description: String, spec: () -> Unit) : Action (description.toList(), spec)
class When (vararg description: String, spec: () -> Unit) : Action (description.toList(), spec)
class Then (vararg description: String, spec: () -> Unit) : Action (description.toList(), spec)

