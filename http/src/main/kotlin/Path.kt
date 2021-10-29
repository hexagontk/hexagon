package com.hexagonkt.http

import com.hexagonkt.core.helpers.findGroups

/**
 * A path definition. It parses path patterns and extract values for parameters.
 *
 * Differences with Sinatra:
 *
 *   * No splats (you can use named parameters though)
 *   * Delimiter is {var} to conform with [RFC 6570](https://tools.ietf.org/html/rfc6570)
 */
data class Path(val pattern: String) {
    private companion object {
        const val PARAMETER_PREFIX = "{"
        const val PARAMETER_SUFFIX = "}"

        const val WILDCARD = "*"

        val WILDCARD_REGEX = Regex("\\$WILDCARD")
        val PARAMETER_REGEX = Regex("\\$PARAMETER_PREFIX\\w+$PARAMETER_SUFFIX")
        val PLACEHOLDER_REGEX = Regex("\\$WILDCARD|\\$PARAMETER_PREFIX\\w+$PARAMETER_SUFFIX")
    }

    init {
        val validPrefix = pattern.startsWith("/") || pattern.startsWith("*")
        require(validPrefix) { "'$pattern' must start with '/' or '*'" }
        val validVariables = !pattern.contains(":")
        require(validVariables) { "Variables have {var} format. Path cannot have ':' $pattern" }
    }

    val hasWildcards by lazy { WILDCARD_REGEX in pattern }
    val hasParameters by lazy { PARAMETER_REGEX in pattern }

    val parameterIndex: List<String> by lazy {
        if (hasParameters)
            PLACEHOLDER_REGEX.findAll(pattern)
                .map {
                    if (it.value == WILDCARD) ""
                    else it.value.removePrefix(PARAMETER_PREFIX).removeSuffix(PARAMETER_SUFFIX)
                }
                .toList()
        else
            emptyList()
    }

    val regex: Regex? by lazy {
        pattern
            .replace("(", "(?:")
            .let {
                when (Pair(hasWildcards, hasParameters)) {
                    Pair(true, true) ->
                        it.replace(WILDCARD, "(.*?)").replace(PARAMETER_REGEX, "(.+?)")
                    Pair(true, false) ->
                        it.replace(WILDCARD, "(.*?)")
                    Pair(false, true) ->
                        it.replace(PARAMETER_REGEX, "(.*?)")
                    else -> null
                }
            }
            ?.let { Regex("$it$") }
    }

    val segments by lazy { pattern.split(PLACEHOLDER_REGEX) }

    fun matches(requestUrl: String) = regex?.matches(requestUrl) ?: (pattern == requestUrl)

    fun extractParameters(requestUrl: String): Map<String, String> {
        require(matches(requestUrl)) { "URL '$requestUrl' does not match path" }

        fun parameters(re: Regex) = re
            .findGroups(requestUrl)
            .mapIndexed { idx, (value) -> parameterIndex[idx] to value }
            .filter { (first) -> first != "" }
            .toMap()

        val re = regex
        return if (hasParameters && re != null) parameters(re) else emptyMap()
    }
}
