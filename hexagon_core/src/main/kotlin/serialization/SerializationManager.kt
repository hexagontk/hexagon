package com.hexagonkt.serialization

import com.hexagonkt.helpers.eol
import com.hexagonkt.helpers.mimeTypes

object SerializationManager {
    val coreFormats: LinkedHashSet<SerializationFormat> = linkedSetOf (JsonFormat, YamlFormat)

    /** List of formats. NOTE should be defined AFTER mapper definition to avoid runtime issues. */
    var formats: LinkedHashSet<SerializationFormat> = coreFormats
        set(value) {
            require(value.isNotEmpty()) { "Formats list can not be empty" }
            field = value
            contentTypes = contentTypes()
            formatsMap = formatsMap()
            extensions = value.flatMap { f -> f.extensions.map { it to f.contentType } }.toMap()
            mimeTypes.addMimeTypes(
                formats.joinToString(eol) { "${it.contentType} ${it.extensions.joinToString(" ")}" }
            )
        }

    var contentTypes: LinkedHashSet<String> = contentTypes()
        private set

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

    private var extensions: Map<String, String> = extensions(coreFormats)

    fun setFormats(vararg formats: SerializationFormat) {
        SerializationManager.formats = linkedSetOf(*formats)
    }

    fun getContentTypeFormat(contentType: String): SerializationFormat =
        formatsMap[contentType] ?: error("$contentType not found")

    internal fun getFileFormat(extension: String): SerializationFormat =
        getContentTypeFormat(mimeTypes.getContentType(extension))

    private fun contentTypes () = LinkedHashSet(formats.map { it.contentType })

    private fun formatsMap () = linkedMapOf (*formats.map { it.contentType to it }.toTypedArray())

    private fun extensions (formats: HashSet<SerializationFormat>) =
        formats.flatMap { f -> f.extensions.map { it to f.contentType } }.toMap()
}
