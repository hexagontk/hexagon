package com.hexagonkt.helpers

import java.io.InputStream

/** Content Type -> Extensions. */
var extensions: Map<String, List<String>> =
    loadExtensions(resourceAsStream("serialization/mime.types") ?: error)

/** Extension -> Content Type. */
var mimeTypes: Map<String, String> =
    extensions.flatMap { it.value.map { ext -> ext to it.key } }.toMap()

private fun loadExtensions(input: InputStream): Map<String, List<String>> =
    input
        .bufferedReader()
        .readLines()
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .filter { !it.startsWith('#') }
        .map { it.split("""\s+""".toRegex()) }
        .map { it.first() to it.drop(1) }
        .toMap()
