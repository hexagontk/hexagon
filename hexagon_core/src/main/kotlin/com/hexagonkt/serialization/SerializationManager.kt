package com.hexagonkt.serialization

import com.hexagonkt.helpers.eol
import javax.activation.MimetypesFileTypeMap

object SerializationManager {
    /** List of formats. NOTE should be defined AFTER mapper definition to avoid runtime issues. */
    var formats: LinkedHashSet<SerializationFormat> = coreFormats
        set(value) {
            require(value.isNotEmpty()) { "Formats list can not be empty" }
            field = value
            contentTypes = contentTypes()
            formatsMap = formatsMap()
            fileTypes = fileTypes()
        }

    var fileTypes: MimetypesFileTypeMap = fileTypes()
        private set

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
        getContentTypeFormat(fileTypes.getContentType(extension))

    private fun contentTypes () = LinkedHashSet(formats.map { it.contentType })

    private fun formatsMap () = linkedMapOf (*formats.map { it.contentType to it }.toTypedArray())

    private fun fileTypes() = MimetypesFileTypeMap().also {
        it.addMimeTypes(
            formats.joinToString (eol) { it.contentType + " " + it.extensions.joinToString(" ")  }
        )
    }
}
