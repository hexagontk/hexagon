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
 * Filters the target string substituting each key by its value. The keys format is:
 * `#{key}` and all occurrences are replaced by the supplied value.
 *
 * If a variable does not have a parameter, it is left as it is.
 *
 * @param parameters The map with the list of key/value tuples.
 * @return The filtered text or the same string if no values are passed or found in the text.
 * @sample com.hexagonkt.helpers.StringsSamplesTest.filterVarsExample
 */
fun String.filterVars (parameters: Map<*, *>): String =
    parameters.entries
        .asSequence()
        .filter { it.key.toString().isNotEmpty() }
        .fold(this) { result, pair ->
            val key = pair.key.toString()
            val value = pair.value.toString()
            result.replace ("$VARIABLE_PREFIX$key$VARIABLE_SUFFIX", value)
        }

fun String.filterVars (vararg parameters: Pair<*, *>) = this.filterVars(mapOf (*parameters))

fun String.filter (
    prefix: String, suffix: String, vararg parameters: Pair<String, String>): String =
        parameters.fold(this) { result, (first, second) ->
            result.replace (prefix + first + suffix, second)
        }

fun Regex.findGroups (str: String): List<MatchGroup> =
    (this.find (str)?.groups ?: listOf<MatchGroup> ())
        .map { it ?: throw IllegalArgumentException () }
        .drop(1)

/**
 * Transforms the target string from snake case to camel case.
 */
fun String.snakeToCamel (): String =
    this.split ("_")
        .asSequence()
        .filter(String::isNotEmpty)
        .joinToString("", transform = String::capitalize)
        .decapitalize ()

/**
 * Transforms the target string from camel case to snake case.
 */
fun String.camelToSnake (): String =
    this.split ("(?=\\p{Upper}\\p{Lower})".toRegex())
        .joinToString ("_", transform = String::toLowerCase)
        .decapitalize ()

/**
 * Formats the string as a banner with a delimiter above and below text. The character used to
 * render the delimiter is defined.
 *
 * @param bannerDelimiter Delimiter char for banners.
 */
fun String.banner (bannerDelimiter: String = "*"): String {
    val separator = bannerDelimiter.repeat (this.lines().asSequence().map { it.length }.max() ?: 0)
    return "$separator$eol$this$eol$separator"
}

fun String.stripAccents(): String = normalize(this, NFD).replace("\\p{M}".toRegex(), "")

fun String.toStream(): InputStream = ByteArrayInputStream(this.toByteArray())

fun utf8(vararg bytes: Int): String = String(bytes.map(Int::toByte).toByteArray())
