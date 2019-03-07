package com.hexagonkt.helpers

import org.testng.annotations.Test

@Test class StringsTest {

    @Test fun `Filter returns the given string if no parameters are set` () {
        val template = "User #{user}"

        assert (template.filterVars(mapOf<Any, Any> ()) == template)
        assert (template.filterVars() == template)
    }

    @Test fun `Filter returns the same string if no variables are defined in it` () {
        val template = "User no vars"

        assert (template.filterVars () == template)
        assert (template.filterVars ("vars" to "value") == template)
        assert (template.filterVars (mapOf<Any, Any> ()) == template)
    }

    @Test fun `Filter returns the same string if variable values are not found` () {
        val template = "User #{user}"

        assert (template.filterVars ("key" to "value") == template)
    }

    @Test fun `Filter ignores empty parameters` () {
        val result = "#{email}: User #{user} aka #{user} <#{email}>".filterVars (
            "" to "John",
            "email" to "john@example.co"
        )

        assert (result == "john@example.co: User #{user} aka #{user} <john@example.co>")
    }

    @Test fun `Filter replaces all occurences of variables with their values` () {
        val result = "#{email}: User #{user} aka #{user} <#{email}>".filterVars (
            "user" to "John",
            "email" to "john@example.co"
        )

        assert (result == "john@example.co: User John aka John <john@example.co>")
    }

    @Test fun `Converting empty text to camel case fails` () {
        assert ("".snakeToCamel () == "")
    }

    @Test fun `Converting valid snake case texts to camel case succeed` () {
        assert ("alfa_beta".snakeToCamel () == "alfaBeta")
        assert ("alfa__beta".snakeToCamel () == "alfaBeta")
        assert ("alfa___beta".snakeToCamel () == "alfaBeta")
    }

    @Test fun `Converting valid camel case texts to snake case succeed` () {
        assert ("alfaBeta".camelToSnake () == "alfa_beta")
    }

    @Test fun `Banner logs the proper message`() {
        var banner = "alfa line".banner()
        assert(banner.contains("alfa line"))
        assert(banner.contains("*********"))

        banner = "".banner()
        assert(banner == eol + eol)

        banner =
            """alfa
            looong line
            beta
            tango""".trimIndent().trim().banner()
        assert(banner.contains("alfa"))
        assert(banner.contains("beta"))
        assert(banner.contains("tango"))
        assert(banner.contains("looong line"))
        assert(banner.contains("***********"))
    }

    @Test fun `Normalize works as expected`() {
        val striped = "áéíóúñçÁÉÍÓÚÑÇ".stripAccents()
        assert(striped == "aeiouncAEIOUNC")
    }

    @Test fun `Utf8 returns proper characters`() {
        assert(utf8(0xF0, 0x9F, 0x91, 0x8D) == "👍")
        assert(utf8(0xF0, 0x9F, 0x91, 0x8E) == "👎")
    }
}
