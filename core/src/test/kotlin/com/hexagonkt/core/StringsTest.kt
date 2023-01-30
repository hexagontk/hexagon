package com.hexagonkt.core

import com.hexagonkt.core.StringsTest.Size.S
import com.hexagonkt.core.StringsTest.Size.X_L
import io.mockk.every
import io.mockk.mockk
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.test.Test
import kotlin.IllegalArgumentException
import kotlin.test.*
import kotlin.text.prependIndent

internal class StringsTest {

    enum class Size { S, M, L, X_L }

    @Test fun `String transformations work properly`() {
        assertEquals(File("dir/f.txt"), "dir/f.txt".toOrNull(File::class))
        assertEquals(LocalDate.parse("2020-02-28"), "2020-02-28".toOrNull(LocalDate::class))
        assertEquals(LocalTime.parse("21:20:10"), "21:20:10".toOrNull(LocalTime::class))
        assertEquals(
            LocalDateTime.parse("2020-02-28T21:20:10"),
            "2020-02-28T21:20:10".toOrNull(LocalDateTime::class)
        )
    }

    @Test fun `String case can changed`() {
        val words = listOf("these", "are", "a", "few", "words")
        assertEquals("These Are A Few Words", words.wordsToTitle())
        assertEquals("These are a few words", words.wordsToSentence())
        assertEquals("x l", X_L.toWords())
    }

    @Test fun `Strings can be converted to enum values`() {
        assertEquals(S, "s".toEnum(Size::valueOf))
        assertEquals(S, "S".toEnum(Size::valueOf))
        assertEquals(X_L, "x l".toEnum(Size::valueOf))
        assertEquals(X_L, "X L".toEnum(Size::valueOf))
        assertEquals(X_L, "X_L".toEnum(Size::valueOf))

        val e = assertFailsWith<IllegalArgumentException> {
            assertEquals(Size.M, "z".toEnum(Size::valueOf))
        }
        assertEquals("No enum constant com.hexagonkt.core.StringsTest.Size.Z", e.message)

        assertEquals(S, "s".toEnumOrNull(Size::valueOf))
        assertNull("z".toEnumOrNull(Size::valueOf))
    }

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

    @Test fun `Texts are translated properly to enum values`() {
        val sources = listOf("A", "Ab", "ab c", "DE f", "  Gh Y  ")
        val expected = listOf("A", "AB", "AB_C", "DE_F", "GH_Y")
        assertEquals(expected, sources.map(String::toEnumValue))
    }
}
