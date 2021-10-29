package com.hexagonkt.core.helpers

import java.lang.IllegalArgumentException
import java.util.regex.PatternSyntaxException

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @property pattern .
 */
data class Glob(val pattern: String) {

    /** [TODO](https://github.com/hexagonkt/hexagon/issues/271). */
    val regex: Regex =
        try {
            globToRegex(pattern).toRegex()
        }
        catch (e: PatternSyntaxException) {
            throw IllegalArgumentException("Pattern: '$pattern' is not a valid Glob", e)
        }

    /**
     * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
     *
     * @param text .
     * @return .
     */
    fun matches(text: String): Boolean =
        regex.matches(text)

    private fun globToRegex(pattern: String): String {
        var escaping = false
        var bracesCount = 0

        return pattern.toCharArray().joinToString("") { currentChar ->
            val globPart = when (currentChar) {
                '.', '(', ')', '+', '|', '^', '$', '@', '%' -> """\$currentChar"""
                '*' -> if (escaping) """\*""" else ".*"
                '?' -> if (escaping) """\?""" else "."
                '\\' -> if (escaping) """\\""" else ""
                '{' -> if (escaping) """\{""" else "("
                '}' -> when {
                    bracesCount > 0 && !escaping -> ")"
                    escaping -> """\}"""
                    else -> "}"
                }
                ',' -> when {
                    bracesCount > 0 && !escaping -> "|"
                    escaping -> "\\,"
                    else -> ","
                }
                else -> currentChar.toString()
            }

            if (currentChar == '{' && !escaping)
                bracesCount++

            if (currentChar == '}' && !escaping && bracesCount > 0)
                bracesCount--

            escaping = !escaping && currentChar == '\\'

            globPart
        }
    }
}
