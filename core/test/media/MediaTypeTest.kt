package com.hexagontk.core.media

import com.hexagontk.core.media.MediaTypeGroup.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import kotlin.IllegalArgumentException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

@TestInstance(PER_CLASS)
internal class MediaTypeTest {

    val mediaTypes: Map<String, MediaType> = mediaTypesExtensions

    val audioBasic = MediaType(AUDIO, "basic")
    val audioMpeg = MediaType(AUDIO, "mpeg")
    val audioWav = MediaType(AUDIO, "wav")
    val audioAac = MediaType(AUDIO, "aac")
    val audioMidi = MediaType(AUDIO, "midi")
    val audioOgg = MediaType(AUDIO, "ogg")
    val audioOpus = MediaType(AUDIO, "opus")
    val audioWebm = MediaType(AUDIO, "webm")

    val fontOtf = MediaType(FONT, "otf")
    val fontTtf = MediaType(FONT, "ttf")
    val fontWoff = MediaType(FONT, "woff")
    val fontWoff2 = MediaType(FONT, "woff2")

    val imageGif = MediaType(IMAGE, "gif")
    val imageJpeg = MediaType(IMAGE, "jpeg")
    val imagePng = MediaType(IMAGE, "png")
    val imageTiff = MediaType(IMAGE, "tiff")
    val imageSvg = MediaType(IMAGE, "svg+xml")
    val imageIco = MediaType(IMAGE, "vnd.microsoft.icon")
    val imageWebp = MediaType(IMAGE, "webp")
    val imageAvif = MediaType(IMAGE, "avif")

    val videoMpeg = MediaType(VIDEO, "mpeg")
    val videoQuicktime = MediaType(VIDEO, "quicktime")
    val videoXMsvideo = MediaType(VIDEO, "x-msvideo")
    val videoMp4 = MediaType(VIDEO, "mp4")
    val videoOgg = MediaType(VIDEO, "ogg")
    val videoWebm = MediaType(VIDEO, "webm")

    @BeforeAll fun setUp() {
        mediaTypesExtensions += createExtraMediaTypes()
    }

    @AfterAll fun shutDown() {
        mediaTypesExtensions = mediaTypes
    }

    @Test fun `MediaType init checks are disabled in production mode`() {
        assertFailsWith<IllegalArgumentException> { MediaType(TEXT, "&plain") }
    }

    @Test fun `Media types can be fetched from their full type`() {
        assertSame(MediaType("application/avro"), MediaType.fullTypes["application/avro"])
        assertEquals(MediaType("application/example"), MediaType(APPLICATION, "example"))
    }

