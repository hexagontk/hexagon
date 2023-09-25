package com.hexagonkt.http

import com.hexagonkt.core.Jvm
import com.hexagonkt.http.model.Header
import com.hexagonkt.http.model.QueryParameters
import com.hexagonkt.http.model.Headers
import com.hexagonkt.http.model.QueryParameter
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

    @Test fun `Check headers fails when using reserved headers when not in production mode` () {
        val forbiddenHeaders = listOf("content-type", "accept", "set-cookie")
            .map { Headers(Header(it, "value")) }

        forbiddenHeaders.forEach {
            val e = assertFailsWith<IllegalStateException> { checkHeaders(it) }
            val header = it.httpFields.keys.first()
            assertTrue(e.message?.contains("'$header'") ?: false)
        }
    }

    @Test fun `Check headers list all invalid headers on error` () {
        val headers = Headers(
            Header("content-type", "1"),
            Header("accept", "1"),
            Header("set-cookie", "1"),
        )

        val e = assertFailsWith<IllegalStateException> { checkHeaders(headers) }
        headers.httpFields.keys
            .map { it.lowercase() }
            .forEach { assertTrue(e.message?.contains("'$it'") ?: false) }
    }

    @Test fun `Check headers succeed on regular headers` () {
        listOf("referrer", "origin")
            .map { Headers(Header(it, "value")) }
            .forEach { checkHeaders(it) }
    }

    @Test fun `Parse handles encoded characters` () {
        val expected = QueryParameters(
            QueryParameter("a ", "1"),
            QueryParameter("b ", ""),
            QueryParameter(" c ", ""),
            QueryParameter("d ", " e"),
        )

        assertEquals(expected, parseQueryString("a%20=1&b%20&%20c%20&d%20=%20e"))
        assertEquals(expected, parseQueryString("a+=1&b+&+c+&d+=+e"))
    }

    @Test fun `Parse strips spaces` () {
        assertEquals(QueryParameters(
            QueryParameter("a", "1"),
            QueryParameter("b", ""),
            QueryParameter("c", ""),
            QueryParameter("d", "e"),
        ), parseQueryString("a =1&b & c &d = e"))
    }

    @Test fun `Parse key only query parameters return correct data` () {
        assertEquals(QueryParameters(
            QueryParameter("a", "1"),
            QueryParameter("b", ""),
            QueryParameter("c", ""),
            QueryParameter("d", "e"),
        ), parseQueryString("a=1&b&c&d=e"))
    }

    @Test fun `Parse multiple keys return list of values` () {
        assertEquals(QueryParameters(
            QueryParameter("a", "1", "2"),
            QueryParameter("b", "", "c"),
            QueryParameter("c", ""),
            QueryParameter("d", "e"),
        ), parseQueryString("a=1&b&c&d=e&a=2&b=c"))
    }

    @Test fun `Parse multiple empty values` () {
        assertEquals(QueryParameters(QueryParameter("a", "")), parseQueryString("a="))
        assertEquals(QueryParameters(QueryParameter("c", "", "")), parseQueryString("c=&c"))
        assertEquals(QueryParameters(
            QueryParameter("a", ""),
            QueryParameter("b", "")
        ), parseQueryString("a=&b="))
        assertEquals(QueryParameters(
            QueryParameter("a", ""),
            QueryParameter("b", ""),
            QueryParameter("c", ""),
        ), parseQueryString("a=&b=&c"))
    }

    @Test fun `Parse key only` () {
        assertEquals(QueryParameters(QueryParameter("ab", "")), parseQueryString("ab"))
    }

    @Test fun `Parse value only` () {
        assert(parseQueryString(" =ab").isEmpty())
    }

    @Test fun `Parse white space only`() {
        assert(parseQueryString("    ").isEmpty())
    }

    @Test fun `HTTP date has the correct format`() {
        val localDateTime = LocalDateTime.of(2018, 1, 1, 0, 0)
        val gmtDateTime = ZonedDateTime.of(localDateTime, Jvm.zoneId).withZoneSameInstant(GMT_ZONE)
        assertEquals(gmtDateTime.format(HTTP_DATE_FORMATTER), localDateTime.toHttpFormat())

        val instant = localDateTime.toInstant(ZoneOffset.UTC)
        assertEquals("Mon, 1 Jan 2018 00:00:00 GMT", instant.toHttpFormat())
    }

    @Test fun `URL encoding and decoding works properly`() {
        val text = "\\:"
        val encoded = text.urlEncode()
        assertEquals("%5C%3A", encoded)
        assertEquals(text, encoded.urlDecode())
    }
}
