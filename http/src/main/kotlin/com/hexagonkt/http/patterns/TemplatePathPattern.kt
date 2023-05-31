package com.hexagonkt.http.patterns

/**
 * A path definition. It parses path patterns and extract values for parameters.
 *
 * No splat support (you can use named parameters though).
 *
 * Delimiter is {var} to conform with [RFC 6570](https://tools.ietf.org/html/rfc6570).
 */
data class TemplatePathPattern(
    override val pattern: String,
    override val prefix: Boolean = false
) : PathPattern by RegexPathPattern(Regex(patternToRegex(pattern, prefix))) {

    internal companion object {
        private const val PARAMETER_PREFIX = "{"
        private const val PARAMETER_SUFFIX = "}"

        internal const val WILDCARD = "*"
        private const val PARAMETER = "\\$PARAMETER_PREFIX\\w+$PARAMETER_SUFFIX"

        private val REGEX_CHARACTERS = listOf('(', ')', '|', '?', '+', '[', ']')

        const val VARIABLE_PATTERN = "[^/]+"
        val WILDCARD_REGEX = Regex("\\$WILDCARD")
        val PARAMETER_REGEX = Regex(PARAMETER)
        val PLACEHOLDER_REGEX = Regex("\\$WILDCARD|$PARAMETER")

        fun isTemplate(pattern: String): Boolean =
            REGEX_CHARACTERS.any { pattern.contains(it) } || PLACEHOLDER_REGEX in pattern

        fun patternToRegex(pattern: String, prefix: Boolean): String {
            checkPathPatternVariables(pattern)
            return pattern
                .replace(WILDCARD, "(.*?)")
                .replaceParameters(parameters(pattern))
                .let { if (prefix) it else "$it$" }
        }

        private fun parameters(pattern: String): List<String> =
            PARAMETER_REGEX.findAll(pattern)
                .map { it.value.removePrefix(PARAMETER_PREFIX).removeSuffix(PARAMETER_SUFFIX) }
                .toList()

        private fun String.replaceParameters(parameters: List<String>): String =
            parameters
                .fold(this) { accumulator, item ->
                    accumulator.replace("$PARAMETER_PREFIX$item$PARAMETER_SUFFIX", "(?<$item>$VARIABLE_PATTERN?)")
                }
    }

    init {
        checkPathPatternPrefix(pattern, listOf("*"))
    }
}
