package com.hexagonkt.core.media

import com.hexagonkt.core.media.MediaTypeGroup.*
import java.io.File
import java.net.URI
import java.net.URL
import java.nio.file.Path
import kotlin.io.path.extension

val MEDIA_TYPE_FORMAT: Regex by lazy { """\*|([\w+.-]+)""".toRegex() }

val APPLICATION_AVRO: MediaType by lazy { MediaType(APPLICATION, "avro") }
val APPLICATION_CBOR: MediaType by lazy { MediaType(APPLICATION, "cbor") }
val APPLICATION_JSON: MediaType by lazy { MediaType(APPLICATION, "json") }
val APPLICATION_YAML: MediaType by lazy { MediaType(APPLICATION, "yaml") }
val APPLICATION_XML: MediaType by lazy { MediaType(APPLICATION, "xml") }
val APPLICATION_GZIP: MediaType by lazy { MediaType(APPLICATION, "gzip") }
val APPLICATION_COMPRESS: MediaType by lazy { MediaType(APPLICATION, "compress") }
val APPLICATION_OCTET_STREAM: MediaType by lazy { MediaType(APPLICATION, "octet-stream") }
val APPLICATION_PDF: MediaType by lazy { MediaType(APPLICATION, "pdf") }
val APPLICATION_POSTSCRIPT: MediaType by lazy { MediaType(APPLICATION, "postscript") }
val APPLICATION_RTF: MediaType by lazy { MediaType(APPLICATION, "rtf") }
val APPLICATION_X_CSH: MediaType by lazy { MediaType(APPLICATION, "x-csh") }
val APPLICATION_X_GTAR: MediaType by lazy { MediaType(APPLICATION, "x-gtar") }
val APPLICATION_X_LATEX: MediaType by lazy { MediaType(APPLICATION, "x-latex") }
val APPLICATION_X_SH: MediaType by lazy { MediaType(APPLICATION, "x-sh") }
val APPLICATION_X_TAR: MediaType by lazy { MediaType(APPLICATION, "x-tar") }
val APPLICATION_X_TCL: MediaType by lazy { MediaType(APPLICATION, "x-tcl") }
val APPLICATION_X_TEX: MediaType by lazy { MediaType(APPLICATION, "x-tex") }
val APPLICATION_X_TEXINFO: MediaType by lazy { MediaType(APPLICATION, "x-texinfo") }
val APPLICATION_ZIP: MediaType by lazy { MediaType(APPLICATION, "zip") }
val APPLICATION_EPUB_ZIP: MediaType by lazy { MediaType(APPLICATION, "epub+zip") }
val APPLICATION_JAVA_ARCHIVE: MediaType by lazy { MediaType(APPLICATION, "java-archive") }
val APPLICATION_OGG: MediaType by lazy { MediaType(APPLICATION, "ogg") }
val APPLICATION_RAR: MediaType by lazy { MediaType(APPLICATION, "vnd.rar") }
val APPLICATION_XHTML: MediaType by lazy { MediaType(APPLICATION, "xhtml+xml") }
val APPLICATION_WEB_MANIFEST: MediaType by lazy { MediaType(APPLICATION, "manifest+json") }
val APPLICATION_TOML: MediaType by lazy { MediaType(APPLICATION, "toml") }
val APPLICATION_7Z: MediaType by lazy { MediaType(APPLICATION, "x-7z-compressed") }
val APPLICATION_BZIP: MediaType by lazy { MediaType(APPLICATION, "x-bzip") }
val APPLICATION_BZIP2: MediaType by lazy { MediaType(APPLICATION, "x-bzip2") }
val APPLICATION_CDF: MediaType by lazy { MediaType(APPLICATION, "x-cdf") }
val APPLICATION_PHP: MediaType by lazy { MediaType(APPLICATION, "x-httpd-php") }

val AUDIO_BASIC: MediaType by lazy { MediaType(AUDIO, "basic") }
val AUDIO_MPEG: MediaType by lazy { MediaType(AUDIO, "mpeg") }
val AUDIO_WAV: MediaType by lazy { MediaType(AUDIO, "wav") }
val AUDIO_AAC: MediaType by lazy { MediaType(AUDIO, "aac") }
val AUDIO_MIDI: MediaType by lazy { MediaType(AUDIO, "midi") }
val AUDIO_OGG: MediaType by lazy { MediaType(AUDIO, "ogg") }
val AUDIO_OPUS: MediaType by lazy { MediaType(AUDIO, "opus") }
val AUDIO_WEBM: MediaType by lazy { MediaType(AUDIO, "webm") }

val FONT_OTF: MediaType by lazy { MediaType(FONT, "otf") }
val FONT_TTF: MediaType by lazy { MediaType(FONT, "ttf") }
val FONT_WOFF: MediaType by lazy { MediaType(FONT, "woff") }
val FONT_WOFF2: MediaType by lazy { MediaType(FONT, "woff2") }

val IMAGE_GIF: MediaType by lazy { MediaType(IMAGE, "gif") }
val IMAGE_JPEG: MediaType by lazy { MediaType(IMAGE, "jpeg") }
val IMAGE_PNG: MediaType by lazy { MediaType(IMAGE, "png") }
val IMAGE_TIFF: MediaType by lazy { MediaType(IMAGE, "tiff") }
val IMAGE_SVG: MediaType by lazy { MediaType(IMAGE, "svg+xml") }
val IMAGE_ICO: MediaType by lazy { MediaType(IMAGE, "vnd.microsoft.icon") }
val IMAGE_WEBP: MediaType by lazy { MediaType(IMAGE, "webp") }
val IMAGE_AVIF: MediaType by lazy { MediaType(IMAGE, "avif") }

val MULTIPART_ALTERNATIVE: MediaType by lazy { MediaType(MULTIPART, "alternative") }
val MULTIPART_APPLEDOUBLE: MediaType by lazy { MediaType(MULTIPART, "appledouble") }
val MULTIPART_DIGEST: MediaType by lazy { MediaType(MULTIPART, "digest") }
val MULTIPART_MIXED: MediaType by lazy { MediaType(MULTIPART, "mixed") }
val MULTIPART_PARALLEL: MediaType by lazy { MediaType(MULTIPART, "parallel") }

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

val VIDEO_MPEG: MediaType by lazy { MediaType(VIDEO, "mpeg") }
val VIDEO_QUICKTIME: MediaType by lazy { MediaType(VIDEO, "quicktime") }
val VIDEO_X_MSVIDEO: MediaType by lazy { MediaType(VIDEO, "x-msvideo") }
val VIDEO_MP4: MediaType by lazy { MediaType(VIDEO, "mp4") }
val VIDEO_OGG: MediaType by lazy { MediaType(VIDEO, "ogg") }
val VIDEO_WEBM: MediaType by lazy { MediaType(VIDEO, "webm") }

val DEFAULT_MEDIA_TYPE: MediaType by lazy { APPLICATION_OCTET_STREAM }

internal val MEDIA_TYPES_EXTENSIONS: Map<String, MediaType> by lazy {
    mapOf(
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
}

internal val EXTENSIONS_BY_MEDIA: Map<MediaType, List<String>> by lazy {
    MEDIA_TYPES_EXTENSIONS.entries
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
    MEDIA_TYPES_EXTENSIONS[extension]

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
