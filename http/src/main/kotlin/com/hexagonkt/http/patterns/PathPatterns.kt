package com.hexagonkt.http.patterns

import com.hexagonkt.core.assertEnabled
import com.hexagonkt.http.patterns.TemplatePathPattern.Companion.WILDCARD

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

internal fun checkPathPatternVariables(pattern: String) {
    if (assertEnabled) {
        require(!pattern.contains(":")) {
            "Variables have {var} format. Path cannot have ':' $pattern"
        }
    }
}
