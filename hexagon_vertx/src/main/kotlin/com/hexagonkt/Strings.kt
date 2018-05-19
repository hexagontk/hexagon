package com.hexagonkt

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.lang.System.getProperty
import java.text.Normalizer.Form.NFD
import java.text.Normalizer.normalize

/** Runtime specific end of line. */
val eol: String = getProperty("line.separator")

/**
 * Transforms the target string from snake case to camel case.
 */
fun String.snakeToCamel (): String =
    this.split ("_")
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

fun String.stripAccents(): String = normalize(this, NFD).replace("\\p{M}".toRegex(), "")

fun String.toStream(): InputStream = ByteArrayInputStream(this.toByteArray())
