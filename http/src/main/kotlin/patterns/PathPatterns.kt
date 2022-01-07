package com.hexagonkt.http.patterns

/*
 * TODO Move http_server handlers here and generalize to allow them to be used by server and client
 */

import com.hexagonkt.core.disableChecks

fun createPathPattern(pattern: String, prefix: Boolean): PathPattern =
    when {
        TemplatePathPattern.isTemplate(pattern) -> TemplatePathPattern(pattern, prefix)
        else -> LiteralPathPattern(pattern, prefix)
    }

fun checkPathPatternPrefix(pattern: String, allowedPatterns: List<String> = listOf("")) {
    if (!disableChecks)
        require(pattern in allowedPatterns || pattern.startsWith('/')) {
            "'$pattern' must be empty or start with '/'"
        }
}

fun checkPathPatternVariables(pattern: String) {
    if (!disableChecks) {
        require(!pattern.contains(":")) {
            "Variables have {var} format. Path cannot have ':' $pattern"
        }
    }
}
