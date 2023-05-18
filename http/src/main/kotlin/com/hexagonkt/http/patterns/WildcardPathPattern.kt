package com.hexagonkt.http.patterns

import com.hexagonkt.http.patterns.TemplatePathPattern.Companion.WILDCARD

data class WildcardPathPattern(override val prefix: Boolean = false) : PathPattern {

    override val pattern: String = WILDCARD

    override fun addPrefix(prefix: String?): PathPattern =
        if (prefix == null) this
        else createPathPattern("$prefix/$WILDCARD", this.prefix)

    override fun matches(requestUrl: String): Boolean =
        true

    override fun extractParameters(requestUrl: String): Map<String, String> =
        mapOf(1.toString() to requestUrl)
}
