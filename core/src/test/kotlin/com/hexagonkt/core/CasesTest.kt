package com.hexagonkt.core

import com.hexagonkt.core.CasesTest.Size.X_L
import kotlin.test.Test
import kotlin.test.*

internal class CasesTest {

    private enum class Size { X_L }

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
}
