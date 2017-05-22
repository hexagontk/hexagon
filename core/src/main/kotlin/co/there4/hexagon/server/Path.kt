package co.there4.hexagon.server

import co.there4.hexagon.helpers.CachedLogger
import co.there4.hexagon.helpers.filter
import co.there4.hexagon.helpers.findGroups

/**
 * A path definition. It parses path patterns and extract values for parameters.
 *
 * Differences with Sinatra:
 *
 *   * No splats (you can use named parameters though)
 *   * Delimiter is {var} to conform with [RFC 6570](https://tools.ietf.org/html/rfc6570)
 */
data class Path (val path: String) {
    private companion object : CachedLogger(Path::class) {
        internal const val PARAMETER_PREFIX = "{"
        internal const val PARAMETER_SUFFIX = "}"

        internal const val WILDCARD = "*"

        internal val WILDCARD_REGEX = Regex ("\\$WILDCARD")
        internal val PARAMETER_REGEX = Regex ("\\$PARAMETER_PREFIX\\w+\\$PARAMETER_SUFFIX")
        internal val PLACEHOLDER_REGEX =
            Regex ("\\$WILDCARD|\\$PARAMETER_PREFIX\\w+\\$PARAMETER_SUFFIX")
    }

    init {
        require(path.startsWith("/")) { "'$path' must start with '/'" }
        require(!path.contains(":")) { "Variables have {var} format. Path cannot have ':' $path" }
    }

    val hasWildcards by lazy { WILDCARD_REGEX in path }
    val hasParameters by lazy { PARAMETER_REGEX in path }

    val parameterIndex: List<String> by lazy {
        if (hasParameters)
            PLACEHOLDER_REGEX.findAll(path)
                .map {
                    if (it.value == WILDCARD) ""
                    else it.value.removePrefix(PARAMETER_PREFIX).removeSuffix(PARAMETER_SUFFIX)
                }
                .toList ()
        else
            emptyList()
    }

    val regex: Regex? by lazy {
        when (Pair (hasWildcards, hasParameters)) {
            Pair (true, true) ->
                Regex (path.replace (WILDCARD, "(.*?)").replace (PARAMETER_REGEX, "(.+?)") + "$")
            Pair (true, false) -> Regex (path.replace (WILDCARD, "(.*?)") + "$")
            Pair (false, true) -> Regex (path.replace (PARAMETER_REGEX, "(.+?)") + "$")
            else -> null
        }
    }

    val segments by lazy { path.split(PLACEHOLDER_REGEX) }

    fun matches (requestUrl: String) = regex?.matches(requestUrl) ?: (path == requestUrl)

    fun extractParameters (requestUrl: String): Map<String, String> {
        require(matches (requestUrl)) { "URL '$requestUrl' does not match path" }

        fun parameters (re: Regex) = re
            .findGroups(requestUrl)
            .mapIndexed { idx, (value) -> parameterIndex[idx] to value }
            .filter { (first) -> first != "" }
            .toMap()

        val re = regex
        return if (hasParameters && re != null) parameters(re) else emptyMap()
    }

    fun create(vararg parameters: Pair<String, Any>) =
        if (hasWildcards || parameters.size != parameterIndex.size) {
            val expectedParams = parameterIndex.size
            val paramCount = parameters.size
            error("Path has wildcards or different parameters: $expectedParams/$paramCount")
        }
        else {
            val map = parameters.map { it.first to it.second.toString() }
            path.filter (PARAMETER_PREFIX, PARAMETER_SUFFIX, *map.toTypedArray())
        }
}
