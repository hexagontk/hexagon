package com.hexagonkt.http

import org.testng.annotations.Test
import java.time.LocalDateTime

@Test class HttpTest {

    @Test fun `Parse strips spaces` () {
        assert(parseQueryParameters("a =1&b & c &d = e") == mapOf(
            "a" to "1",
            "b" to "",
            "c" to "",
            "d" to "e"
        ))
    }

    @Test fun `Parse key only query parameters return correct data` () {
        assert(parseQueryParameters("a=1&b&c&d=e") == mapOf(
            "a" to "1",
            "b" to "",
            "c" to "",
            "d" to "e"
        ))
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
