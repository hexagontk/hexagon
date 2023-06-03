package com.hexagonkt.http.patterns

import com.hexagonkt.http.patterns.TemplatePathPattern.Companion.VARIABLE_PATTERN as VP
import kotlin.test.Test
import kotlin.test.*

internal class RegexPathPatternTest {

    @Test fun `Insert parameters fails on invalid parameters`() {
        fun testInvalid(re: String, vararg params: Pair<String, Any>) {
            val path = RegexPathPattern(Regex(re))
            val pathParameters = path.parameters
            val parameters = params.toMap()
            val parametersKeys = parameters.keys
            val e = assertFailsWith<IllegalArgumentException> { path.insertParameters(parameters) }
            assertEquals(
                "Parameters must match pattern's parameters($pathParameters). Provided: $parametersKeys",
                e.message
            )
        }

        testInvalid("/alpha/(?<param>$VP?)/tango/(?<arg>$VP?)$", "param1" to "v1", "arg" to "v2")
        testInvalid("/alpha/(?<param>$VP?)/tango/(?<arg>$VP?)$", "param" to "v1")
        testInvalid("/alpha/(?<param>$VP?)/tango/(?<arg>$VP?)$", "arg" to "v2")
        testInvalid(
            "/alpha/(?<param>$VP?)/tango/(?<arg>$VP?)$",
            "param" to "v1", "arg" to "v2", "extra" to "v3"
        )
    }

    @Test fun `Insert parameters on path`() {
        val path = RegexPathPattern(Regex("/alpha/(?<param>$VP?)/tango/(?<arg>$VP?)$"))
        assertEquals(listOf("param", "arg"), path.parameters)
        val parameters = mapOf("param" to "v1", "arg" to "v2")
        assertEquals("/alpha/v1/tango/v2$", path.insertParameters(parameters))
    }

    @Test fun `Prefixes are matched if pattern is prefix`() {
        val regexPath = RegexPathPattern(Regex("/alpha/bravo$"))
        assertFalse(regexPath.matches("/alpha/bravo/tango"))

        val regexPathPrefix = RegexPathPattern(Regex("/alpha/bravo"))
        assert(regexPathPrefix.matches("/alpha/bravo"))
        assert(regexPathPrefix.matches("/alpha/bravo/tango"))
        assertFalse(regexPathPrefix.matches("/prefix/alpha/bravo"))
    }

    @Test fun `Regex prefixes can be appended to patterns`() {
        val regexPath = RegexPathPattern(Regex(""))
        val pattern = regexPath.addPrefix("/prefix/{variable}")
        assertTrue(pattern.matches("/prefix/bravo"))
    }

    @Test fun `Prefixes can be appended to patterns`() {
        val regexPath = RegexPathPattern(Regex("/alpha/bravo"))
        assert(regexPath.addPrefix(null).matches("/alpha/bravo"))
        assertFalse(regexPath.addPrefix("/prefix").matches("/alpha/bravo"))
        assertTrue(regexPath.addPrefix("/prefix").matches("/prefix/alpha/bravo"))
    }

    @Test fun `A path without parameters do not have params table`() {
        val pathWithoutData = RegexPathPattern(Regex("/alpha/bravo/tango$"))
        assert(pathWithoutData.parameters.isEmpty())

        assert(pathWithoutData.matches("/alpha/bravo/tango"))
        assert(!pathWithoutData.matches("/alpha/bravo/tango/zulu"))
        assert(!pathWithoutData.matches("/zulu/alpha/bravo/tango"))

        assert(pathWithoutData.extractParameters("/alpha/bravo/tango").isEmpty())

        val pathWithoutParams = RegexPathPattern(Regex("/alpha/(bravo|charlie)/tango$"))
        assert(pathWithoutParams.parameters.isEmpty())

        assert(pathWithoutParams.matches("/alpha/bravo/tango"))
        assert(!pathWithoutParams.matches("/alpha/bravo/tango/zulu"))
        assert(!pathWithoutParams.matches("/zulu/alpha/bravo/tango"))

        assert(pathWithoutParams.matches("/alpha/charlie/tango"))
        assert(!pathWithoutParams.matches("/alpha/charlie/tango/zulu"))
        assert(!pathWithoutParams.matches("/zulu/alpha/charlie/tango"))

        assertEquals(
            mapOf("0" to "bravo"),
            pathWithoutParams.extractParameters("/alpha/bravo/tango")
        )
        assertEquals(
            mapOf("0" to "charlie"),
            pathWithoutParams.extractParameters("/alpha/charlie/tango")
        )
    }

