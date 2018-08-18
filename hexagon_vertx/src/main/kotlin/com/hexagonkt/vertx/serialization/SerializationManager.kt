package com.hexagonkt.vertx.serialization

import com.hexagonkt.helpers.error
import io.vertx.core.http.impl.MimeMapping

object SerializationManager {
    val coreFormats: LinkedHashSet<SerializationFormat> = linkedSetOf (JsonFormat, YamlFormat)

    /** List of formats. NOTE should be defined AFTER mapper definition to avoid runtime issues. */
    var formats: LinkedHashSet<SerializationFormat> = coreFormats
        set(value) {
            require(value.isNotEmpty()) { "Formats list can not be empty" }
            field = value
            formatsMap = formatsMap()
            extensions = value.flatMap { f -> f.extensions.map { it to f.contentType } }.toMap()
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

    private var extensions: Map<String, String> = extensions(coreFormats)

    fun setFormats(vararg formats: SerializationFormat) {
        SerializationManager.formats = linkedSetOf(*formats)
    }

    fun getContentTypeFormat(contentType: String): SerializationFormat =
        formatsMap[contentType] ?: error("$contentType not found")

    fun getMimeTypeForExtension(ext: String): String =
        extensions[ext] ?: MimeMapping.getMimeTypeForExtension(ext)

    fun getMimeTypeForFilename(filename: String): String {
        val ext = filename.substringAfterLast('.')
        return extensions[ext] ?: MimeMapping.getMimeTypeForFilename(filename) ?: error
    }

    internal fun getFileFormat(extension: String): SerializationFormat =
        getContentTypeFormat(getMimeTypeForFilename(extension))

    private fun formatsMap () = linkedMapOf (*formats.map { it.contentType to it }.toTypedArray())

    private fun extensions (formats: HashSet<SerializationFormat>) =
        formats.flatMap { f -> f.extensions.map { it to f.contentType } }.toMap()
}
