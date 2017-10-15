package com.hexagonkt.serialization

import com.hexagonkt.helpers.eol
import com.hexagonkt.helpers.mimeTypes

object SerializationManager {
    /** List of formats. NOTE should be defined AFTER mapper definition to avoid runtime issues. */
    var formats: LinkedHashSet<SerializationFormat> = coreFormats
        set(value) {
            require(value.isNotEmpty()) { "Formats list can not be empty" }
            field = value
            contentTypes = contentTypes()
            formatsMap = formatsMap()
            mimeTypes.addMimeTypes(
                formats.joinToString(eol) { it.contentType + " " + it.extensions.joinToString(" ") }
            )
        }

    var contentTypes: LinkedHashSet<String> = contentTypes()
        private set

    var formatsMap: LinkedHashMap<String, SerializationFormat> = formatsMap()
        private set

    var defaultFormat: String = contentTypes.first()
        set(value) {
            require(contentTypes.contains(value)) {
                "'$value' not available in: ${contentTypes.joinToString(", ")}"
            }
            field = value
        }

    internal fun getContentTypeFormat(contentType: String): SerializationFormat =
        formatsMap[contentType] ?: error("$contentType not found")

    internal fun getFileFormat(extension: String): SerializationFormat =
        getContentTypeFormat(mimeTypes.getContentType(extension))

    private fun contentTypes () = LinkedHashSet(formats.map { it.contentType })

    private fun formatsMap () = linkedMapOf (*formats.map { it.contentType to it }.toTypedArray())
}
