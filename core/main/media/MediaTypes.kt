package com.hexagontk.core.media

import com.hexagontk.core.media.MediaTypeGroup.*
import java.io.File
import java.net.URI
import java.net.URL
import java.nio.file.Path
import kotlin.io.path.extension

val MEDIA_TYPE_FORMAT: Regex by lazy { """\*|([\w+.-]+)""".toRegex() }

val ANY_MEDIA: MediaType = MediaType(ANY, "*")

val APPLICATION_AVRO: MediaType = MediaType(APPLICATION, "avro")
val APPLICATION_CBOR: MediaType = MediaType(APPLICATION, "cbor")
val APPLICATION_JSON: MediaType = MediaType(APPLICATION, "json")
val APPLICATION_YAML: MediaType = MediaType(APPLICATION, "yaml")
val APPLICATION_XML: MediaType = MediaType(APPLICATION, "xml")
val APPLICATION_GZIP: MediaType = MediaType(APPLICATION, "gzip")
val APPLICATION_COMPRESS: MediaType = MediaType(APPLICATION, "compress")
val APPLICATION_OCTET_STREAM: MediaType = MediaType(APPLICATION, "octet-stream")
val APPLICATION_PDF: MediaType = MediaType(APPLICATION, "pdf")
val APPLICATION_RTF: MediaType = MediaType(APPLICATION, "rtf")
val APPLICATION_X_TAR: MediaType = MediaType(APPLICATION, "x-tar")
val APPLICATION_ZIP: MediaType = MediaType(APPLICATION, "zip")
val APPLICATION_JAVA_ARCHIVE: MediaType = MediaType(APPLICATION, "java-archive")
val APPLICATION_OGG: MediaType = MediaType(APPLICATION, "ogg")
val APPLICATION_RAR: MediaType = MediaType(APPLICATION, "vnd.rar")
val APPLICATION_XHTML: MediaType = MediaType(APPLICATION, "xhtml+xml")
val APPLICATION_WEB_MANIFEST: MediaType = MediaType(APPLICATION, "manifest+json")
val APPLICATION_TOML: MediaType = MediaType(APPLICATION, "toml")
val APPLICATION_7Z: MediaType = MediaType(APPLICATION, "x-7z-compressed")
val APPLICATION_BZIP: MediaType = MediaType(APPLICATION, "x-bzip")
val APPLICATION_BZIP2: MediaType = MediaType(APPLICATION, "x-bzip2")
val APPLICATION_PHP: MediaType = MediaType(APPLICATION, "x-httpd-php")
val APPLICATION_FORM_URLENCODED: MediaType = MediaType(APPLICATION, "x-www-form-urlencoded")

val MULTIPART_ALTERNATIVE: MediaType by lazy { MediaType(MULTIPART, "alternative") }
val MULTIPART_DIGEST: MediaType by lazy { MediaType(MULTIPART, "digest") }
val MULTIPART_MIXED: MediaType by lazy { MediaType(MULTIPART, "mixed") }
val MULTIPART_PARALLEL: MediaType by lazy { MediaType(MULTIPART, "parallel") }
val MULTIPART_FORM_DATA: MediaType by lazy { MediaType(MULTIPART, "form-data") }

val TEXT_CSV: MediaType by lazy { MediaType(TEXT, "csv") }
val TEXT_X_JAVA_PROPERTIES: MediaType by lazy { MediaType(TEXT, "x-java-properties") }
val TEXT_JAVASCRIPT: MediaType by lazy { MediaType(TEXT, "javascript") }
val TEXT_CSS: MediaType by lazy { MediaType(TEXT, "css") }
val TEXT_HTML: MediaType by lazy { MediaType(TEXT, "html") }
val TEXT_MARKDOWN: MediaType by lazy { MediaType(TEXT, "markdown") }
val TEXT_PLAIN: MediaType by lazy { MediaType(TEXT, "plain") }
val TEXT_RICHTEXT: MediaType by lazy { MediaType(TEXT, "richtext") }
val TEXT_TAB_SEPARATED_VALUES: MediaType by lazy { MediaType(TEXT, "tab-separated-values") }
val TEXT_CALENDAR: MediaType by lazy { MediaType(TEXT, "calendar") }
val TEXT_EVENT_STREAM: MediaType by lazy { MediaType(TEXT, "event-stream") }

val DEFAULT_MEDIA_TYPE: MediaType by lazy { APPLICATION_OCTET_STREAM }

