package com.hexagonkt.core.media

import com.hexagonkt.core.media.MediaTypeGroup.*
import org.junit.jupiter.api.Test
import kotlin.IllegalArgumentException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

internal class MediaTypeTest {

    @Test fun `MediaType init checks are disabled in production mode`() {
        assertFailsWith<IllegalArgumentException> { MediaType(TEXT, "&plain") }
    }

    @Test fun `Media types can be fetched from their full type`() {
        assertSame(MediaType("application/avro"), MediaType.fullTypes["application/avro"])
        assertEquals(MediaType("application/example"), MediaType(APPLICATION, "example"))
    }

    @Test fun `Media types without extensions are correct`() {
        assertEquals("multipart/alternative", MULTIPART_ALTERNATIVE.fullType)
        assertEquals("multipart/appledouble", MULTIPART_APPLEDOUBLE.fullType)
        assertEquals("multipart/digest", MULTIPART_DIGEST.fullType)
        assertEquals("multipart/mixed", MULTIPART_MIXED.fullType)
        assertEquals("multipart/parallel", MULTIPART_PARALLEL.fullType)

        assertEquals("text/event-stream", TEXT_EVENT_STREAM.fullType)
    }

    @Test fun `Media types can be fetched from their file extensions`() {
        assertEquals("application/avro", MediaType["avro"].fullType)
        assertEquals("application/cbor", MediaType["cbor"].fullType)
        assertEquals("application/json", MediaType["json"].fullType)
        assertEquals("application/yaml", MediaType["yaml"].fullType)
        assertEquals("application/yaml", MediaType["yml"].fullType)
        assertEquals("application/xml", MediaType["xml"].fullType)
        assertEquals("application/octet-stream", MediaType["bin"].fullType)
        assertEquals("application/octet-stream", MediaType["lha"].fullType)
        assertEquals("application/octet-stream", MediaType["lzh"].fullType)
        assertEquals("application/octet-stream", MediaType["exe"].fullType)
        assertEquals("application/octet-stream", MediaType["so"].fullType)
        assertEquals("application/octet-stream", MediaType["class"].fullType)
        assertEquals("application/compress", MediaType["z"].fullType)
        assertEquals("application/gzip", MediaType["gz"].fullType)
        assertEquals("application/gzip", MediaType["gzip"].fullType)
        assertEquals("application/pdf", MediaType["pdf"].fullType)
        assertEquals("application/postscript", MediaType["ai"].fullType)
        assertEquals("application/postscript", MediaType["eps"].fullType)
        assertEquals("application/postscript", MediaType["ps"].fullType)
        assertEquals("application/rtf", MediaType["rtf"].fullType)
        assertEquals("application/x-csh", MediaType["csh"].fullType)
        assertEquals("application/x-gtar", MediaType["gtar"].fullType)
        assertEquals("application/x-latex", MediaType["latex"].fullType)
        assertEquals("application/x-sh", MediaType["sh"].fullType)
        assertEquals("application/x-tar", MediaType["tar"].fullType)
        assertEquals("application/x-tcl", MediaType["tcl"].fullType)
        assertEquals("application/x-tex", MediaType["tex"].fullType)
        assertEquals("application/x-texinfo", MediaType["texinfo"].fullType)
        assertEquals("application/x-texinfo", MediaType["texi"].fullType)
        assertEquals("application/zip", MediaType["zip"].fullType)
        assertEquals("application/epub+zip", MediaType["epub"].fullType)
        assertEquals("application/java-archive", MediaType["jar"].fullType)
        assertEquals("application/ogg", MediaType["ogx"].fullType)
        assertEquals("application/vnd.rar", MediaType["rar"].fullType)
        assertEquals("application/x-7z-compressed", MediaType["7z"].fullType)
        assertEquals("application/x-bzip", MediaType["bz"].fullType)
        assertEquals("application/x-bzip2", MediaType["bz2"].fullType)
        assertEquals("application/x-cdf", MediaType["cda"].fullType)
        assertEquals("application/x-httpd-php", MediaType["php"].fullType)
        assertEquals("application/xhtml+xml", MediaType["xhtml"].fullType)
        assertEquals("application/manifest+json", MediaType["webmanifest"].fullType)
        assertEquals("application/toml", MediaType["toml"].fullType)

        assertEquals("audio/basic", MediaType["au"].fullType)
        assertEquals("audio/basic", MediaType["snd"].fullType)
        assertEquals("audio/mpeg", MediaType["mpga"].fullType)
        assertEquals("audio/mpeg", MediaType["mp2"].fullType)
        assertEquals("audio/mpeg", MediaType["mp2a"].fullType)
        assertEquals("audio/mpeg", MediaType["mp3"].fullType)
        assertEquals("audio/mpeg", MediaType["m2a"].fullType)
        assertEquals("audio/mpeg", MediaType["m3a"].fullType)
        assertEquals("audio/wav", MediaType["wav"].fullType)
        assertEquals("audio/aac", MediaType["aac"].fullType)
        assertEquals("audio/midi", MediaType["mid"].fullType)
        assertEquals("audio/midi", MediaType["midi"].fullType)
        assertEquals("audio/ogg", MediaType["oga"].fullType)
        assertEquals("audio/opus", MediaType["opus"].fullType)
        assertEquals("audio/webm", MediaType["weba"].fullType)

        assertEquals("font/otf", MediaType["otf"].fullType)
        assertEquals("font/ttf", MediaType["ttf"].fullType)
        assertEquals("font/woff", MediaType["woff"].fullType)
        assertEquals("font/woff2", MediaType["woff2"].fullType)

        assertEquals("image/gif", MediaType["gif"].fullType)
        assertEquals("image/jpeg", MediaType["jpeg"].fullType)
        assertEquals("image/jpeg", MediaType["jpg"].fullType)
        assertEquals("image/png", MediaType["png"].fullType)
        assertEquals("image/tiff", MediaType["tiff"].fullType)
        assertEquals("image/tiff", MediaType["tif"].fullType)
        assertEquals("image/svg+xml", MediaType["svg"].fullType)
        assertEquals("image/vnd.microsoft.icon", MediaType["ico"].fullType)
        assertEquals("image/webp", MediaType["webp"].fullType)
        assertEquals("image/avif", MediaType["avif"].fullType)

        assertEquals("text/csv", MediaType["csv"].fullType)
        assertEquals("text/x-java-properties", MediaType["properties"].fullType)
        assertEquals("text/javascript", MediaType["js"].fullType)
        assertEquals("text/css", MediaType["css"].fullType)
        assertEquals("text/html", MediaType["html"].fullType)
        assertEquals("text/html", MediaType["htm"].fullType)
        assertEquals("text/markdown", MediaType["md"].fullType)
        assertEquals("text/plain", MediaType["txt"].fullType)
        assertEquals("text/plain", MediaType["log"].fullType)
        assertEquals("text/richtext", MediaType["rtx"].fullType)
        assertEquals("text/tab-separated-values", MediaType["tsv"].fullType)
        assertEquals("text/calendar", MediaType["ics"].fullType)

        assertEquals("video/mpeg", MediaType["mpeg"].fullType)
        assertEquals("video/mpeg", MediaType["mpg"].fullType)
        assertEquals("video/mpeg", MediaType["mpe"].fullType)
        assertEquals("video/mpeg", MediaType["m1v"].fullType)
        assertEquals("video/mpeg", MediaType["m2v"].fullType)
        assertEquals("video/quicktime", MediaType["qt"].fullType)
        assertEquals("video/quicktime", MediaType["mov"].fullType)
        assertEquals("video/x-msvideo", MediaType["avi"].fullType)
    }

