package com.hexagonkt.core.media

import com.hexagonkt.core.media.MediaTypeGroup.*
import java.io.File
import java.net.URI
import java.net.URL
import java.nio.file.Path
import kotlin.io.path.extension

val MEDIA_TYPE_FORMAT: Regex = """\*|([\w+.-]+)""".toRegex()

val APPLICATION_AVRO: MediaType = MediaType(APPLICATION, "avro")
val APPLICATION_CBOR: MediaType = MediaType(APPLICATION, "cbor")
val APPLICATION_JSON: MediaType = MediaType(APPLICATION, "json")
val APPLICATION_YAML: MediaType = MediaType(APPLICATION, "yaml")
val APPLICATION_XML: MediaType = MediaType(APPLICATION, "xml")
val APPLICATION_GZIP: MediaType = MediaType(APPLICATION, "gzip")
val APPLICATION_COMPRESS: MediaType = MediaType(APPLICATION, "compress")
val APPLICATION_OCTET_STREAM: MediaType = MediaType(APPLICATION, "octet-stream")
val APPLICATION_PDF: MediaType = MediaType(APPLICATION, "pdf")
val APPLICATION_POSTSCRIPT: MediaType = MediaType(APPLICATION, "postscript")
val APPLICATION_RTF: MediaType = MediaType(APPLICATION, "rtf")
val APPLICATION_X_CSH: MediaType = MediaType(APPLICATION, "x-csh")
val APPLICATION_X_GTAR: MediaType = MediaType(APPLICATION, "x-gtar")
val APPLICATION_X_LATEX: MediaType = MediaType(APPLICATION, "x-latex")
val APPLICATION_X_SH: MediaType = MediaType(APPLICATION, "x-sh")
val APPLICATION_X_TAR: MediaType = MediaType(APPLICATION, "x-tar")
val APPLICATION_X_TCL: MediaType = MediaType(APPLICATION, "x-tcl")
val APPLICATION_X_TEX: MediaType = MediaType(APPLICATION, "x-tex")
val APPLICATION_X_TEXINFO: MediaType = MediaType(APPLICATION, "x-texinfo")
val APPLICATION_ZIP: MediaType = MediaType(APPLICATION, "zip")
val APPLICATION_EPUB_ZIP: MediaType = MediaType(APPLICATION, "epub+zip")
val APPLICATION_JAVA_ARCHIVE: MediaType = MediaType(APPLICATION, "java-archive")
val APPLICATION_OGG: MediaType = MediaType(APPLICATION, "ogg")
val APPLICATION_RAR: MediaType = MediaType(APPLICATION, "vnd.rar")
val APPLICATION_XHTML: MediaType = MediaType(APPLICATION, "xhtml+xml")
val APPLICATION_WEB_MANIFEST: MediaType = MediaType(APPLICATION, "manifest+json")
val APPLICATION_TOML: MediaType = MediaType(APPLICATION, "toml")
val APPLICATION_7Z: MediaType = MediaType(APPLICATION, "x-7z-compressed")
val APPLICATION_BZIP: MediaType = MediaType(APPLICATION, "x-bzip")
val APPLICATION_BZIP2: MediaType = MediaType(APPLICATION, "x-bzip2")
val APPLICATION_CDF: MediaType = MediaType(APPLICATION, "x-cdf")
val APPLICATION_PHP: MediaType = MediaType(APPLICATION, "x-httpd-php")

val AUDIO_BASIC: MediaType = MediaType(AUDIO, "basic")
val AUDIO_MPEG: MediaType = MediaType(AUDIO, "mpeg")
val AUDIO_WAV: MediaType = MediaType(AUDIO, "wav")
val AUDIO_AAC: MediaType = MediaType(AUDIO, "aac")
val AUDIO_MIDI: MediaType = MediaType(AUDIO, "midi")
val AUDIO_OGG: MediaType = MediaType(AUDIO, "ogg")
val AUDIO_OPUS: MediaType = MediaType(AUDIO, "opus")
val AUDIO_WEBM: MediaType = MediaType(AUDIO, "webm")

