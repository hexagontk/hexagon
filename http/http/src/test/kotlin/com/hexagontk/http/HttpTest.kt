package com.hexagontk.http

import com.hexagontk.core.GMT_ZONE
import com.hexagontk.core.Platform
import com.hexagontk.http.model.Field
import com.hexagontk.http.model.Parameters
import com.hexagontk.http.model.HttpField
import org.junit.jupiter.api.Test
import java.time.*
import kotlin.test.assertEquals

internal class HttpTest {

    @Test fun `Format query string with empty keys`() {
        assertEquals("", formatQueryString(Parameters(Field("", 1))))
        assertEquals("", formatQueryString(Parameters(Field(" ", 1))))
    }

    @Test fun `Basic auth is encoded correctly`() {
        assertEquals("YTo", basicAuth("a"))
        assertEquals("YTpi", basicAuth("a", "b"))
    }

    @Test fun `Format query string`() {
        fun testParseFormat(expected: String, queryString: String) {
            assertEquals(expected, formatQueryString(parseQueryString(queryString)))
        }

        testParseFormat("a=1&b&c&d=e", "a =1&b & c &d = e")
        testParseFormat("a=1&b&c&d=e", "a=1&b&c&d=e")
        testParseFormat("a=1&b&c&d=e&a=2&b=c", "a=1&b&c&d=e&a=2&b=c")
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

    @Test fun `Parse handles encoded characters` () {
        val expected = Parameters(
            Field("a ", "1"),
            Field("b ", ""),
            Field(" c ", ""),
            Field("d ", " e"),
        )

        assertEquals(expected, parseQueryString("a%20=1&b%20&%20c%20&d%20=%20e"))
        assertEquals(expected, parseQueryString("a+=1&b+&+c+&d+=+e"))
    }

    @Test fun `Parse strips spaces` () {
        assertEquals(Parameters(
            Field("a", "1"),
            Field("b", ""),
            Field("c", ""),
            Field("d", "e"),
        ), parseQueryString("a =1&b & c &d = e"))
    }

    @Test fun `Parse key only query parameters return correct data` () {
        assertEquals(Parameters(
            Field("a", "1"),
            Field("b", ""),
            Field("c", ""),
            Field("d", "e"),
        ), parseQueryString("a=1&b&c&d=e"))
    }

    @Test fun `Parse multiple keys return list of values` () {
        assertEquals(Parameters(
            Field("a", "1"),
            Field("b", ""),
            Field("c", ""),
            Field("d", "e"),
            Field("a", "2"),
            Field("b", "c"),
        ), parseQueryString("a=1&b&c&d=e&a=2&b=c"))
    }

    @Test fun `Parse multiple empty values` () {
        assertEquals(Parameters(Field("a", "")), parseQueryString("a="))
        assertEquals(Parameters(
            Field("c", ""),
            Field("c", ""),
        ), parseQueryString("c=&c"))
        assertEquals(Parameters(
            Field("a", ""),
            Field("b", "")
        ), parseQueryString("a=&b="))
        assertEquals(Parameters(
            Field("a", ""),
            Field("b", ""),
            Field("c", ""),
        ), parseQueryString("a=&b=&c"))
    }

    @Test fun `Parse key only` () {
        assertEquals(Parameters(Field("ab", "")), parseQueryString("ab"))
    }

    @Test fun `Parse value only` () {
        assert(parseQueryString(" =ab").isEmpty())
    }

    @Test fun `Parse white space only`() {
        assert(parseQueryString("    ").isEmpty())
    }

    @Test fun `HTTP date has the correct format`() {
        val localDateTime = LocalDateTime.of(2018, 1, 1, 0, 0)
        val gmtDateTime =
            ZonedDateTime.of(localDateTime, Platform.zoneId).withZoneSameInstant(GMT_ZONE)
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
