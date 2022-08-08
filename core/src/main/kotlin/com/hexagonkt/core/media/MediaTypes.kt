package com.hexagonkt.core.media

import java.io.File
import java.net.URL

fun parseMediaType(fullType: String): MediaType {
    val groupType = fullType.split("/")
    require(groupType.size == 2) { "Media type format must be <type>/<subtype>: $fullType" }

    val groupText = groupType.first().uppercase()
    val group = if (groupText == "*") MediaTypeGroup.ANY else MediaTypeGroup.valueOf(groupText)
    val type = groupType.last()
    return CustomMedia(group, type)
}

val mediaTypeFormat: Regex = """\*|([\w+.-]+)""".toRegex()

val defaultMediaType: MediaType = ApplicationMedia.OCTET_STREAM

fun mediaTypeOfOrNull(url: URL): MediaType? =
    mediaTypeOfOrNull(pathExtension(url.file))

fun mediaTypeOfOrNull(file: File): MediaType? =
    mediaTypeOfOrNull(file.extension)

fun mediaTypeOfOrNull(extension: String): MediaType? =
    extensions[extension]

fun mediaTypeOf(url: URL): MediaType =
    mediaTypeOfOrNull(url) ?: error("Media type not found for: '$url' URL")

fun mediaTypeOf(file: File): MediaType =
    mediaTypeOfOrNull(file) ?: error("Media type not found for: '$file' file")

fun mediaTypeOf(extension: String): MediaType =
    mediaTypeOfOrNull(extension) ?: error("Media type not found for: '$extension' extension")

private fun pathExtension(path: String): String =
    path.substringAfterLast('.')

internal val extensions: Map<String, MediaType> = mapOf(
    "avro" to ApplicationMedia.AVRO,
    "cbor" to ApplicationMedia.CBOR,
    "json" to ApplicationMedia.JSON,
    "yaml" to ApplicationMedia.YAML,
    "yml" to ApplicationMedia.YAML,
    "xml" to ApplicationMedia.XML,
    "bin" to ApplicationMedia.OCTET_STREAM,
    "lha" to ApplicationMedia.OCTET_STREAM,
    "lzh" to ApplicationMedia.OCTET_STREAM,
    "exe" to ApplicationMedia.OCTET_STREAM,
    "so" to ApplicationMedia.OCTET_STREAM,
    "class" to ApplicationMedia.OCTET_STREAM,
    "pdf" to ApplicationMedia.PDF,
    "ai" to ApplicationMedia.POSTSCRIPT,
    "eps" to ApplicationMedia.POSTSCRIPT,
    "ps" to ApplicationMedia.POSTSCRIPT,
    "rtf" to ApplicationMedia.RTF,
    "z" to ApplicationMedia.COMPRESS,
    "csh" to ApplicationMedia.X_CSH,
    "gtar" to ApplicationMedia.X_GTAR,
    "gz" to ApplicationMedia.GZIP,
    "gzip" to ApplicationMedia.GZIP,
    "latex" to ApplicationMedia.X_LATEX,
    "sh" to ApplicationMedia.X_SH,
    "tar" to ApplicationMedia.X_TAR,
    "tcl" to ApplicationMedia.X_TCL,
    "tex" to ApplicationMedia.X_TEX,
    "texinfo" to ApplicationMedia.X_TEXINFO,
    "texi" to ApplicationMedia.X_TEXINFO,
    "zip" to ApplicationMedia.ZIP,
    "epub" to ApplicationMedia.EPUB_ZIP,
    "jar" to ApplicationMedia.JAVA_ARCHIVE,
    "ogx" to ApplicationMedia.OGG,
    "rar" to ApplicationMedia.RAR,
    "7z" to ApplicationMedia.A7Z,
    "bz" to ApplicationMedia.BZIP,
    "bz2" to ApplicationMedia.BZIP2,
    "cda" to ApplicationMedia.CDF,
    "php" to ApplicationMedia.PHP,
    "xhtml" to ApplicationMedia.XHTML,
    "webmanifest" to ApplicationMedia.WEB_MANIFEST,
    "toml" to ApplicationMedia.TOML,
    "au" to AudioMedia.BASIC,
    "snd" to AudioMedia.BASIC,
    "mpga" to AudioMedia.MPEG,
    "mp2" to AudioMedia.MPEG,
    "mp2a" to AudioMedia.MPEG,
    "mp3" to AudioMedia.MPEG,
    "m2a" to AudioMedia.MPEG,
    "m3a" to AudioMedia.MPEG,
    "wav" to AudioMedia.WAV,
    "aac" to AudioMedia.AAC,
    "mid" to AudioMedia.MIDI,
    "midi" to AudioMedia.MIDI,
    "oga" to AudioMedia.OGG,
    "opus" to AudioMedia.OPUS,
    "weba" to AudioMedia.WEBM,
    "otf" to FontMedia.OTF,
    "ttf" to FontMedia.TTF,
    "woff" to FontMedia.WOFF,
    "woff2" to FontMedia.WOFF2,
    "gif" to ImageMedia.GIF,
    "jpeg" to ImageMedia.JPEG,
    "jpg" to ImageMedia.JPEG,
    "png" to ImageMedia.PNG,
    "tiff" to ImageMedia.TIFF,
    "tif" to ImageMedia.TIFF,
    "svg" to ImageMedia.SVG,
    "ico" to ImageMedia.ICO,
    "webp" to ImageMedia.WEBP,
    "csv" to TextMedia.CSV,
    "properties" to TextMedia.X_JAVA_PROPERTIES,
    "js" to TextMedia.JAVASCRIPT,
    "css" to TextMedia.CSS,
    "html" to TextMedia.HTML,
    "htm" to TextMedia.HTML,
    "md" to TextMedia.MARKDOWN,
    "txt" to TextMedia.PLAIN,
    "log" to TextMedia.PLAIN,
    "rtx" to TextMedia.RICHTEXT,
    "tsv" to TextMedia.TAB_SEPARATED_VALUES,
    "ics" to TextMedia.CALENDAR,
    "mpeg" to VideoMedia.MPEG,
    "mpg" to VideoMedia.MPEG,
    "mpe" to VideoMedia.MPEG,
    "m1v" to VideoMedia.MPEG,
    "m2v" to VideoMedia.MPEG,
    "qt" to VideoMedia.QUICKTIME,
    "mov" to VideoMedia.QUICKTIME,
    "avi" to VideoMedia.X_MSVIDEO,
    "mp4" to VideoMedia.MP4,
    "ogv" to VideoMedia.OGG,
    "webm" to VideoMedia.WEBM,
)
