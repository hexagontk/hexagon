package com.hexagonkt.http.model

import com.hexagonkt.core.media.MediaTypeGroup.TEXT
import com.hexagonkt.core.media.CustomMedia
import com.hexagonkt.core.media.TextMedia.HTML
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

internal class HttpPartTest {

    private val fullPart = HttpPart(
        name = "name",
        body = "content",
        headers = HttpFields(Header("header", "value")),
        contentType = ContentType(CustomMedia(TEXT, "plain")),
        size = "content".length.toLong(),
        submittedFileName = "filename",
    )

    private val minimumPart = HttpPart("key", "value")

    @Test fun `Part body can be read as a string`() {
        assertEquals("text", HttpPart("string", "text").bodyString())
        assertEquals("bytes", HttpPart("array", "bytes".toByteArray(), "fileName").bodyString())
        assertEquals("42", HttpPart("object", 42).bodyString())
    }

    @Test fun `Full part contains expected values`() {
        assertEquals("name", fullPart.name)
        assertEquals("content", fullPart.body)
        assertEquals(HttpFields(Header("header", "value")), fullPart.headers)
        assertEquals("text/plain", fullPart.contentType?.mediaType?.fullType)
        assertEquals("content".length.toLong(), fullPart.size)
        assertEquals("filename", fullPart.submittedFileName)
    }

    @Test fun `Minimum part contains expected values`() {
        assertEquals("key", minimumPart.name)
        assertEquals("value", minimumPart.body)
        assertEquals(HttpFields(), minimumPart.headers)
        assertNull(minimumPart.contentType)
        assertEquals("value".length.toLong(), minimumPart.size)
        assertNull(minimumPart.submittedFileName)
    }

    @Test fun `Utility constructors work properly`() {
        HttpPart("key", "value").let {
            assertEquals("key", it.name)
            assertEquals("value", it.body)
            assertEquals(HttpFields(), it.headers)
            assertNull(it.contentType)
            assertEquals("value".toByteArray().size.toLong(), it.size)
            assertNull(it.submittedFileName)
        }

        HttpPart("key", "value".toByteArray(), "fileName").let {
            assertEquals("key", it.name)
            assertEquals("value", String(it.body as ByteArray))
            assertEquals(HttpFields(), it.headers)
            assertNull(it.contentType)
            assertEquals("value".toByteArray().size.toLong(), it.size)
            assertEquals("fileName", it.submittedFileName)
        }
    }

    @Test fun `Part comparison works ok`() {
        val part = HttpPart("key", "value")
        assertEquals(part, part)
        assertEquals(HttpPart("key", "value"), HttpPart("key", "value"))
        assertFalse(part.equals(""))
        assertNotEquals(part, part.copy(name = "other"))
        assertNotEquals(part, part.copy(body = "other"))
        assertNotEquals(part, part.copy(headers = HttpFields(Header("a", "b"))))
        assertNotEquals(part, part.copy(contentType = ContentType(HTML)))
        assertNotEquals(part, part.copy(size = 10))
        assertNotEquals(part, part.copy(submittedFileName = "other"))
        assertEquals(part.hashCode(), HttpPart("key", "value").hashCode())
    }
}
