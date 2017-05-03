package co.there4.hexagon.server

import co.there4.hexagon.util.CachedLogger
import co.there4.hexagon.util.findGroups
import co.there4.hexagon.util.filter
import kotlin.text.Regex

/**
 * A path definition. It parses path patterns and extract values for parameters.
 *
 * Differences with Sinatra:
 *
 *   * No splats (you can use named parameters though)
 *   * Delimiter is {var} to conform with [RFC 6570](https://tools.ietf.org/html/rfc6570)
 */
data class Path (val path: String) {
    companion object : CachedLogger(Path::class) {
        internal val PARAMETER_PREFIX = "{"
        internal val PARAMETER_SUFFIX = "}"

        internal val WILDCARD = "*"

        internal val WILDCARD_REGEX = Regex ("\\$WILDCARD")
        internal val PARAMETER_REGEX = Regex ("\\$PARAMETER_PREFIX\\w+\\$PARAMETER_SUFFIX")
        internal val PLACEHOLDER_REGEX = Regex ("\\$WILDCARD|\\$PARAMETER_PREFIX\\w+\\$PARAMETER_SUFFIX")
    }

    init {
        require(path.startsWith("/")) { "$path must start with '/'" }
        require(!path.contains(":")) { "Variables has {var} format. Path cannot have ':' $path" }
    }

    val hasWildcards = WILDCARD_REGEX in path
    val hasParameters = PARAMETER_REGEX in path

    val parameterIndex: List<String> =
        if (hasParameters)
            PLACEHOLDER_REGEX.findAll(path)
                .map {
                    if (it.value == WILDCARD)
                        ""
                    else {
                        val start = PARAMETER_PREFIX.length
                        val end = it.value.length - PARAMETER_SUFFIX.length
                        it.value.substring(start, end)
                    }
                }
                .toList ()
        else
            listOf ()

    val regex: Regex? = when (Pair (hasWildcards, hasParameters)) {
        Pair (true, true) ->
            Regex (path.replace (WILDCARD, "(.*?)").replace (PARAMETER_REGEX, "(.+?)") + "$")
        Pair (true, false) -> Regex (path.replace (WILDCARD, "(.*?)") + "$")
        Pair (false, true) -> Regex (path.replace (PARAMETER_REGEX, "(.+?)") + "$")
        else -> null
    }

    val segments by lazy { path.split(PLACEHOLDER_REGEX) }

    fun matches (requestUrl: String) = regex?.matches(requestUrl) ?: (path == requestUrl)

    fun extractParameters (requestUrl: String): Map<String, String> =
        if (!matches (requestUrl))
            throw IllegalArgumentException ("URL '$requestUrl' does not match path")
        else if (hasParameters && regex != null)
            regex.findGroups (requestUrl)
                .mapIndexed { idx, match -> parameterIndex[idx] to match.value }
                .filter { pair -> pair.first != "" }
                .toMap ()
        else
            mapOf ()

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
