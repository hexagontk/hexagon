package com.hexagonkt.serialization

import com.hexagonkt.helpers.extensions
import com.hexagonkt.helpers.mimeTypes

object SerializationManager {
    val coreFormats: LinkedHashSet<SerializationFormat> = linkedSetOf (JsonFormat, YamlFormat)

    /** List of formats. NOTE should be defined AFTER mapper definition to avoid runtime issues. */
    var formats: LinkedHashSet<SerializationFormat> = coreFormats
        set(value) {
            require(value.isNotEmpty()) { "Formats list can not be empty" }
            field = value
            formatsMap = formatsMap()
            formats.forEach { format ->
                extensions += format.contentType to format.extensions.toList()
                mimeTypes += format.extensions.map { ext -> ext to format.contentType }
            }
        }

    var formatsMap: LinkedHashMap<String, SerializationFormat> = formatsMap()
        private set

    var defaultFormat: SerializationFormat = formats.first()
        set(value) {
            require(formats.contains(value)) {
                val contentTypes = formats.joinToString(", ") { it.contentType }
                "'$value' not available in: $contentTypes"
            }
            field = value
        }

    fun setFormats(vararg formats: SerializationFormat) {
        SerializationManager.formats = linkedSetOf(*formats)
    }

    fun getContentTypeFormat(contentType: String): SerializationFormat =
        formatsMap[contentType] ?: error("$contentType not found")

    internal fun getFileFormat(file: String): SerializationFormat =
        getContentTypeFormat(mimeTypes[file.substringAfterLast('.')] ?: error("No mime type found for '$file'"))

    private fun formatsMap () = linkedMapOf (*formats.map { it.contentType to it }.toTypedArray())
}
