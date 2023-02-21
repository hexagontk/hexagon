package com.hexagonkt.helpers

import com.hexagonkt.core.eol
import com.hexagonkt.helpers.StringsTest.Size.S
import com.hexagonkt.helpers.StringsTest.Size.X_L
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.IllegalArgumentException
import kotlin.test.*

internal class StringsTest {

    enum class Size { S, M, L, X_L }

    @Test fun `Case regex matches proper text`() {
        assert("camelCaseTest1".matches(CAMEL_CASE))
        assert("PascalCaseTest2".matches(PASCAL_CASE))
        assert("Snake_Case_Test_3".matches(SNAKE_CASE))
        assert("Kebab-Case-Test-4".matches(KEBAB_CASE))

        assertFalse("0camelCaseTest1".matches(CAMEL_CASE))
        assertFalse("CamelCaseTest1".matches(CAMEL_CASE))

        assertFalse("1PascalCaseTest2".matches(PASCAL_CASE))
        assertFalse("pascalCaseTest2".matches(PASCAL_CASE))

        assertFalse("2_Snake_Case_Test_3".matches(SNAKE_CASE))
        assertFalse("Snake-Case-Test-3".matches(SNAKE_CASE))

        assertFalse("3-Kebab-Case-Test-4".matches(KEBAB_CASE))
        assertFalse("Kebab_Case_Test_4".matches(KEBAB_CASE))
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
        assertEquals("No enum constant com.hexagonkt.helpers.StringsTest.Size.Z", e.message)

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

    @Test fun `Converting valid kebab works properly`() {
        assertEquals(listOf("alfa", "beta"), "alfa-beta".kebabToWords())
        assertEquals(listOf("alfa", "beta"), "alfa--beta".kebabToWords())
        assertEquals(listOf("alfa", "beta"), "alfa---beta".kebabToWords())

        assertEquals("alfa-beta", listOf("alfa", "beta").wordsToKebab())
        assertEquals("alfa", listOf("alfa").wordsToKebab())
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

    @Test fun `Normalize works as expected`() {
        val striped = "áéíóúñçÁÉÍÓÚÑÇ".stripAccents()
        assertEquals("aeiouncAEIOUNC", striped)
    }

    @Test fun `Utf8 returns proper characters`() {
        assertEquals("👍", utf8(0xF0, 0x9F, 0x91, 0x8D))
        assertEquals("👎", utf8(0xF0, 0x9F, 0x91, 0x8E))
    }

    @Test fun `Texts are translated properly to enum values`() {
        val sources = listOf("A", "Ab", "ab c", "DE f", "  Gh Y  ")
        val expected = listOf("A", "AB", "AB_C", "DE_F", "GH_Y")
        assertEquals(expected, sources.map(String::toEnumValue))
    }
}
