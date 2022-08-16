package com.hexagonkt.core

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.text.prependIndent

internal class StringsTest {

    @Test fun `Find groups takes care of 'nulls'`() {
        val reEmpty = mockk<Regex>()
        every { reEmpty.find(any()) } returns null

        assert(reEmpty.findGroups("").isEmpty())

        val matchGroupCollection = mockk<MatchGroupCollection>()
        every { matchGroupCollection.size } returns 1
        every { matchGroupCollection.iterator() } returns listOf<MatchGroup?>(null).iterator()
        val matchResult = mockk<MatchResult>()
        every { matchResult.groups } returns matchGroupCollection
        val reNullGroup = mockk<Regex>()
        every { reNullGroup.find(any()) } returns matchResult

        assert(reNullGroup.findGroups("").isEmpty())
    }

    @Test fun `Data can be encoded and decoded from to base64`() {
        val data = "abcDEF"
        val base64Data = data.encodeToBase64()
        val decodedData = base64Data.decodeBase64()

        assertEquals(data, String(decodedData))
    }

    @Test fun `Filter variables returns the given string if no parameters are set`() {
        val template = "User #{user}"

        assertEquals(template, template.filterVars(mapOf<Any, Any>()))
        assertEquals(template, template.filterVars())
    }

    @Test fun `Filter variables returns the same string if no variables are defined in it`() {
        val template = "User no vars"

        assertEquals(template, template.filterVars())
        assertEquals(template, template.filterVars("vars" to "value"))
        assertEquals(template, template.filterVars(mapOf<Any, Any>()))
    }

    @Test fun `Filter variables returns the same string if variable values are not found`() {
        val template = "User #{user}"

        assertEquals(template, template.filterVars("key" to "value"))
    }

    @Test fun `Filter variables ignores empty parameters`() {
        val result = "{{email}}: User {{user}} aka {{user}} <{{email}}>".filterVars(
            null to "Void",
            "" to "John",
            "email" to "john@example.co"
        )

        assertEquals("john@example.co: User {{user}} aka {{user}} <john@example.co>", result)
    }

    @Test fun `Filter variables replaces all occurrences of variables with their values`() {
        val result = "{{email}}: User {{user}} aka {{user}} <{{email}}>".filterVars(
            "user" to "John",
            "email" to "john@example.co"
        )

        assertEquals("john@example.co: User John aka John <john@example.co>", result)
    }

    @Test fun `Filter returns the given string if no parameters are set`() {
        val template = "User #{user}"

        assertEquals(template, template.filter("#{", "}"))
    }

    @Test fun `Filter returns the same string if no variables are defined in it`() {
        val template = "User no vars"

        assertEquals(template, template.filter("#{", "}"))
        assertEquals(template, template.filter("#{", "}", "vars" to "value"))
    }

    @Test fun `Filter returns the same string if variable values are not found`() {
        val template = "User #{user}"

        assertEquals(template, template.filter("#{", "}", "key" to "value"))
    }

    @Test fun `Filter ignores empty parameters`() {
        val result = "{{email}}: User {{user}} aka {{user}} <{{email}}>".filter(
            "{{", "}}",
            "" to "John",
            "email" to "john@example.co"
        )

        assertEquals("john@example.co: User {{user}} aka {{user}} <john@example.co>", result)
    }

    @Test fun `Filter replaces all occurrences of variables with their values`() {
        val result = "{{email}}: User {{user}} aka {{user}} <{{email}}>".filter(
            "{{", "}}",
            "user" to "John",
            "email" to "john@example.co"
        )

        assertEquals("john@example.co: User John aka John <john@example.co>", result)
    }

    @Test fun `Converting empty text to camel case fails`() {
        assertEquals("", "".snakeToCamel())
    }

    @Test fun `Converting valid snake case texts to camel case succeed`() {
        assertEquals("alfaBeta", "alfa_beta".snakeToCamel())
        assertEquals("alfaBeta", "alfa__beta".snakeToCamel())
        assertEquals("alfaBeta", "alfa___beta".snakeToCamel())
    }

    @Test fun `Converting valid camel case texts to snake case succeed`() {
        assertEquals("alfa_beta", "alfaBeta".camelToSnake())
    }

    @Test fun `Banner logs the proper message`() {
        var banner = "alfa line".banner()
        assert(banner.contains("alfa line"))
        assert(banner.contains("*********"))

        banner = "".banner()
        assertEquals(eol + eol, banner)

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

        assertEquals(123, sequenceOf<Int>().maxOrElse(123))

        val banner1 = "foo".banner(">")
        assert(banner1.contains("foo"))
        assert(banner1.contains(">>>"))
    }

    @Test fun `toStream works as expected`() {
        val s = "alfa-beta-charlie"
        val striped = s.toStream().readAllBytes()
        assertContentEquals(striped, s.toByteArray())
    }

    @Test fun `Normalize works as expected`() {
        val striped = "áéíóúñçÁÉÍÓÚÑÇ".stripAccents()
        assertEquals("aeiouncAEIOUNC", striped)
    }

    @Test fun `Utf8 returns proper characters`() {
        assertEquals("👍", utf8(0xF0, 0x9F, 0x91, 0x8D))
        assertEquals("👎", utf8(0xF0, 0x9F, 0x91, 0x8E))
    }

    @Test fun `Indent works as expected`() {
        assertEquals("     text ", " text ".prependIndent())
        assertEquals(" text ", " text ".prependIndent(0))
        assertEquals(" text ", " text ".prependIndent(0, "·"))
        assertEquals("  text ", " text ".prependIndent(1))
        assertEquals(" text ", " text ".prependIndent(1, ""))
        assertEquals("· text ", " text ".prependIndent(1, "·"))
        assertEquals("·· text ", " text ".prependIndent(2, "·"))
        assertEquals("·* text ", " text ".prependIndent(1, "·*"))
        assertEquals("·*·* text ", " text ".prependIndent(2, "·*"))
        assertEquals("·*·*·*text ", "·*text ".prependIndent(2, "·*"))

        assertEquals("    line 1\n    line 2", "line 1\nline 2".prependIndent())
        assertEquals("line 1\nline 2", "line 1\nline 2".prependIndent(0))
        assertEquals("line 1\nline 2", "line 1\nline 2".prependIndent(0, "·"))
        assertEquals(" line 1\n line 2", "line 1\nline 2".prependIndent(1))
        assertEquals("line 1\nline 2", "line 1\nline 2".prependIndent(1, ""))
        assertEquals("·line 1\n·line 2", "line 1\nline 2".prependIndent(1, "·"))
        assertEquals("··line 1\n··line 2", "line 1\nline 2".prependIndent(2, "·"))
        assertEquals("·*line 1\n·*line 2", "line 1\nline 2".prependIndent(1, "·*"))
        assertEquals("·*·*line 1\n·*·*line 2", "line 1\nline 2".prependIndent(2, "·*"))
        assertEquals("·*·*·*line 1\n·*·*·*line 2", "·*line 1\n·*line 2".prependIndent(2, "·*"))
    }

    @Test fun `ANSI testing`() {
        val message = "${Ansi.RED_BG}${Ansi.BRIGHT_WHITE}${Ansi.UNDERLINE}ANSI${Ansi.RESET} normal"
        val noAnsiMessage = message.stripAnsi()
        assertNotEquals(message, noAnsiMessage)
        assertContentEquals(noAnsiMessage.toByteArray(), "ANSI normal".toByteArray())
    }
}
