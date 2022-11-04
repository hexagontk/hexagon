package com.hexagonkt.core.args

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.lang.IllegalStateException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class OptionParserTest {

    private lateinit var optionParser: OptionParser

    @BeforeEach fun setUp() {
        optionParser = OptionParser
    }

    @Test fun `Parse string option`() {
        val verbose = Option('o', String::class, "option")
        val actual = optionParser.parse(listOf(verbose), arrayOf("--option=value"))
        val expected: Map<Option<*>, *> = mapOf(verbose to "value")

        assertEquals(expected, actual)
    }

    @Test fun `Parse boolean option without value`() {
        val verbose = Option('v', Boolean::class, "verbose")
        val actual = optionParser.parse(listOf(verbose), arrayOf("--verbose"))
        val expected: Map<Option<*>, *> = mapOf(verbose to true)

        assertEquals(expected, actual)
    }
    @Test fun `Parse string option with two words name`() {
        val verbose = Option('o', String::class, "long-option")
        val actual = optionParser.parse(listOf(verbose), arrayOf("--long-option=value"))
        val expected: Map<Option<*>, *> = mapOf(verbose to "value")

        assertEquals(expected, actual)
    }

    @Test fun `Parse options with short name`() {
        val flag1 = Option('a', type = Boolean::class)
        val flag2 = Option('b', type = Boolean::class)
        val flag3 = Option('c', type = Boolean::class)

        val actual = optionParser.parse(listOf(flag1, flag2, flag3), arrayOf("-a", "-b", "-c"))
        val expected: Map<Option<*>, *> = mapOf(flag1 to true, flag2 to true, flag3 to true)

        assertEquals(expected, actual)
    }

    @Test fun `Parse options with short name in the compacted fashion`() {
        val flag1 = Option('a', type = Boolean::class)
        val flag2 = Option('b', type = Boolean::class)
        val flag3 = Option('c', type = Boolean::class)

        val actual = optionParser.parse(listOf(flag1, flag2, flag3), arrayOf("-abc"))
        val expected: Map<Option<*>, *> = mapOf(flag1 to true, flag2 to true, flag3 to true)

        assertEquals(expected, actual)
    }
    @Test fun `Parse should fail if the some of the args is invalid`() {

        val someArg = Option('s', String::class, "some-arg")

        val e = assertFailsWith<IllegalStateException> {
            optionParser.parse(listOf(someArg), arrayOf("---some-arg=value"))
        }

        assertEquals("InvalidOptionSyntaxException", e.message)
    }

    @Test fun `Parse should fail if a short named arg is invalid`() {

        val someArg = Option('s', Boolean::class, "some-arg")
        val another = Option('a', Boolean::class, "another-arg")

        val e = assertFailsWith<IllegalStateException> {
            optionParser.parse(listOf(someArg, another), arrayOf("-s-a"))
        }

        assertEquals("InvalidOptionSyntaxException", e.message)
    }

    @Test fun `Parse both long and short named options`() {
        val shortFlag = Option('s', type = Boolean::class)
        val longFlag = Option('l', type = Boolean::class, longName = "long")
        val longValue = Option('v', String::class, "value")
        val options = listOf(shortFlag, longFlag, longValue)

        val actual = optionParser.parse(options, arrayOf("-s", "--long", "--value=some"))
        val expected: Map<Option<*>, *> =
            mapOf(shortFlag to true, longFlag to true, longValue to "some")

        assertEquals(expected, actual)
    }

    @Test fun `Parse long named flag with false as value`() {
        val longFlag = Option('l', type = Boolean::class, longName = "long")

        val actual = optionParser.parse(listOf(longFlag), arrayOf("--long=false"))
        val expected: Map<Option<*>, *> = mapOf(longFlag to false)

        assertEquals(expected, actual)
    }

    @Test fun `Parse options with int values`() {
        val level = Option('l', type = Int::class, longName = "level")
        val another = Option('a', Boolean::class, "another-arg")
        val options = listOf(level, another)

        val actual = optionParser.parse(options, arrayOf("--another-arg", "--level=3"))
        val expected: Map<Option<*>, *> = mapOf(level to 3, another to true)

        assertEquals(expected, actual)
    }

    @Test fun `Parse options with double values`() {
        val time = Option('t', type = Double::class, longName = "time")
        val another = Option('a', Boolean::class, "another-arg")
        val options = listOf(time, another)

        val actual = optionParser.parse(options, arrayOf("--time=2.5", "--another-arg"))
        val expected: Map<Option<*>, *> = mapOf(time to 2.5, another to true)

        assertEquals(expected, actual)
    }
}