val FONT_OTF: MediaType = MediaType(FONT, "otf")
val FONT_TTF: MediaType = MediaType(FONT, "ttf")
val FONT_WOFF: MediaType = MediaType(FONT, "woff")
val FONT_WOFF2: MediaType = MediaType(FONT, "woff2")

val IMAGE_GIF: MediaType = MediaType(IMAGE, "gif")
val IMAGE_JPEG: MediaType = MediaType(IMAGE, "jpeg")
val IMAGE_PNG: MediaType = MediaType(IMAGE, "png")
val IMAGE_TIFF: MediaType = MediaType(IMAGE, "tiff")
val IMAGE_SVG: MediaType = MediaType(IMAGE, "svg+xml")
val IMAGE_ICO: MediaType = MediaType(IMAGE, "vnd.microsoft.icon")
val IMAGE_WEBP: MediaType = MediaType(IMAGE, "webp")
val IMAGE_AVIF: MediaType = MediaType(IMAGE, "avif")

val MULTIPART_ALTERNATIVE: MediaType = MediaType(MULTIPART, "alternative")
val MULTIPART_APPLEDOUBLE: MediaType = MediaType(MULTIPART, "appledouble")
val MULTIPART_DIGEST: MediaType = MediaType(MULTIPART, "digest")
val MULTIPART_MIXED: MediaType = MediaType(MULTIPART, "mixed")
val MULTIPART_PARALLEL: MediaType = MediaType(MULTIPART, "parallel")

val TEXT_CSV: MediaType = MediaType(TEXT, "csv")
val TEXT_X_JAVA_PROPERTIES: MediaType = MediaType(TEXT, "x-java-properties")
val TEXT_JAVASCRIPT: MediaType = MediaType(TEXT, "javascript")
val TEXT_CSS: MediaType = MediaType(TEXT, "css")
val TEXT_HTML: MediaType = MediaType(TEXT, "html")
val TEXT_MARKDOWN: MediaType = MediaType(TEXT, "markdown")
val TEXT_PLAIN: MediaType = MediaType(TEXT, "plain")
val TEXT_RICHTEXT: MediaType = MediaType(TEXT, "richtext")
val TEXT_TAB_SEPARATED_VALUES: MediaType = MediaType(TEXT, "tab-separated-values")
val TEXT_CALENDAR: MediaType = MediaType(TEXT, "calendar")
val TEXT_EVENT_STREAM: MediaType = MediaType(TEXT, "event-stream")

val VIDEO_MPEG: MediaType = MediaType(VIDEO, "mpeg")
val VIDEO_QUICKTIME: MediaType = MediaType(VIDEO, "quicktime")
val VIDEO_X_MSVIDEO: MediaType = MediaType(VIDEO, "x-msvideo")
val VIDEO_MP4: MediaType = MediaType(VIDEO, "mp4")
val VIDEO_OGG: MediaType = MediaType(VIDEO, "ogg")
val VIDEO_WEBM: MediaType = MediaType(VIDEO, "webm")

val DEFAULT_MEDIA_TYPE: MediaType = APPLICATION_OCTET_STREAM