    @Test fun `Media type extensions can be retrieved`() {
        assertEquals(listOf("avro"), extensionsOf(APPLICATION_AVRO))
        assertEquals(listOf("cbor"), extensionsOf(APPLICATION_CBOR))
        assertEquals(listOf("json"), extensionsOf(APPLICATION_JSON))
        assertEquals(listOf("yaml", "yml"), extensionsOf(APPLICATION_YAML))
        assertEquals(listOf("xml"), extensionsOf(APPLICATION_XML))
        assertEquals(listOf("bin", "lha", "lzh", "exe", "so", "class"), extensionsOf(APPLICATION_OCTET_STREAM))
        assertEquals(listOf("z"), extensionsOf(APPLICATION_COMPRESS))
        assertEquals(listOf("gz", "gzip"), extensionsOf(APPLICATION_GZIP))
        assertEquals(listOf("pdf"), extensionsOf(APPLICATION_PDF))
        assertEquals(listOf("ai", "eps", "ps"), extensionsOf(APPLICATION_POSTSCRIPT))
        assertEquals(listOf("rtf"), extensionsOf(APPLICATION_RTF))
        assertEquals(listOf("csh"), extensionsOf(APPLICATION_X_CSH))
        assertEquals(listOf("gtar"), extensionsOf(APPLICATION_X_GTAR))
        assertEquals(listOf("latex"), extensionsOf(APPLICATION_X_LATEX))
        assertEquals(listOf("sh"), extensionsOf(APPLICATION_X_SH))
        assertEquals(listOf("tar"), extensionsOf(APPLICATION_X_TAR))
        assertEquals(listOf("tcl"), extensionsOf(APPLICATION_X_TCL))
        assertEquals(listOf("tex"), extensionsOf(APPLICATION_X_TEX))
        assertEquals(listOf("texinfo", "texi"), extensionsOf(APPLICATION_X_TEXINFO))
        assertEquals(listOf("zip"), extensionsOf(APPLICATION_ZIP))
        assertEquals(listOf("epub"), extensionsOf(APPLICATION_EPUB_ZIP))
        assertEquals(listOf("jar"), extensionsOf(APPLICATION_JAVA_ARCHIVE))
        assertEquals(listOf("ogx"), extensionsOf(APPLICATION_OGG))
        assertEquals(listOf("rar"), extensionsOf(APPLICATION_RAR))
        assertEquals(listOf("7z"), extensionsOf(APPLICATION_7Z))
        assertEquals(listOf("bz"), extensionsOf(APPLICATION_BZIP))
        assertEquals(listOf("bz2"), extensionsOf(APPLICATION_BZIP2))
        assertEquals(listOf("cda"), extensionsOf(APPLICATION_CDF))
        assertEquals(listOf("php"), extensionsOf(APPLICATION_PHP))
        assertEquals(listOf("xhtml"), extensionsOf(APPLICATION_XHTML))
        assertEquals(listOf("webmanifest"), extensionsOf(APPLICATION_WEB_MANIFEST))
        assertEquals(listOf("toml"), extensionsOf(APPLICATION_TOML))

        assertEquals(listOf("au", "snd"), extensionsOf(AUDIO_BASIC))
        assertEquals(listOf("mpga", "mp2", "mp2a", "mp3", "m2a", "m3a"), extensionsOf(AUDIO_MPEG))
        assertEquals(listOf("wav"), extensionsOf(AUDIO_WAV))
        assertEquals(listOf("aac"), extensionsOf(AUDIO_AAC))
        assertEquals(listOf("mid", "midi"), extensionsOf(AUDIO_MIDI))
        assertEquals(listOf("oga"), extensionsOf(AUDIO_OGG))
        assertEquals(listOf("opus"), extensionsOf(AUDIO_OPUS))
        assertEquals(listOf("weba"), extensionsOf(AUDIO_WEBM))

        assertEquals(listOf("otf"), extensionsOf(FONT_OTF))
        assertEquals(listOf("ttf"), extensionsOf(FONT_TTF))
        assertEquals(listOf("woff"), extensionsOf(FONT_WOFF))
        assertEquals(listOf("woff2"), extensionsOf(FONT_WOFF2))

        assertEquals(listOf("gif"), extensionsOf(IMAGE_GIF))
        assertEquals(listOf("jpeg", "jpg"), extensionsOf(IMAGE_JPEG))
        assertEquals(listOf("png"), extensionsOf(IMAGE_PNG))
        assertEquals(listOf("tiff", "tif"), extensionsOf(IMAGE_TIFF))

        assertEquals(listOf("svg"), extensionsOf(IMAGE_SVG))
        assertEquals(listOf("ico"), extensionsOf(IMAGE_ICO))

        assertEquals(listOf("webp"), extensionsOf(IMAGE_WEBP))
        assertEquals(listOf("avif"), extensionsOf(IMAGE_AVIF))

        assertEquals(listOf("csv"), extensionsOf(TEXT_CSV))
        assertEquals(listOf("properties"), extensionsOf(TEXT_X_JAVA_PROPERTIES))
        assertEquals(listOf("js"), extensionsOf(TEXT_JAVASCRIPT))
        assertEquals(listOf("css"), extensionsOf(TEXT_CSS))
        assertEquals(listOf("html", "htm"), extensionsOf(TEXT_HTML))
        assertEquals(listOf("md"), extensionsOf(TEXT_MARKDOWN))
        assertEquals(listOf("txt", "log"), extensionsOf(TEXT_PLAIN))
        assertEquals(listOf("rtx"), extensionsOf(TEXT_RICHTEXT))
        assertEquals(listOf("tsv"), extensionsOf(TEXT_TAB_SEPARATED_VALUES))
        assertEquals(listOf("ics"), extensionsOf(TEXT_CALENDAR))

        assertEquals(listOf("mpeg", "mpg", "mpe", "m1v", "m2v"), extensionsOf(VIDEO_MPEG))
        assertEquals(listOf("qt", "mov"), extensionsOf(VIDEO_QUICKTIME))
        assertEquals(listOf("avi"), extensionsOf(VIDEO_X_MSVIDEO))
    }

    @Test fun `Not found extension returns the default media type`() {
        assertEquals(DEFAULT_MEDIA_TYPE, MediaType["___"])
    }
}
