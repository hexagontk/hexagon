package com.hexagonkt.http

import org.testng.annotations.Test

@Test class HelpersTest {

    @Test fun `Parse key only query parameters return correct data` () {
        assert(parseQueryParameters("a=1&b&c&d=e") == mapOf(
            "a" to "1",
            "b" to "",
            "c" to "",
            "d" to "e"
        ))
    }
}
