package com.hexagonkt.helpers

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.lang.System.getProperty
import java.text.Normalizer.Form.NFD
import java.text.Normalizer.normalize

/** Variable prefix for string filtering. It starts with '#' because of Kotlin's syntax. */
private const val VARIABLE_PREFIX = "#{"
/** Variable suffix for string filtering. */
private const val VARIABLE_SUFFIX = "}"

/** Runtime specific end of line. */
val eol: String by lazy { getProperty("line.separator") }

/**
 * Filter the target string substituting each key by its value. The keys format is:
 * `#{key}` and all occurrences are replaced by the supplied value.
 *
 * If a variable does not have a parameter, it is left as it is.
 *
 * @param parameters The map with the list of key/value tuples.
 * @return The filtered text or the same string if no values are passed or found in the text.
 * @sample com.hexagonkt.helpers.StringsSamplesTest.filterVarsExample
 */
fun String.filterVars(parameters: Map<*, *>): String =
    parameters.entries
        .asSequence()
        .filter { it.key.toString().isNotEmpty() }
        .fold(this) { result, pair ->
            val key = pair.key.toString()
            val value = pair.value.toString()
            result.replace("$VARIABLE_PREFIX$key$VARIABLE_SUFFIX", value)
        }

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @receiver .
 * @param parameters .
 * @return .
 */
fun String.filterVars(vararg parameters: Pair<*, *>) =
    this.filterVars(mapOf(*parameters))

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @receiver .
 * @param prefix .
 * @param suffix .
 * @param parameters .
 * @return .
 */
fun String.filter(prefix: String, suffix: String, vararg parameters: Pair<String, String>): String =
    parameters.fold(this) { result, (first, second) ->
        result.replace(prefix + first + suffix, second)
    }

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @receiver .
 * @param text .
 * @return .
 */
fun Regex.findGroups(text: String): List<MatchGroup> =
    (this.find(text)?.groups ?: emptyList<MatchGroup>())
        .filterNotNull()
        .drop(1)

/**
 * Transform the target string from snake case to camel case.
 */
@ExperimentalStdlibApi // TODO Remove when using Kotlin 1.5
fun String.snakeToCamel(): String =
    this.split("_")
        .asSequence()
        .filter(String::isNotEmpty)
        .joinToString("", transform = { it.replaceFirstChar(Char::uppercase) })
        .replaceFirstChar(Char::lowercase)

/**
 * Transform the target string from camel case to snake case.
 */
@ExperimentalStdlibApi // TODO Remove when using Kotlin 1.5
fun String.camelToSnake(): String =
    this.split("(?=\\p{Upper}\\p{Lower})".toRegex())
        .joinToString("_", transform = String::lowercase)
        .replaceFirstChar(Char::lowercase)

/**
 * Format the string as a banner with a delimiter above and below text. The character used to
 * render the delimiter is defined.
 *
 * @param bannerDelimiter Delimiter char for banners.
 */
fun String.banner(bannerDelimiter: String = "*"): String =
    bannerDelimiter
        .repeat(this
            .lines()
            .asSequence()
            .map { it.length }
            .maxOrElse(0)
        )
        .let { "$it$eol$this$eol$it" }

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @receiver .
 * @return .
 */
fun String.stripAccents(): String =
    normalize(this, NFD).replace("\\p{M}".toRegex(), "")

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @receiver .
 * @return .
 */
fun String.toStream(): InputStream =
    ByteArrayInputStream(this.toByteArray())

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @param bytes .
 * @return .
 */
fun utf8(vararg bytes: Int): String =
    String(bytes.map(Int::toByte).toByteArray())

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @receiver .
 * @return .
 */
fun String.globToRegex(): Regex = Regex(
    this.map {
        when (it) {
            '*' -> ".*"
            '?' -> "."
            '.' -> "\\."
            '\\' -> "\\\\"
            else -> it.toString()
        }
    }
    .joinToString("", "^", "$")
)

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @receiver .
 * @param count .
 * @param pad .
 * @return .
 */
fun String.prependIndent(count: Int = 4, pad: String = " "): String =
    this.prependIndent(pad.repeat(count))

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @receiver .
 * @param fallback .
 * @return .
 */
internal fun Sequence<Int>.maxOrElse(fallback: Int): Int =
    this.maxOrNull() ?: fallback
