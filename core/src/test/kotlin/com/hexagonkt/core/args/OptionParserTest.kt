package com.hexagonkt.core.args

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class OptionParserTest {

    private lateinit var optionParser: OptionParser

    @BeforeEach fun setUp() {
        optionParser = OptionParser
    }

    @Test fun `Parse string option`() {
        val verbose = Option('o', "option", String::class)
        val actual = optionParser.parse(listOf(verbose), arrayOf("--option=value"))
        val expected: Result<Map<Option<*>, *>> = Result.success(mapOf(verbose to "value"))

        assertEquals(expected, actual)
    }

    @Test fun `Parse boolean option without value`() {
        val verbose = Option('v', "verbose", Boolean::class)
        val actual = optionParser.parse(listOf(verbose), arrayOf("--verbose"))
        val expected: Result<Map<Option<*>, *>> = Result.success(mapOf(verbose to true))

        assertEquals(expected, actual)
    }
    @Test fun `Parse string option with two words name`() {
        val verbose = Option('o', "long-option", String::class)
        val actual = optionParser.parse(listOf(verbose), arrayOf("--long-option=value"))
        val expected: Result<Map<Option<*>, *>> = Result.success(mapOf(verbose to "value"))

        assertEquals(expected, actual)
    }

    @Test fun `Parse options with short name`() {
        val flag1 = Option('a', type = Boolean::class)
        val flag2 = Option('b', type = Boolean::class)
        val flag3 = Option('c', type = Boolean::class)

        val actual = optionParser.parse(listOf(flag1, flag2, flag3), arrayOf("-a", "-b", "-c"))
        val expected: Result<Map<Option<*>, *>> = Result.success(mapOf(flag1 to true, flag2 to true, flag3 to true))

        assertEquals(expected, actual)
    }

    @Test fun `Parse options with short name in the compacted fashion`() {
        val flag1 = Option('a', type = Boolean::class)
        val flag2 = Option('b', type = Boolean::class)
        val flag3 = Option('c', type = Boolean::class)

        val actual = optionParser.parse(listOf(flag1, flag2, flag3), arrayOf("-abc"))
        val expected: Result<Map<Option<*>, *>> = Result.success(mapOf(flag1 to true, flag2 to true, flag3 to true))

        assertEquals(expected, actual)
    }
    @Test fun `Parse should fail if the some of the args is invalid`() {

        val someArg = Option('s', "some-arg", String::class)

        val actual = optionParser.parse(listOf(someArg), arrayOf("---some-arg=value"))
        val expected: Result<Map<Option<*>, *>> = Result.failure(InvalidOptionSyntaxException)

        assertEquals(expected, actual)
    }

    @Test fun `Parse should fail if a short named arg is invalid`() {

        val someArg = Option('s', "some-arg", Boolean::class)
        val another = Option('a', "another-arg", Boolean::class)

        val actual = optionParser.parse(listOf(someArg, another), arrayOf("-s-a"))
        val expected: Result<Map<Option<*>, *>> = Result.failure(InvalidOptionSyntaxException)

        assertEquals(expected, actual)
    }

    @Test fun `Parse both long and short named options`() {
        val shortFlag = Option('s', type = Boolean::class)
        val longFlag = Option('l', longName = "long", type = Boolean::class)
        val longValue = Option('v', "value", String::class)

        val actual = optionParser.parse(listOf(shortFlag, longFlag, longValue), arrayOf("-s", "--long", "--value=some"))
        val expected: Result<Map<Option<*>, *>> = Result.success(mapOf(shortFlag to true, longFlag to true, longValue to "some"))

        assertEquals(expected, actual)
    }
}
