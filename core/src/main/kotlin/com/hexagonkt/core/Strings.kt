package com.hexagonkt.core

import java.io.ByteArrayInputStream
import java.io.InputStream
import kotlin.IllegalArgumentException
import java.lang.System.getProperty
import java.net.InetAddress
import java.net.URI
import java.net.URL
import java.text.Normalizer.Form.NFD
import java.text.Normalizer.normalize
import java.util.*
import kotlin.reflect.KClass

private const val VARIABLE_PREFIX = "{{"
private const val VARIABLE_SUFFIX = "}}"

private val base64Encoder: Base64.Encoder = Base64.getEncoder().withoutPadding()
private val base64Decoder: Base64.Decoder = Base64.getDecoder()

/** Runtime specific end of line. */
val eol: String by lazy { getProperty("line.separator") }

/**
 * Encode the content of this byteArray to base64.
 *
 * @receiver ByteArray to be encoded to base64.
 * @return The base64 encoded string.
 */
fun ByteArray.encodeToBase64(): String =
    base64Encoder.encodeToString(this)

/**
 * Encode this string to base64.
 *
 * @receiver String to be encoded to base64.
 * @return The base64 encoded string.
 */
fun String.encodeToBase64(): String =
    toByteArray().encodeToBase64()

/**
 * Decode this base64 encoded string.
 *
 * @receiver String encoded to base64.
 * @return The ByteArray result of decoding the base64 string.
 */
fun String.decodeBase64(): ByteArray =
    base64Decoder.decode(this)

/**
 * Filter the target string substituting each key by its value. The keys format resembles Mustache's
 * one: `{{key}}` and all occurrences are replaced by the supplied value.
 *
 * If a variable does not have a parameter, it is left as it is.
 *
 * @param parameters The map with the list of key/value tuples.
 * @return The filtered text or the same string if no values are passed or found in the text.
 * @sample com.hexagonkt.core.StringsSamplesTest.filterVarsExample
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
 * @sample com.hexagonkt.core.StringsSamplesTest.filterVarsVarargExample
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
 * @param T .
 * @param type .
 * @return .
 */
@Suppress("UNCHECKED_CAST") // All allowed types are checked at runtime
fun <T : Any> String?.toOrNull(type: KClass<T>): T? =
    this?.let {
        when (type) {
            Boolean::class -> this.toBooleanStrictOrNull()
            Int::class -> this.toIntOrNull()
            Long::class -> this.toLongOrNull()
            Float::class -> this.toFloatOrNull()
            Double::class -> this.toDoubleOrNull()
            String::class -> this
            InetAddress::class -> this.let(InetAddress::getByName)
            URL::class -> this.let(::URL)
            URI::class -> this.let(::URI)
            else -> error("Unsupported type: ${type.qualifiedName}")
        }
    } as? T

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
fun String.snakeToCamel(): String =
    snakeToWords().wordsToCamel()

fun String.snakeToWords(): List<String> =
    this.split("_").filter(String::isNotEmpty).map(String::lowercase)

fun List<String>.wordsToSnake(): String =
    joinToString("_").replaceFirstChar(Char::lowercase)

fun String.camelToWords(): List<String> =
    split("(?=\\p{Upper}\\p{Lower})".toRegex()).map(String::lowercase)

fun List<String>.wordsToCamel(): String =
    joinToString("") { it.replaceFirstChar(Char::uppercase) }.replaceFirstChar(Char::lowercase)

fun List<String>.wordsToTitle(): String =
    joinToString(" ") { it.replaceFirstChar(Char::uppercase) }

fun List<String>.wordsToSentence(): String =
    joinToString(" ").replaceFirstChar(Char::uppercase)

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

fun String.stripAnsi(): String =
    replace(Ansi.regex, "")

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
 * @param count .
 * @param pad .
 * @return .
 */
fun String.prependIndent(count: Int = 4, pad: String = " "): String =
    this.prependIndent(pad.repeat(count))

fun String.toEnumValue(): String =
    trim().uppercase().replace(" ", "_")

internal fun Sequence<Int>.maxOrElse(fallback: Int): Int =
    this.maxOrNull() ?: fallback
