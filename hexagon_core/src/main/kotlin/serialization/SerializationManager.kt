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

    internal var formatsMap: LinkedHashMap<String, SerializationFormat> = formatsMap()
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
    private var extensions: Map<String, List<String>> =
        loadContentTypeExtensions(Resource("serialization/mime.types").stream() ?: error)

    /** Extension -> Content Type. */
    internal var mimeTypes: Map<String, String> =
        extensions.flatMap { it.value.map { ext -> ext to it.key } }.toMap()

    fun setFormats(vararg formats: SerializationFormat) {
        SerializationManager.formats = linkedSetOf(*formats)
    }

    private fun loadContentTypeExtensions(input: InputStream): Map<String, List<String>> =
        input
            .bufferedReader()
            .readLines()
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .filter { !it.startsWith('#') }
            .map { it.split("""\s+""".toRegex()) }
            .map { it.first() to it.drop(1) }
            .toMap()

    private fun formatsMap () = linkedMapOf (*formats.map { it.contentType to it }.toTypedArray())

    private fun contentTypeOf(url: URL) = mimeTypes[url.file.substringAfterLast('.')]

    private fun contentTypeOf(file: File) = mimeTypes[file.extension]

    fun formatOf(contentType: String?): SerializationFormat =
        formatsMap[contentType] ?: error("$contentType not found")

    fun formatOf(contentType: String?, defaultFormat: SerializationFormat): SerializationFormat =
        formatsMap[contentType] ?: defaultFormat

    fun formatOf(url: URL): SerializationFormat = formatOf(contentTypeOf(url) ?: error)

    fun formatOf(file: File): SerializationFormat = formatOf(contentTypeOf(file) ?: error)
}
