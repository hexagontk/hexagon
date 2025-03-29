package com.hexagontk.http.patterns

import com.hexagontk.http.patterns.TemplatePathPattern.Companion.WILDCARD

class WildcardPathPattern(override val prefix: Boolean = false) : PathPattern {

    override val pattern: String = WILDCARD

    override fun addPrefix(prefix: String?): PathPattern =
        if (prefix == null) this
        else createPathPattern("$prefix$WILDCARD", this.prefix)

    override fun matches(requestUri: String): Boolean =
        true

    override fun extractParameters(requestUri: String): Map<String, String> =
        mapOf(1.toString() to requestUri)
}
