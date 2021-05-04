package com.hexagonkt.serialization

import com.hexagonkt.ClasspathHandler
import com.hexagonkt.logging.Logger
import java.io.File
import java.net.URL

/**
 * TODO
 *
 * Can be a Serializer immutable class and:
 *
 * object SerializationManager : Serializer(mimeTypesResource, formats)
 */
object SerializationManager {

    private val logger: Logger = Logger(this::class)

    private val mimeTypesResource by lazy {
        ClasspathHandler.registerHandler() // Prevent error on runtimes not supporting SPI
        URL("classpath:serialization/mime.types")
    }

    /** List of formats. NOTE should be defined AFTER mapper definition to avoid runtime issues. */
    var formats: LinkedHashSet<SerializationFormat> = linkedSetOf()
        set(value) {
            require(value.isNotEmpty()) { "Formats list can not be empty" }
            field = value

            formatsMap = formatsMap()
            formats.forEach { format ->
                extensions = extensions + (format.contentType to format.extensions.toList())
                mimeTypes = mimeTypes + format.extensions.map { ext -> ext to format.contentType }
            }

            // TODO Check default update when changing formats list on a test
            if (defaultFormat !in field)
                defaultFormat = field.first()

            logger.info { serializationFormats() }
        }

    var defaultFormat: SerializationFormat? = formats.firstOrNull()
        set(value) {
            require(formats.contains(value)) {
                val contentTypes = formats.joinToString(", ") { it.contentType }
                "'$value' not available in: $contentTypes"
            }
            field = value

            logger.info { "Default serialization format set to '${field?.contentType}'" }
        }

    var mapper: Mapper? = null

    private var formatsMap: LinkedHashMap<String, SerializationFormat> = formatsMap()

    /** Content Type -> Extensions. */
    private var extensions: Map<String, List<String>> = loadContentTypeExtensions(mimeTypesResource)

    /** Extension -> Content Type. */
    private var mimeTypes: Map<String, String> =
        extensions.flatMap { it.value.map { ext -> ext to it.key } }.toMap()

    init {
        logger.info { serializationFormats() }
        logger.info { "Default serialization format set to '${defaultFormat?.contentType}'" }
        logger.info { "${extensions.size} Content types loaded from: ${mimeTypesResource.path}" }
    }

    fun requireMapper(): Mapper =
        mapper ?: error("Mapper adapter required. Set it using `SerializationManager.mapper`")

    fun requireDefaultFormat(): SerializationFormat =
        defaultFormat ?: error("No default serialization format defined")

    fun formats(vararg formats: SerializationFormat) {
        this.formats = LinkedHashSet(formats.toList())
    }

    fun defaultFormat(defaultFormat: SerializationFormat) {
        this.defaultFormat = defaultFormat
    }

    fun contentTypeOf(extension: String): String? = mimeTypes[extension]

    fun contentTypeOf(url: URL): String? = contentTypeOf(pathExtension(url.file))

    fun contentTypeOf(file: File): String? = mimeTypes[file.extension]

    fun formatOf(contentType: String): SerializationFormat =
        formatsMap[contentType] ?: error("$contentType serialization format not found")

    fun formatOf(contentType: String, defaultFormat: SerializationFormat): SerializationFormat =
        formatsMap[contentType] ?: defaultFormat

    fun formatOf(url: URL): SerializationFormat =
        formatOf(contentTypeOf(url) ?: error("Content type for '$url' not found"))

    fun formatOf(file: File): SerializationFormat =
        formatOf(contentTypeOf(file) ?: error("Content type for '${file.name}' not found"))

    private fun pathExtension(path: String): String = path.substringAfterLast('.')

    private fun loadContentTypeExtensions(input: URL): Map<String, List<String>> =
        input
            .openStream()
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

    private fun serializationFormats(): String =
        formats.joinToString("\n", "Serialization formats loaded:\n") {
            "* ${it.contentType} (${it.extensions.joinToString(", ")})"
        }
}