// TODO Allow adding media types manually (allow resource loading, and keep here the bare minimum)
//  With something like MediaTypeManager (as SerializationManager)
internal var mediaTypesExtensions: Map<String, MediaType> = mapOf(
    "avro" to APPLICATION_AVRO,
    "cbor" to APPLICATION_CBOR,
    "json" to APPLICATION_JSON,
    "yaml" to APPLICATION_YAML,
    "yml" to APPLICATION_YAML,
    "xml" to APPLICATION_XML,
    "bin" to APPLICATION_OCTET_STREAM,
    "lha" to APPLICATION_OCTET_STREAM,
    "lzh" to APPLICATION_OCTET_STREAM,
    "exe" to APPLICATION_OCTET_STREAM,
    "so" to APPLICATION_OCTET_STREAM,
    "class" to APPLICATION_OCTET_STREAM,
    "pdf" to APPLICATION_PDF,
    "rtf" to APPLICATION_RTF,
    "z" to APPLICATION_COMPRESS,
    "gz" to APPLICATION_GZIP,
    "gzip" to APPLICATION_GZIP,
    "tar" to APPLICATION_X_TAR,
    "zip" to APPLICATION_ZIP,
    "jar" to APPLICATION_JAVA_ARCHIVE,
    "ogx" to APPLICATION_OGG,
    "rar" to APPLICATION_RAR,
    "7z" to APPLICATION_7Z,
    "bz" to APPLICATION_BZIP,
    "bz2" to APPLICATION_BZIP2,
    "php" to APPLICATION_PHP,
    "xhtml" to APPLICATION_XHTML,
    "webmanifest" to APPLICATION_WEB_MANIFEST,
    "toml" to APPLICATION_TOML,
    "csv" to TEXT_CSV,
    "properties" to TEXT_X_JAVA_PROPERTIES,
    "js" to TEXT_JAVASCRIPT,
    "css" to TEXT_CSS,
    "html" to TEXT_HTML,
    "htm" to TEXT_HTML,
    "md" to TEXT_MARKDOWN,
    "txt" to TEXT_PLAIN,
    "log" to TEXT_PLAIN,
    "rtx" to TEXT_RICHTEXT,
    "tsv" to TEXT_TAB_SEPARATED_VALUES,
    "ics" to TEXT_CALENDAR,
)

internal val EXTENSIONS_BY_MEDIA: Map<MediaType, List<String>> by lazy {
    mediaTypesExtensions.entries
        .groupBy { it.value }
        .mapValues {
            it.value.map { entry -> entry.key }
        }
}

fun parseMediaType(fullType: String): MediaType {
    val groupType = fullType.split("/")
    require(groupType.size == 2) { "Media type format must be <type>/<subtype>: $fullType" }

    val groupText = groupType.first().uppercase()
    val group = if (groupText == "*") ANY else MediaTypeGroup.valueOf(groupText)
    val type = groupType.last()
    return MediaType(group, type)
}

fun mediaTypeOfOrNull(uri: URI): MediaType? =
    mediaTypeOfOrNull(pathExtension(uri.path))

fun mediaTypeOfOrNull(url: URL): MediaType? =
    mediaTypeOfOrNull(pathExtension(url.file))

fun mediaTypeOfOrNull(file: File): MediaType? =
    mediaTypeOfOrNull(file.extension)

fun mediaTypeOfOrNull(path: Path): MediaType? =
    mediaTypeOfOrNull(path.extension)

fun mediaTypeOfOrNull(extension: String): MediaType? =
    mediaTypesExtensions[extension]

fun mediaTypeOf(uri: URI): MediaType =
    mediaTypeOfOrNull(uri) ?: error("Media type not found for: '$uri' URI")

fun mediaTypeOf(url: URL): MediaType =
    mediaTypeOfOrNull(url) ?: error("Media type not found for: '$url' URL")

fun mediaTypeOf(file: File): MediaType =
    mediaTypeOfOrNull(file) ?: error("Media type not found for: '$file' file")

fun mediaTypeOf(path: Path): MediaType =
    mediaTypeOfOrNull(path) ?: error("Media type not found for: '$path' path")

fun mediaTypeOf(extension: String): MediaType =
    mediaTypeOfOrNull(extension) ?: error("Media type not found for: '$extension' extension")

fun extensionsOf(mediaType: MediaType): List<String> =
    EXTENSIONS_BY_MEDIA[mediaType] ?: emptyList()

fun pathExtension(path: String): String =
    path.substringAfterLast('.')
