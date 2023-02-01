package com.hexagonkt.core.media

import com.hexagonkt.core.media.MediaTypeGroup.*
import kotlin.test.Test
import kotlin.IllegalArgumentException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

internal class MediaTypeTest {

    @Test fun `MediaType init checks are disabled in production mode`() {
        assertFailsWith<IllegalArgumentException> { CustomMedia(TEXT, "&plain") }
    }

    @Test fun `Media types can be fetched from their full type`() {
        assertSame(MediaType("application/avro"), MediaType.fullTypes["application/avro"])
        assertEquals(MediaType("application/example"), CustomMedia(APPLICATION, "example"))
    }

    @Test fun `Media types without extensions are correct`() {
        assertEquals("multipart/alternative", MultipartMedia.ALTERNATIVE.fullType)
        assertEquals("multipart/appledouble", MultipartMedia.APPLEDOUBLE.fullType)
        assertEquals("multipart/digest", MultipartMedia.DIGEST.fullType)
        assertEquals("multipart/mixed", MultipartMedia.MIXED.fullType)
        assertEquals("multipart/parallel", MultipartMedia.PARALLEL.fullType)

        assertEquals("text/event-stream", TextMedia.EVENT_STREAM.fullType)
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

    @Test fun `Not found extension returns the default media type`() {
        assertEquals(defaultMediaType, MediaType["___"])
    }
}
