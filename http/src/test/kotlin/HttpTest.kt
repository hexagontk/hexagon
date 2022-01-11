package com.hexagonkt.http

import com.hexagonkt.core.disableChecks
import com.hexagonkt.core.multiMapOf
import com.hexagonkt.core.multiMapOfLists
import org.junit.jupiter.api.Test
import java.math.BigInteger
import java.time.*
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

internal class HttpTest {

    @Test fun `Format query string`() {
        fun testParseFormat(expected: String, queryString: String) {
            assertEquals(expected, formatQueryString(parseQueryString(queryString)))
        }

        testParseFormat("a=1&b&c&d=e", "a =1&b & c &d = e")
        testParseFormat("a=1&b&c&d=e", "a=1&b&c&d=e")
        testParseFormat("a=1&a=2&b&b=c&c&d=e", "a=1&b&c&d=e&a=2&b=c")
        testParseFormat("a", "a=")
        testParseFormat("a&b", "a=&b=")
        testParseFormat("c&c", "c=&c")
        testParseFormat("a&b&c", "a=&b=&c")
        testParseFormat("ab", "ab")
        testParseFormat("", " =ab")
        testParseFormat("", "    ")
        testParseFormat("a+=+b+", "a+=+b+")
        testParseFormat("a+=+b+", "a%20=%20b%20")
    }

    @Test fun `Basic types can be converted to byte arrays to be sent as bodies`() {
        assertContentEquals("text".toByteArray(), bodyToBytes("text"))
        assertContentEquals("text".toByteArray(), bodyToBytes("text".toByteArray()))
        assertContentEquals(BigInteger.valueOf(42).toByteArray(), bodyToBytes(42))
        assertContentEquals(BigInteger.valueOf(1_234_567L).toByteArray(), bodyToBytes(1_234_567L))
        assertFailsWith<IllegalStateException> { bodyToBytes(LocalDate.now())  }
    }

    @Test fun `Invalid header names fail validation check` () {
        val invalidHeaderError =
            "Header names must be lower-case and contain only letters, digits or '-':"
        val forbiddenHeaders = listOf("Content-Type", "accept_all")
            .map { multiMapOf(it to "value") }

        forbiddenHeaders.forEach {
            val e = assertFailsWith<IllegalStateException> { checkHeaders(it) }
            val header = it.keys.first()
            assertTrue(e.message?.contains("'$header'") ?: false)
            assertTrue(e.message?.contains(invalidHeaderError) ?: false)
        }

        disableChecks = true
        forbiddenHeaders.forEach { checkHeaders(it) }
        disableChecks = false
    }

    @Test fun `Check headers fails when using reserved headers when not in production mode` () {
        val forbiddenHeaders = listOf("content-type", "accept", "set-cookie")
            .map { multiMapOf(it to "value") }

        forbiddenHeaders.forEach {
            val e = assertFailsWith<IllegalStateException> { checkHeaders(it) }
            val header = it.keys.first()
            assertTrue(e.message?.contains("'$header'") ?: false)
        }

        disableChecks = true
        forbiddenHeaders.forEach { checkHeaders(it) }
        disableChecks = false
    }

    @Test fun `Check headers list all invalid headers on error` () {
        val headers = multiMapOf(
            "content-type" to "1",
            "accept" to "1",
            "set-cookie" to "1",
        )

        val e = assertFailsWith<IllegalStateException> { checkHeaders(headers) }
        headers.keys
            .map { it.lowercase() }
            .forEach { assertTrue(e.message?.contains("'$it'") ?: false) }
    }

    @Test fun `Check headers succeed on regular headers` () {
        listOf("referrer", "origin")
            .map { multiMapOf(it to "value") }
            .forEach { checkHeaders(it) }
    }

    @Test fun `Parse handles encoded characters` () {
        val expected = multiMapOf(
            "a " to "1",
            "b " to "",
            " c " to "",
            "d " to " e"
        )

        assertEquals(expected, parseQueryString("a%20=1&b%20&%20c%20&d%20=%20e"))
        assertEquals(expected, parseQueryString("a+=1&b+&+c+&d+=+e"))
    }

    @Test fun `Parse strips spaces` () {
        assertEquals(multiMapOf(
            "a" to "1",
            "b" to "",
            "c" to "",
            "d" to "e"
        ), parseQueryString("a =1&b & c &d = e"))
    }

    @Test fun `Parse key only query parameters return correct data` () {
        assertEquals(multiMapOf(
            "a" to "1",
            "b" to "",
            "c" to "",
            "d" to "e"
        ), parseQueryString("a=1&b&c&d=e"))
    }

    @Test fun `Parse multiple keys return list of values` () {
        assertEquals(multiMapOfLists(
            "a" to listOf("1", "2"),
            "b" to listOf("", "c"),
            "c" to listOf(""),
            "d" to listOf("e")
        ), parseQueryString("a=1&b&c&d=e&a=2&b=c"))
    }

    @Test fun `Parse multiple empty values` () {
        assertEquals(multiMapOf("a" to ""), parseQueryString("a="))
        assertEquals(multiMapOf("a" to "", "b" to ""), parseQueryString("a=&b="))
        assertEquals(multiMapOfLists("c" to listOf("", "")), parseQueryString("c=&c"))
        assertEquals(multiMapOf(
            "a" to "",
            "b" to "",
            "c" to ""
        ), parseQueryString("a=&b=&c"))
    }

    @Test fun `Parse key only` () {
        assertEquals(multiMapOf("ab" to ""), parseQueryString("ab"))
    }

    @Test fun `Parse value only` () {
        assert(parseQueryString(" =ab").none())
    }

    @Test fun `Parse white space only`() {
        assert(parseQueryString("    ").none())
    }

    @Test fun `HTTP date has the correct format`() {
        val httpTimeStamp = LocalDateTime.of(2018, 1, 1, 0, 0).toHttpFormat()
        assertEquals("Mon, 1 Jan 2018 00:00:00 GMT", httpTimeStamp)
    }

    @Test fun `URL encoding and decoding works properly`() {
        val text = "\\:"
        val encoded = text.urlEncode()
        assertEquals("%5C%3A", encoded)
        assertEquals(text, encoded.urlDecode())
    }
}