    @Test fun `Empty and wildcard path patterns are allowed`() {
        assertEquals("", RegexPathPattern(Regex("")).pattern)
        assertEquals("(.*?)", RegexPathPattern(Regex("(.*?)")).pattern)
        assertEquals("(.*?)/whatever", RegexPathPattern(Regex("(.*?)/whatever")).pattern)
    }

    @Test fun `Invalid path parameters`() {
        assertFailsWith<IllegalArgumentException> {
            RegexPathPattern(Regex("alpha/bravo"))
        }
    }

    @Test fun `Extract parameter from a pattern without parameters returns empty map`() {
        val pathWithoutData = RegexPathPattern(Regex("/alpha/bravo/tango"))
        assertFailsWith<IllegalArgumentException> {
            pathWithoutData.extractParameters("/alpha/bravo/tango/zulu")
        }
    }

    @Test fun `Extract parameter from a non matching url fails`() {
        assertFailsWith<IllegalArgumentException> {
            val regex = Regex("/alpha/(?<param>$VP?)/tango$")
            RegexPathPattern(regex).extractParameters("zulu/alpha/abc/tango")
        }
    }

    @Test fun `A path with parameters have regex and params table`() {
        val regexPathPattern = Regex("/alpha/(?<param>$VP?)/tango$")
        assert(!RegexPathPattern(regexPathPattern).matches("/alpha/a/tango/zulu"))

        val pathWith1Parameter = RegexPathPattern(Regex("/alpha/(?<param>$VP?)/tango.*$"))
        assertEquals(listOf("param"), pathWith1Parameter.parameters)

        assert(pathWith1Parameter.matches("/alpha/a/tango"))
        assert(pathWith1Parameter.matches("/alpha/abc/tango"))
        assert(!pathWith1Parameter.matches("/alpha//tango"))
        assert(!pathWith1Parameter.matches("/alpha/tango"))
        assert(pathWith1Parameter.matches("/alpha/a/tango/zulu"))

        val params = pathWith1Parameter.extractParameters("/alpha/abc/tango")
        assertEquals(mapOf("param" to "abc", "0" to "abc"), params)

        val regex = Regex("/alpha/(?<param>$VP?)/tango/(?<arg>$VP?)$")
        val pathWith2Parameters = RegexPathPattern(regex)
        assertEquals(listOf("param", "arg"), pathWith2Parameters.parameters)

        val params2 = pathWith2Parameters.extractParameters("/alpha/abc/tango/def")
        assertEquals(mapOf("param" to "abc", "arg" to "def", "0" to "abc", "1" to "def"), params2)
    }

    @Test fun `Path with a wildcard resolve parameters properly`() {
        val regex1 = Regex("/alpha/$VP/(?<param>$VP?)/tango$")
        assertEquals(listOf("param"), RegexPathPattern(regex1).parameters)
        val regex2 = Regex("/alpha/(?<param>$VP?)/tango/(?<arg>$VP?)/$VP")
        assertEquals(listOf("param", "arg"), RegexPathPattern(regex2).parameters)
    }

    @Test fun `Path without parameters does not return any parameter`() {
        assertEquals(emptyList(), RegexPathPattern(Regex("/$VP/alpha/$VP/tango")).parameters)
        assertEquals(emptyList(), RegexPathPattern(Regex("/alpha/tango")).parameters)
    }

    @Test fun `Path with many wildcards resolve parameters properly`() {
        val regex1 = Regex("/$VP/alpha/$VP/(?<param>$VP?)/tango")
        assertEquals(listOf("param"), RegexPathPattern(regex1).parameters)
        val regex2 = Regex("/alpha/$VP/(?<param>$VP?)/tango/(?<arg>$VP?)/$VP")
        assertEquals(listOf("param", "arg"), RegexPathPattern(regex2).parameters)
    }

    @Test fun `Cover missing lines`() {
        assertEquals(RegexPathPattern.PARAMETER_REGEX, RegexPathPattern.PARAMETER_REGEX)
    }
}
