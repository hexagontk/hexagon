package com.hexagonkt.http.model

import com.hexagonkt.core.media.TextMedia.PLAIN
import com.hexagonkt.core.disableChecks
import com.hexagonkt.http.parseContentType
import kotlin.test.Test
import kotlin.IllegalStateException
import java.nio.charset.Charset
import java.nio.charset.UnsupportedCharsetException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

internal class ContentTypeTest {

    @Test fun `Parse correct content types`() {
        parseContentType("text/plain").apply {
            assertEquals("text", mediaType.group.text)
            assertEquals("plain", mediaType.type)
            assertNull(boundary)
            assertNull(charset)
            assertNull(q)
            assertEquals("text/plain", text)
        }
        parseContentType("image/png;q=0.0").apply {
            assertEquals("image", mediaType.group.text)
            assertEquals("png", mediaType.type)
            assertNull(boundary)
            assertNull(charset)
            assertEquals(0.0, q)
            assertEquals("image/png;q=0.0", text)
        }
        parseContentType("image/png;q=1.0").apply {
            assertEquals("image", mediaType.group.text)
            assertEquals("png", mediaType.type)
            assertNull(boundary)
            assertNull(charset)
            assertEquals(1.0, q)
            assertEquals("image/png;q=1.0", text)
        }
        listOf("multipart/mixed;boundary=000", "multipart/mixed ; boundary = 000").forEach {
            parseContentType(it).apply {
                assertEquals("multipart", mediaType.group.text)
                assertEquals("mixed", mediaType.type)
                assertEquals("000", boundary)
                assertNull(charset)
                assertNull(q)
                assertEquals("multipart/mixed;boundary=000", text)
            }
        }
        listOf("text/html;charset=UTF-8", "text/html ; charset = UTF-8").forEach {
            parseContentType(it).apply {
                assertEquals("text", mediaType.group.text)
                assertEquals("html", mediaType.type)
                assertNull(boundary)
                assertEquals(Charsets.UTF_8, charset)
                assertNull(q)
                assertEquals("text/html;charset=UTF-8", text)
            }
        }
        listOf("image/png;q=0.5", "image/png ; q = 0.5").forEach {
            parseContentType(it).apply {
                assertEquals("image", mediaType.group.text)
                assertEquals("png", mediaType.type)
                assertNull(boundary)
                assertNull(charset)
                assertEquals(0.5, q)
                assertEquals("image/png;q=0.5", text)
            }
        }
    }

    @Test fun `Parse invalid content types`() {
        assertFailsWith<IllegalStateException> { parseContentType("text/plain;") }
        assertFailsWith<IllegalStateException> { parseContentType("text/plain;;") }
        assertFailsWith<IllegalStateException> { parseContentType("text/plain;param=val") }
        assertFailsWith<UnsupportedCharsetException> { parseContentType("text/plain;charset=foo") }
    }

    @Test fun `Build content types with more than one parameter fails`() {
        val charset = Charset.defaultCharset()
        assertFailsWith<IllegalArgumentException> { ContentType(PLAIN, "abc", charset, 0.1) }
        assertFailsWith<IllegalArgumentException> { ContentType(PLAIN, "abc", charset, null) }
        assertFailsWith<IllegalArgumentException> { ContentType(PLAIN, "abc", null, 0.1) }
        assertFailsWith<IllegalArgumentException> { ContentType(PLAIN, null, charset, 0.1) }
    }

    @Test fun `Build content types with invalid parameters fails`() {
        assertFailsWith<IllegalArgumentException> { ContentType(PLAIN, "") }
        assertFailsWith<IllegalArgumentException> { ContentType(PLAIN, " ") }
        assertFailsWith<IllegalArgumentException> { ContentType(PLAIN, q = -0.1) }
        assertFailsWith<IllegalArgumentException> { ContentType(PLAIN, q = 1.1) }
    }

    @Test fun `Checks are ignored in production mode`() {
        disableChecks = true

        val charset = Charset.defaultCharset()
        ContentType(PLAIN, "abc", charset, 0.1)
        ContentType(PLAIN, "abc", charset)
        ContentType(PLAIN, null, charset, 0.1)
        ContentType(PLAIN, "abc", null, 0.1)

        ContentType(PLAIN, "")
        ContentType(PLAIN, " ")
        ContentType(PLAIN, q = -0.1)
        ContentType(PLAIN, q = 1.1)

        disableChecks = false
    }

    @Test fun `HttpPart hashcode behaves as expected`() {
        fun part(contentType: ContentType?, file: String?) =
            HttpPart("n", "v", contentType = contentType, submittedFileName = file)

        val part1 = part(ContentType(PLAIN), null)
        val part2 = part(null, "sfn")
        val part3 = part(ContentType(PLAIN), "sfn")
        val part4 = part(null, null)

        assertEquals(part1.hashCode(), part(ContentType(PLAIN), null).hashCode())
        assertEquals(part2.hashCode(), part(null, "sfn").hashCode())
        assertEquals(part3.hashCode(), part(ContentType(PLAIN), "sfn").hashCode())
        assertEquals(part4.hashCode(), part(null, null).hashCode())
    }
}
