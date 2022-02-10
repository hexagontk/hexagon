package com.hexagonkt.http.patterns

data class LiteralPathPattern(
    override val pattern: String = "",
    override val prefix: Boolean = false
) : PathPattern {

    init {
        checkPathPatternPrefix(pattern)
    }

    override fun addPrefix(prefix: String?): PathPattern =
        if (prefix == null) this
        else createPathPattern(prefix + pattern, this.prefix)

    override fun matches(requestUrl: String): Boolean =
        if (prefix) requestUrl.startsWith(pattern)
        else requestUrl == pattern

    override fun extractParameters(requestUrl: String): Map<String, String> =
        emptyMap()
}
