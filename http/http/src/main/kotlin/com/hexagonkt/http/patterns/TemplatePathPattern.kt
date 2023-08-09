package com.hexagonkt.http.patterns

/**
 * A path definition. It parses path patterns and extract values for parameters.
 *
 * No splat support (you can use named parameters though).
 *
 * Delimiter is {var} to conform with [RFC 6570](https://tools.ietf.org/html/rfc6570).
 *
 * It supports the {var:regex} format to match only parameters with a specific pattern.
 */
data class TemplatePathPattern(
    override val pattern: String,
    override val prefix: Boolean = false
) : PathPattern by RegexPathPattern(Regex(patternToRegex(pattern, prefix))) {

    internal companion object {
        private const val PARAMETER_PREFIX = "{"
        private const val PARAMETER_SUFFIX = "}"

        internal const val WILDCARD = "*"
        private const val PARAMETER = "\\$PARAMETER_PREFIX(\\w+)(:.+?)?$PARAMETER_SUFFIX"

        private val REGEX_CHARACTERS = listOf('(', ')', '|', '?', '+', '[', ']')

        const val VARIABLE_PATTERN = "[^/]+"
        val WILDCARD_REGEX = Regex("\\$WILDCARD")
        val PARAMETER_REGEX = Regex(PARAMETER)
        val PLACEHOLDER_REGEX = Regex("\\$WILDCARD|$PARAMETER")

        fun isTemplate(pattern: String): Boolean =
            REGEX_CHARACTERS.any { pattern.contains(it) } || PLACEHOLDER_REGEX in pattern

        fun patternToRegex(pattern: String, prefix: Boolean): String {
            return pattern
                .replace(WILDCARD, "(.*?)")
                .replaceParameters(parameters(pattern))
                .let { if (prefix) it else "$it$" }
        }

        private fun parameters(pattern: String): Map<String, String> =
            PARAMETER_REGEX.findAll(pattern)
                .map {
                    val (_, k, re) = it.groupValues
                    k to re.ifEmpty { VARIABLE_PATTERN }
                }
                .toMap()

        private fun String.replaceParameters(parameters: Map<String, String>): String =
            parameters
                .entries
                .fold(this) { accumulator, (k, v) ->
                    val re = if (v == VARIABLE_PATTERN) "" else v
                    val search = "$PARAMETER_PREFIX$k$re$PARAMETER_SUFFIX"
                    val replacement = "(?<$k>${v.removePrefix(":")}?)"
                    accumulator.replace(search, replacement)
                }
    }

    val parameters: List<String> = parameters(pattern).keys.toList()

    init {
        checkPathPatternPrefix(pattern, listOf("*"))
    }

    override fun insertParameters(parameters: Map<String, Any>): String {
        val keys = parameters.keys
        val patternParameters = this.parameters

        require(keys.toSet() == patternParameters.toSet()) {
            "Parameters must match pattern's parameters($patternParameters). Provided: $keys"
        }

        return parameters.entries.fold(pattern) { accumulator, (k, v) ->
            val re = Regex("\\$PARAMETER_PREFIX$k(:.+?)?$PARAMETER_SUFFIX")
            accumulator.replace(re, v.toString())
        }
    }
}