    @Test fun `Media types without extensions are correct`() {
        assertEquals("*/*", ANY_MEDIA.fullType)

        assertEquals("multipart/alternative", MULTIPART_ALTERNATIVE.fullType)
        assertEquals("multipart/digest", MULTIPART_DIGEST.fullType)
        assertEquals("multipart/mixed", MULTIPART_MIXED.fullType)
        assertEquals("multipart/parallel", MULTIPART_PARALLEL.fullType)
        assertEquals("multipart/form-data", MULTIPART_FORM_DATA.fullType)

        assertEquals("text/event-stream", TEXT_EVENT_STREAM.fullType)

        assertEquals("application/x-www-form-urlencoded", APPLICATION_FORM_URLENCODED.fullType)
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
        assertEquals("application/rtf", MediaType["rtf"].fullType)
        assertEquals("application/x-tar", MediaType["tar"].fullType)
        assertEquals("application/zip", MediaType["zip"].fullType)
        assertEquals("application/java-archive", MediaType["jar"].fullType)
        assertEquals("application/ogg", MediaType["ogx"].fullType)
        assertEquals("application/vnd.rar", MediaType["rar"].fullType)
        assertEquals("application/x-7z-compressed", MediaType["7z"].fullType)
        assertEquals("application/x-bzip", MediaType["bz"].fullType)
        assertEquals("application/x-bzip2", MediaType["bz2"].fullType)
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
        assertEquals(listOf("rtf"), extensionsOf(APPLICATION_RTF))
        assertEquals(listOf("tar"), extensionsOf(APPLICATION_X_TAR))
        assertEquals(listOf("zip"), extensionsOf(APPLICATION_ZIP))
        assertEquals(listOf("jar"), extensionsOf(APPLICATION_JAVA_ARCHIVE))
        assertEquals(listOf("ogx"), extensionsOf(APPLICATION_OGG))
        assertEquals(listOf("rar"), extensionsOf(APPLICATION_RAR))
        assertEquals(listOf("7z"), extensionsOf(APPLICATION_7Z))
        assertEquals(listOf("bz"), extensionsOf(APPLICATION_BZIP))
        assertEquals(listOf("bz2"), extensionsOf(APPLICATION_BZIP2))
        assertEquals(listOf("php"), extensionsOf(APPLICATION_PHP))
        assertEquals(listOf("xhtml"), extensionsOf(APPLICATION_XHTML))
        assertEquals(listOf("webmanifest"), extensionsOf(APPLICATION_WEB_MANIFEST))
        assertEquals(listOf("toml"), extensionsOf(APPLICATION_TOML))

        assertEquals(listOf("au", "snd"), extensionsOf(audioBasic))
        assertEquals(listOf("mpga", "mp2", "mp2a", "mp3", "m2a", "m3a"), extensionsOf(audioMpeg))
        assertEquals(listOf("wav"), extensionsOf(audioWav))
        assertEquals(listOf("aac"), extensionsOf(audioAac))
        assertEquals(listOf("mid", "midi"), extensionsOf(audioMidi))
        assertEquals(listOf("oga"), extensionsOf(audioOgg))
        assertEquals(listOf("opus"), extensionsOf(audioOpus))
        assertEquals(listOf("weba"), extensionsOf(audioWebm))

        assertEquals(listOf("otf"), extensionsOf(fontOtf))
        assertEquals(listOf("ttf"), extensionsOf(fontTtf))
        assertEquals(listOf("woff"), extensionsOf(fontWoff))
        assertEquals(listOf("woff2"), extensionsOf(fontWoff2))

        assertEquals(listOf("gif"), extensionsOf(imageGif))
        assertEquals(listOf("jpeg", "jpg"), extensionsOf(imageJpeg))
        assertEquals(listOf("png"), extensionsOf(imagePng))
        assertEquals(listOf("tiff", "tif"), extensionsOf(imageTiff))

        assertEquals(listOf("svg"), extensionsOf(imageSvg))
        assertEquals(listOf("ico"), extensionsOf(imageIco))

        assertEquals(listOf("webp"), extensionsOf(imageWebp))
        assertEquals(listOf("avif"), extensionsOf(imageAvif))

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

        assertEquals(listOf("mpeg", "mpg", "mpe", "m1v", "m2v"), extensionsOf(videoMpeg))
        assertEquals(listOf("qt", "mov"), extensionsOf(videoQuicktime))
        assertEquals(listOf("avi"), extensionsOf(videoXMsvideo))
    }

    @Test fun `Not found extension returns the default media type`() {
        assertEquals(DEFAULT_MEDIA_TYPE, MediaType["___"])
    }

    private fun createExtraMediaTypes(): Map<String, MediaType> =
        mapOf(
            "au" to audioBasic,
            "snd" to audioBasic,
            "mpga" to audioMpeg,
            "mp2" to audioMpeg,
            "mp2a" to audioMpeg,
            "mp3" to audioMpeg,
            "m2a" to audioMpeg,
            "m3a" to audioMpeg,
            "wav" to audioWav,
            "aac" to audioAac,
            "mid" to audioMidi,
            "midi" to audioMidi,
            "oga" to audioOgg,
            "opus" to audioOpus,
            "weba" to audioWebm,
            "otf" to fontOtf,
            "ttf" to fontTtf,
            "woff" to fontWoff,
            "woff2" to fontWoff2,
            "gif" to imageGif,
            "jpeg" to imageJpeg,
            "jpg" to imageJpeg,
            "png" to imagePng,
            "tiff" to imageTiff,
            "tif" to imageTiff,
            "svg" to imageSvg,
            "ico" to imageIco,
            "webp" to imageWebp,
            "avif" to imageAvif,
            "mpeg" to videoMpeg,
            "mpg" to videoMpeg,
            "mpe" to videoMpeg,
            "m1v" to videoMpeg,
            "m2v" to videoMpeg,
            "qt" to videoQuicktime,
            "mov" to videoQuicktime,
            "avi" to videoXMsvideo,
            "mp4" to videoMp4,
            "ogv" to videoOgg,
            "webm" to videoWebm,
        )
}
