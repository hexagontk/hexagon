package com.hexagonkt.serialization

import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.WRITE_DOC_START_MARKER
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory

object SerializationManager {
    /** List of formats. NOTE should be defined AFTER mapper definition to avoid runtime issues. */
    private val formatList = listOf (
        JacksonTextFormat("json"),
        JacksonTextFormat("yaml") {
            with(YAMLFactory()) { configure(WRITE_DOC_START_MARKER, false) }
        }
    )

    private val formats = mapOf (*formatList.map { it.contentType to it }.toTypedArray())

    val contentTypes = formatList.map { it.contentType }

    var defaultFormat: String = contentTypes.first()
        set(value) {
            check(contentTypes.contains(value))
            field = value
        }

    internal fun getFormat(contentType: String) =
        formats[contentType] ?: error("$contentType not found")
}
