package com.hexagontk.core.media

import com.hexagontk.core.media.MediaTypeGroup.*
import com.hexagontk.core.urlOf
import org.junit.jupiter.api.Test
import java.io.File
import java.net.URI
import kotlin.IllegalArgumentException
import kotlin.IllegalStateException
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

internal class MediaTypesTest {

    @Test fun `Media types utilities`() {
        // mediaTypes
        // TODO
        // mediaTypes
    }

    @Test fun `Parse correct media types`() {
        MediaType("*/*").apply {
            assertEquals(ANY, group)
            assertEquals("any", group.text)
            assertEquals("*", type)
            assertEquals("*/*", fullType)
        }

        MediaType("text/plain").apply {
            assertEquals(TEXT, group)
            assertEquals("text", group.text)
            assertEquals("plain", type)
        }

        MediaType("application/octet-stream").apply {
            assertEquals(APPLICATION, group)
            assertEquals("application", group.text)
            assertEquals("octet-stream", type)
        }
    }

    @Test fun `Parse incorrect media types`() {
        assertFailsWith<IllegalArgumentException> { MediaType("text plain") }
        assertFailsWith<IllegalArgumentException> { MediaType("text/plain/string") }
        assertFailsWith<IllegalArgumentException> { MediaType("document/plain") }
        assertFailsWith<IllegalArgumentException> { MediaType("text/ plain") }
    }

    @Test fun `Media types of files and URLs can be retrieved`() {
        assertEquals(APPLICATION_TOML, mediaTypeOfOrNull(URI("http://localhost/file.toml")))
        assertEquals(TEXT_PLAIN, mediaTypeOfOrNull(urlOf("http://localhost/file.txt")))
        assertEquals(APPLICATION_JSON, mediaTypeOfOrNull(urlOf("http://localhost/file.json")))
        assertEquals(APPLICATION_TOML, mediaTypeOf(URI("http://localhost/file.toml")))
        assertEquals(TEXT_PLAIN, mediaTypeOf(urlOf("http://localhost/file.txt")))
        assertEquals(APPLICATION_JSON, mediaTypeOf(urlOf("http://localhost/file.json")))
        assertEquals(APPLICATION_AVRO, mediaTypeOf("avro"))
        assertNull(mediaTypeOfOrNull(urlOf("http://localhost/file")))
        assertNull(mediaTypeOfOrNull(urlOf("http://localhost/file.foo")))

        assertEquals(TEXT_HTML, mediaTypeOfOrNull(File("file.html")))
        assertEquals(TEXT_HTML, mediaTypeOfOrNull(File("file.htm")))
        assertEquals(APPLICATION_YAML, mediaTypeOfOrNull(File("file.yaml")))
        assertEquals(APPLICATION_YAML, mediaTypeOfOrNull(File("file.yml")))
        assertEquals(TEXT_HTML, mediaTypeOf(File("file.html")))
        assertEquals(TEXT_HTML, mediaTypeOf(File("file.htm")))
        assertEquals(APPLICATION_YAML, mediaTypeOf(File("file.yaml")))
        assertEquals(APPLICATION_YAML, mediaTypeOf(File("file.yml")))
        assertNull(mediaTypeOfOrNull(File("file")))
        assertNull(mediaTypeOfOrNull(File("file.baz")))
        assertNull(mediaTypeOfOrNull(URI("http://localhost/file.baz")))
        assertNull(mediaTypeOfOrNull(urlOf("http://localhost/file.baz")))
        assertEquals(TEXT_HTML, mediaTypeOfOrNull(Path.of("file.html")))
        assertEquals(TEXT_HTML, mediaTypeOfOrNull(Path.of("file.htm")))
        assertEquals(APPLICATION_YAML, mediaTypeOfOrNull(Path.of("file.yaml")))
        assertEquals(APPLICATION_YAML, mediaTypeOfOrNull(Path.of("file.yml")))
        assertEquals(TEXT_HTML, mediaTypeOf(Path.of("file.html")))
        assertEquals(TEXT_HTML, mediaTypeOf(Path.of("file.htm")))
        assertEquals(APPLICATION_YAML, mediaTypeOf(Path.of("file.yaml")))
        assertEquals(APPLICATION_YAML, mediaTypeOf(Path.of("file.yml")))
        assertNull(mediaTypeOfOrNull(Path.of("file")))
        assertNull(mediaTypeOfOrNull(Path.of("file.baz")))
    }

    @Test fun `Exception is thrown if the media type is not found`() {
        assertEquals(
            "Media type not found for: 'http://host/f' URL",
            assertFailsWith<IllegalStateException> { mediaTypeOf(urlOf("http://host/f")) }.message
        )

        assertEquals(
            "Media type not found for: 'http://host/f.foo' URL",
            assertFailsWith<IllegalStateException> { mediaTypeOf(urlOf("http://host/f.foo")) }.message
        )

        assertEquals(
            "Media type not found for: 'file' file",
            assertFailsWith<IllegalStateException> { mediaTypeOf(File("file")) }.message
        )

        assertEquals(
            "Media type not found for: 'file.baz' file",
            assertFailsWith<IllegalStateException> { mediaTypeOf(File("file.baz")) }.message
        )

        assertEquals(
            "Media type not found for: 'file' path",
            assertFailsWith<IllegalStateException> { mediaTypeOf(Path.of("file")) }.message
        )

        assertEquals(
            "Media type not found for: 'file.baz' path",
            assertFailsWith<IllegalStateException> { mediaTypeOf(Path.of("file.baz")) }.message
        )

        assertEquals(
            "Media type not found for: '' extension",
            assertFailsWith<IllegalStateException> { mediaTypeOf("") }.message
        )

        assertEquals(
            "Media type not found for: 'baz' extension",
            assertFailsWith<IllegalStateException> { mediaTypeOf("baz") }.message
        )
    }
}
