package com.hexagonkt.http

import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class HttpTest {

    @Test fun `Parse strips spaces` () {
        assert(parseQueryParameters("a =1&b & c &d = e") == mapOf(
            "a" to listOf("1"),
            "b" to listOf(""),
            "c" to listOf(""),
            "d" to listOf("e")
        ))
    }

    @Test fun `Parse key only query parameters return correct data` () {
        assert(parseQueryParameters("a=1&b&c&d=e") == mapOf(
            "a" to listOf("1"),
            "b" to listOf(""),
            "c" to listOf(""),
            "d" to listOf("e")
        ))
    }

    @Test fun `Parse multiple keys return list of values` () {
        assert(parseQueryParameters("a=1&b&c&d=e&a=2&b=c") == mapOf(
            "a" to listOf("1", "2"),
            "b" to listOf("", "c"),
            "c" to listOf(""),
            "d" to listOf("e")
        ))
    }

    @Test fun `Parse multiple empty values` () {
        assert(parseQueryParameters("a=") == mapOf(
            "a" to listOf("")
        ))
        assert(parseQueryParameters("a=&b=") == mapOf(
            "a" to listOf(""),
            "b" to listOf("")
        ))
        assert(parseQueryParameters("a=&b=&c") == mapOf(
            "a" to listOf(""),
            "b" to listOf(""),
            "c" to listOf("")
        ))
    }

    @Test fun `Parse key only` () {
        assert(parseQueryParameters("ab") == mapOf(
            "ab" to listOf("")
        ))
    }

    @Test fun `Parse value only` () {
        assert(parseQueryParameters(" =ab").none())
    }

    @Test fun `Parse white space only`() {
        assert(parseQueryParameters("    ").none())
    }

    @Test fun `HTTP date has the correct format`() {
        assert(httpDate(LocalDateTime.of(2018, 1, 1, 0, 0)) == "Mon, 1 Jan 2018 00:00:00 GMT")
    }

    @Test fun `URL encoding and decoding works properly`() {
        val text = "\\:"
        val encoded = text.urlEncode()
        assert(encoded == "%5C%3A")
        assert(encoded.urlDecode() == text)
    }
}
