package com.hexagontk.http.patterns

class LiteralPathPattern(
    override val pattern: String = "",
    override val prefix: Boolean = false
) : PathPattern {

    init {
        checkPathPatternPrefix(pattern)
    }

    override fun addPrefix(prefix: String?): PathPattern =
        if (prefix == null) this
        else createPathPattern(prefix + pattern, this.prefix)

    override fun matches(requestUri: String): Boolean =
        if (prefix) requestUri.startsWith(pattern)
        else requestUri == pattern

    override fun extractParameters(requestUri: String): Map<String, String> =
        emptyMap()
}
