package com.hexagonkt.serialization

import com.hexagonkt.helpers.Resource
import com.hexagonkt.helpers.error
import java.io.File
import java.io.InputStream
import java.net.URL

object SerializationManager {
    val coreFormats: LinkedHashSet<SerializationFormat> =
        linkedSetOf (JsonFormat, YamlFormat)//, CsvFormat)

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

    /** Content Type -> Extensions. */
    var extensions: Map<String, List<String>> =
        loadExtensions(Resource("serialization/mime.types").stream() ?: error)

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

    fun setFormats(vararg formats: SerializationFormat) {
        SerializationManager.formats = linkedSetOf(*formats)
    }

    fun getContentTypeFormat(contentType: String): SerializationFormat =
        formatsMap[contentType] ?: error("$contentType not found")

    internal fun getFileFormat(file: String): SerializationFormat =
        getContentTypeFormat(mimeTypes[file.substringAfterLast('.')] ?: error("No mime type found for '$file'"))

    private fun formatsMap () = linkedMapOf (*formats.map { it.contentType to it }.toTypedArray())

    // UTILITIES
    fun contentType(url: URL): SerializationFormat =
        getContentTypeFormat(mimeTypes[url.file.substringAfterLast('.')] ?: error)

    fun contentType(file: File): SerializationFormat =
        getContentTypeFormat(mimeTypes[file.extension] ?: error)
}
