package com.hexagontk.http.patterns

import com.hexagontk.core.text.filter
import com.hexagontk.http.patterns.TemplatePathPattern.Companion.VARIABLE_PATTERN
import com.hexagontk.http.patterns.TemplatePathPattern.Companion.patternToRegex

class RegexPathPattern(private val regex: Regex) : PathPattern {

    override val pattern: String = regex.pattern
    override val prefix: Boolean = !regex.pattern.endsWith("$")

    internal companion object {
        const val PARAMETER_PREFIX = "(?<"
        const val PARAMETER_SUFFIX = ">$VARIABLE_PATTERN?)"
        val PARAMETER_REGEX = Regex("""\(\?<\w+>""")
    }

    init {
        checkPathPatternPrefix(pattern, listOf("(.*?)", "$"))
    }

    val parameters: List<String> =
        PARAMETER_REGEX.findAll(pattern)
            .map { it.value.removePrefix("(?<").removeSuffix(">") }
            .toList()

    override fun addPrefix(prefix: String?): PathPattern =
        if (prefix == null) this
        else RegexPathPattern(Regex(patternToRegex(prefix, true) + pattern))

    override fun matches(requestUri: String): Boolean =
        if (prefix) regex.matchesAt(requestUri, 0)
        else regex.matches(requestUri)

    override fun extractParameters(requestUri: String): Map<String, String> {
        val result = regex.matchEntire(requestUri)
        require(result != null) { "URI '$requestUri' does not match path" }

        val allValues = result.groupValues
            .drop(1)
            .mapIndexed { ii, v -> ii.toString() to v }
            .toMap()

        val resultGroups = result.groups as MatchNamedGroupCollection
        return if (parameters.isEmpty()) allValues
        else parameters.associateWith { resultGroups[it]?.value ?: "" } + allValues
    }

    override fun insertParameters(parameters: Map<String, Any>): String {
        val keys = parameters.keys
        val patternParameters = this.parameters

        require(keys.toSet() == patternParameters.toSet()) {
            "Parameters must match pattern's parameters($patternParameters). Provided: $keys"
        }

        return pattern.filter(PARAMETER_PREFIX, PARAMETER_SUFFIX, parameters)
    }
}
