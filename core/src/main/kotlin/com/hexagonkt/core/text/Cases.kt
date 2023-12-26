package com.hexagonkt.core.text

val CAMEL_CASE: Regex by lazy { Regex("[a-z]+([A-Z][a-z0-9]+)+") }
val PASCAL_CASE: Regex by lazy { Regex("([A-Z][a-z0-9]+)+") }
val SNAKE_CASE: Regex by lazy { Regex("[_A-Za-z]+[_A-Za-z0-9]*") }
val KEBAB_CASE: Regex by lazy { Regex("[\\-A-Za-z]+[\\-A-Za-z0-9]*") }

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
