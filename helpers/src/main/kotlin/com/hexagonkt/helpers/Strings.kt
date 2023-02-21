package com.hexagonkt.helpers

import com.hexagonkt.core.eol
import kotlin.IllegalArgumentException
import java.text.Normalizer.Form.NFD
import java.text.Normalizer.normalize

private const val VARIABLE_PREFIX = "{{"
private const val VARIABLE_SUFFIX = "}}"

val CAMEL_CASE: Regex = Regex("[a-z]+([A-Z][a-z0-9]+)+")
val PASCAL_CASE: Regex = Regex("([A-Z][a-z0-9]+)+")
val SNAKE_CASE: Regex = Regex("[A-Za-z]+(_[A-Za-z0-9]+)+")
val KEBAB_CASE: Regex = Regex("[A-Za-z]+(-[A-Za-z0-9]+)+")

/**
 * Filter the target string substituting each key by its value. The keys format resembles Mustache's
 * one: `{{key}}` and all occurrences are replaced by the supplied value.
 *
 * If a variable does not have a parameter, it is left as it is.
 *
 * @param parameters The map with the list of key/value tuples.
 * @return The filtered text or the same string if no values are passed or found in the text.
 * @sample com.hexagonkt.helpers.StringsSamplesTest.filterVarsExample
 */
fun String.filterVars(parameters: Map<*, *>): String =
    this.filter(
        VARIABLE_PREFIX,
        VARIABLE_SUFFIX,
        *parameters
            .filterKeys { it != null }
            .map { (k, v) -> k.toString() to v.toString() }
            .toTypedArray()
    )

/**
 * Filter the target string substituting each key by its value. The keys format resembles Mustache's
 * one: `{{key}}` and all occurrences are replaced by the supplied value.
 *
 * If a variable does not have a parameter, it is left as it is.
 *
 * @param parameters vararg of key/value pairs.
 * @return The filtered text or the same string if no values are passed or found in the text.
 * @sample com.hexagonkt.helpers.StringsSamplesTest.filterVarsVarargExample
 *
 * @see filterVars
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
fun String.filter(prefix: String, suffix: String, vararg parameters: Pair<String, *>): String =
    parameters.fold(this) { result, (first, second) ->
        result.replace(prefix + first + suffix, second.toString())
    }

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @receiver .
 * @param T .
 * @param converter .
 * @return .
 */
fun <T : Enum<*>> String.toEnum(converter: (String) -> T): T =
    uppercase().replace(" ", "_").let(converter)

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @receiver .
 * @param T .
 * @param converter .
 * @return .
 */
fun <T : Enum<*>> String.toEnumOrNull(converter: (String) -> T): T? =
    try {
        toEnum(converter)
    }
    catch (e: IllegalArgumentException) {
        null
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

fun String.camelToWords(): List<String> =
    split("(?=\\p{Upper}\\p{Lower})".toRegex()).toWords()

fun String.snakeToWords(): List<String> =
    split("_").toWords()

fun String.kebabToWords(): List<String> =
    split("-").toWords()

fun List<String>.toWords(): List<String> =
    filter(String::isNotEmpty).map(String::lowercase)

fun List<String>.wordsToCamel(): String =
    wordsToPascal().replaceFirstChar(Char::lowercase)

fun List<String>.wordsToPascal(): String =
    joinToString("") { it.replaceFirstChar(Char::uppercase) }

fun List<String>.wordsToSnake(): String =
    joinToString("_")

fun List<String>.wordsToKebab(): String =
    joinToString("-")

fun List<String>.wordsToTitle(): String =
    joinToString(" ") { it.replaceFirstChar(Char::uppercase) }

fun List<String>.wordsToSentence(): String =
    joinToString(" ").replaceFirstChar(Char::uppercase)

/**
 * Transform the target string from snake case to camel case.
 */
fun String.snakeToCamel(): String =
    snakeToWords().wordsToCamel()

fun Enum<*>.toWords(): String =
    toString().lowercase().replace("_", " ")

/**
 * Transform the target string from camel case to snake case.
 */
fun String.camelToSnake(): String =
    camelToWords().wordsToSnake()

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
 * @param bytes .
 * @return .
 */
fun utf8(vararg bytes: Int): String =
    String(bytes.map(Int::toByte).toByteArray())

fun String.toEnumValue(): String =
    trim().uppercase().replace(" ", "_")

internal fun Sequence<Int>.maxOrElse(fallback: Int): Int =
    this.maxOrNull() ?: fallback