internal val MEDIA_TYPES_EXTENSIONS: Map<String, MediaType> = mapOf(
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
    "ai" to APPLICATION_POSTSCRIPT,
    "eps" to APPLICATION_POSTSCRIPT,
    "ps" to APPLICATION_POSTSCRIPT,
    "rtf" to APPLICATION_RTF,
    "z" to APPLICATION_COMPRESS,
    "csh" to APPLICATION_X_CSH,
    "gtar" to APPLICATION_X_GTAR,
    "gz" to APPLICATION_GZIP,
    "gzip" to APPLICATION_GZIP,
    "latex" to APPLICATION_X_LATEX,
    "sh" to APPLICATION_X_SH,
    "tar" to APPLICATION_X_TAR,
    "tcl" to APPLICATION_X_TCL,
    "tex" to APPLICATION_X_TEX,
    "texinfo" to APPLICATION_X_TEXINFO,
    "texi" to APPLICATION_X_TEXINFO,
    "zip" to APPLICATION_ZIP,
    "epub" to APPLICATION_EPUB_ZIP,
    "jar" to APPLICATION_JAVA_ARCHIVE,
    "ogx" to APPLICATION_OGG,
    "rar" to APPLICATION_RAR,
    "7z" to APPLICATION_7Z,
    "bz" to APPLICATION_BZIP,
    "bz2" to APPLICATION_BZIP2,
    "cda" to APPLICATION_CDF,
    "php" to APPLICATION_PHP,
    "xhtml" to APPLICATION_XHTML,
    "webmanifest" to APPLICATION_WEB_MANIFEST,
    "toml" to APPLICATION_TOML,
    "au" to AUDIO_BASIC,
    "snd" to AUDIO_BASIC,
    "mpga" to AUDIO_MPEG,
    "mp2" to AUDIO_MPEG,
    "mp2a" to AUDIO_MPEG,
    "mp3" to AUDIO_MPEG,
    "m2a" to AUDIO_MPEG,
    "m3a" to AUDIO_MPEG,
    "wav" to AUDIO_WAV,
    "aac" to AUDIO_AAC,
    "mid" to AUDIO_MIDI,
    "midi" to AUDIO_MIDI,
    "oga" to AUDIO_OGG,
    "opus" to AUDIO_OPUS,
    "weba" to AUDIO_WEBM,
    "otf" to FONT_OTF,
    "ttf" to FONT_TTF,
    "woff" to FONT_WOFF,
    "woff2" to FONT_WOFF2,
    "gif" to IMAGE_GIF,
    "jpeg" to IMAGE_JPEG,
    "jpg" to IMAGE_JPEG,
    "png" to IMAGE_PNG,
    "tiff" to IMAGE_TIFF,
    "tif" to IMAGE_TIFF,
    "svg" to IMAGE_SVG,
    "ico" to IMAGE_ICO,
    "webp" to IMAGE_WEBP,
    "avif" to IMAGE_AVIF,
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
    "mpeg" to VIDEO_MPEG,
    "mpg" to VIDEO_MPEG,
    "mpe" to VIDEO_MPEG,
    "m1v" to VIDEO_MPEG,
    "m2v" to VIDEO_MPEG,
    "qt" to VIDEO_QUICKTIME,
    "mov" to VIDEO_QUICKTIME,
    "avi" to VIDEO_X_MSVIDEO,
    "mp4" to VIDEO_MP4,
    "ogv" to VIDEO_OGG,
    "webm" to VIDEO_WEBM,
)

internal val EXTENSIONS_BY_MEDIA: Map<MediaType, List<String>> = MEDIA_TYPES_EXTENSIONS.entries
    .groupBy { it.value }
    .mapValues {
        it.value.map { entry -> entry.key }
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
    MEDIA_TYPES_EXTENSIONS[extension]

fun mediaTypeOf(uri: URI): MediaType =
    mediaTypeOfOrNull(uri) ?: error("Media type not found for: '$uri' URI")

fun mediaTypeOf(url: URL): MediaType =
    mediaTypeOfOrNull(url) ?: error("Media type not found for: '$url' URL")

fun mediaTypeOf(file: File): MediaType =
    mediaTypeOfOrNull(file) ?: error("Media type not found for: '$file' file")

fun mediaTypeOf(path: Path): MediaType =
    mediaTypeOfOrNull(path) ?: error("Media type not found for: '$path' extension")

fun mediaTypeOf(extension: String): MediaType =
    mediaTypeOfOrNull(extension) ?: error("Media type not found for: '$extension' extension")

fun extensionsOf(mediaType: MediaType): List<String> =
    EXTENSIONS_BY_MEDIA[mediaType] ?: emptyList()

private fun pathExtension(path: String): String =
    path.substringAfterLast('.')
