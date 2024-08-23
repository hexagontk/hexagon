package com.hexagontk.http.patterns

import com.hexagontk.core.assertEnabled
import com.hexagontk.http.patterns.TemplatePathPattern.Companion.WILDCARD

fun createPathPattern(pattern: String, prefix: Boolean): PathPattern =
    when {
        pattern == WILDCARD -> WildcardPathPattern(prefix)
        TemplatePathPattern.isTemplate(pattern) -> TemplatePathPattern(pattern, prefix)
        else -> LiteralPathPattern(pattern, prefix)
    }

internal fun checkPathPatternPrefix(pattern: String, allowedPrefixes: List<String> = emptyList()) {
    if (assertEnabled)
        require(
            pattern.isEmpty()
            || pattern.startsWith('/')
            || allowedPrefixes.any { pattern.startsWith(it) }) {

            "'$pattern' must be empty or start with '/'"
        }
}
