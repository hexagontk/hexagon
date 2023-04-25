package com.hexagonkt.core.media

import com.hexagonkt.core.media.MediaTypeGroup.*
import kotlin.test.Test
import java.io.File
import java.net.URI
import kotlin.IllegalArgumentException
import kotlin.IllegalStateException
import java.net.URL
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

internal class MediaTypesTest {

    @Test fun `Parse correct media types`() {
        MediaType("*/*").apply {
            assertEquals(ANY, group)
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
        assertEquals(TEXT_PLAIN, mediaTypeOfOrNull(URL("http://localhost/file.txt")))
        assertEquals(APPLICATION_JSON, mediaTypeOfOrNull(URL("http://localhost/file.json")))
        assertEquals(APPLICATION_TOML, mediaTypeOf(URI("http://localhost/file.toml")))
        assertEquals(TEXT_PLAIN, mediaTypeOf(URL("http://localhost/file.txt")))
        assertEquals(APPLICATION_JSON, mediaTypeOf(URL("http://localhost/file.json")))
        assertEquals(APPLICATION_AVRO, mediaTypeOf("avro"))
        assertNull(mediaTypeOfOrNull(URL("http://localhost/file")))
        assertNull(mediaTypeOfOrNull(URL("http://localhost/file.foo")))

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
        assertNull(mediaTypeOfOrNull(URL("http://localhost/file.baz")))
    }

    @Test fun `Exception is thrown if the media type is not found`() {
        assertEquals(
            "Media type not found for: 'http://host/f' URL",
            assertFailsWith<IllegalStateException> { mediaTypeOf(URL("http://host/f")) }.message
        )

        assertEquals(
            "Media type not found for: 'http://host/f.foo' URL",
            assertFailsWith<IllegalStateException> { mediaTypeOf(URL("http://host/f.foo")) }.message
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
            "Media type not found for: '' extension",
            assertFailsWith<IllegalStateException> { mediaTypeOf("") }.message
        )

        assertEquals(
            "Media type not found for: 'baz' extension",
            assertFailsWith<IllegalStateException> { mediaTypeOf("baz") }.message
        )
    }
}
