package com.hexagonkt.helpers

import org.testng.annotations.Test

/**
 * TODO Test fields and lines with spaces
 */
class CsvTest {
    @Test fun `Test empty line`() {
        assert(parseLine("").isEmpty())
    }

    @Test fun `Test no quote`() {
        val result = parseLine("10,AU,Australia")

        assert(result.isNotEmpty())
        assert(result.size == 3)
        assert(result[0] == "10")
        assert(result[1] == "AU")
        assert(result[2] == "Australia")
    }

    @Test fun `Test no quote but double quotes in column`() {
        val result = parseLine("""10,AU,Aus""tralia""")

        assert(result.isNotEmpty())
        assert(result.size == 3)
        assert(result[0] == "10")
        assert(result[1] == "AU")
        assert(result[2] == "Aus\"tralia")
    }

    @Test fun `Test parse whitespace`() {
        val result = parseLine("'10','AU','Australia'\r\n", customQuote = '\'')

        assert(result.isNotEmpty())
        assert(result.size == 3)
        assert(result[0] == "10")
        assert(result[1] == "AU")
        assert(result[2] == "Australia")
    }

    @Test fun `Test double quotes`() {
        val result = parseLine(""""10","AU","Australia"""")

        assert(result.isNotEmpty())
        assert(result.size == 3)
        assert(result[0] == "10")
        assert(result[1] == "AU")
        assert(result[2] == "Australia")
    }

    @Test fun `Test double quotes but double quotes in column`() {
        val result = parseLine(""""10","AU","Aus""tralia"""")

        assert(result.isNotEmpty())
        assert(result.size == 3)
        assert(result[0] == "10")
        assert(result[1] == "AU")
        assert(result[2] == "Aus\"tralia")
    }

    @Test fun `Test double quotes but comma in column`() {
        val result = parseLine(""""10","AU","Aus,tralia"""")

        assert(result.isNotEmpty())
        assert(result.size == 3)
        assert(result[0] == "10")
        assert(result[1] == "AU")
        assert(result[2] == "Aus,tralia")
    }

    @Test fun `Test custom separator`() {
        val result = parseLine("10|AU|Australia", '|')

        assert(result.isNotEmpty())
        assert(result.size == 3)
        assert(result[0] == "10")
        assert(result[1] == "AU")
        assert(result[2] == "Australia")
    }

    @Test fun `Test custom separator and quote`() {
        val result = parseLine("'10'|'AU'|'Australia'", '|', '\'')

        assert(result.isNotEmpty())
        assert(result.size == 3)
        assert(result[0] == "10")
        assert(result[1] == "AU")
        assert(result[2] == "Australia")
    }

    @Test fun `Test custom separator and quote but custom quote in column`() {
        val result = parseLine("'10'|'AU'|'Aus|tralia'", '|', '\'')

        assert(result.isNotEmpty())
        assert(result.size == 3)
        assert(result[0] == "10")
        assert(result[1] == "AU")
        assert(result[2] == "Aus|tralia")
    }

    @Test fun `Test custom separator and quote but double quotes in column`() {
        val result = parseLine("'10'|'AU'|'Aus\"\"tralia'", '|', '\'')

        assert(result.isNotEmpty())
        assert(result.size == 3)
        assert(result[0] == "10")
        assert(result[1] == "AU")
        assert(result[2] == "Aus\"tralia")
    }
}
