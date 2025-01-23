package com.hexagontk.http.patterns

import org.junit.jupiter.api.Test
import kotlin.test.*

internal class TemplatePathPatternTest {

    @Test fun `Regex parameters are allowed`() {
        val path = TemplatePathPattern("/alpha/{param:\\d+}/tango/{arg:a|b|c}")
        assertEquals(listOf("param", "arg"), path.parameters)
        val parameters = mapOf("param" to "v1", "arg" to "v2")
        assertEquals("/alpha/v1/tango/v2", path.insertParameters(parameters))

        assert(path.matches("/alpha/0/tango/a"))
        assert(path.matches("/alpha/0/tango/b"))
        assert(path.matches("/alpha/0/tango/c"))
        assert(path.matches("/alpha/10/tango/a"))
        assert(path.matches("/alpha/10/tango/b"))
        assert(path.matches("/alpha/10/tango/c"))

        assertEquals(
            mapOf("param" to "0", "arg" to "a", "0" to "0", "1" to "a"),
            path.extractParameters("/alpha/0/tango/a")
        )
        assertEquals(
            mapOf("param" to "0", "arg" to "b", "0" to "0", "1" to "b"),
            path.extractParameters("/alpha/0/tango/b")
        )
        assertEquals(
            mapOf("param" to "0", "arg" to "c", "0" to "0", "1" to "c"),
            path.extractParameters("/alpha/0/tango/c")
        )
        assertEquals(
            mapOf("param" to "10", "arg" to "a", "0" to "10", "1" to "a"),
            path.extractParameters("/alpha/10/tango/a")
        )
        assertEquals(
            mapOf("param" to "10", "arg" to "b", "0" to "10", "1" to "b"),
            path.extractParameters("/alpha/10/tango/b")
        )
        assertEquals(
            mapOf("param" to "10", "arg" to "c", "0" to "10", "1" to "c"),
            path.extractParameters("/alpha/10/tango/c")
        )

        assert(!path.matches("/alpha/10/tango/abc"))
        assert(!path.matches("/alpha/10/tango/z"))
        assert(!path.matches("/alpha/x/tango/a"))
    }

    @Test fun `Insert parameters fails on invalid parameters`() {
        fun testInvalid(template: String, vararg params: Pair<String, Any>) {
            val path = TemplatePathPattern(template)
            val pathParameters = path.parameters
            val parameters = params.toMap()
            val parametersKeys = parameters.keys
            val e = assertFailsWith<IllegalArgumentException> { path.insertParameters(parameters) }
            assertEquals(
                "Parameters must match pattern's parameters($pathParameters). Provided: $parametersKeys",
                e.message
            )
        }

        testInvalid("/alpha/{param}/tango/{arg}", "param1" to "v1", "arg" to "v2")
        testInvalid("/alpha/{param}/tango/{arg}", "param" to "v1")
        testInvalid("/alpha/{param}/tango/{arg}", "arg" to "v2")
        testInvalid("/alpha/{param}/tango/{arg}", "param" to "v1", "arg" to "v2", "extra" to "v3")
    }

    @Test fun `Insert parameters on path`() {
        val path = TemplatePathPattern("/alpha/{param}/tango/{arg}")
        assertEquals(listOf("param", "arg"), path.parameters)
        val parameters = mapOf("param" to "v1", "arg" to "v2")
        assertEquals("/alpha/v1/tango/v2", path.insertParameters(parameters))
    }

    @Test fun `Prefixes are matched if pattern is prefix`() {
        val regexPath = TemplatePathPattern("/alpha/bravo")
        assertFalse(regexPath.matches("/alpha/bravo/tango"))

        val regexPathPrefix = TemplatePathPattern("/alpha/bravo", true)
        assert(regexPathPrefix.matches("/alpha/bravo"))
        assert(regexPathPrefix.matches("/alpha/bravo/tango"))
        assertFalse(regexPathPrefix.matches("/prefix/alpha/bravo"))

        val regexPathParamsPrefix = TemplatePathPattern("/alpha/{var}/bravo", true)
        assert(regexPathParamsPrefix.matches("/alpha/a/bravo"))
        assert(regexPathParamsPrefix.matches("/alpha/a/bravo/tango"))
        assertFalse(regexPathParamsPrefix.matches("/prefix/alpha/a/bravo"))
    }

    @Test fun `URLs with blank parameters are not matched`() {
        val regexPath = TemplatePathPattern("/alpha/{param}/bravo")
        assertFalse(regexPath.matches("/alpha//bravo"))
    }

    @Test fun `Prefixes can be appended to patterns`() {
        val regexPath = TemplatePathPattern("/alpha/bravo")
        assert(regexPath.addPrefix(null).matches("/alpha/bravo"))
        assertFalse(regexPath.addPrefix("/prefix").matches("/alpha/bravo"))
        assertTrue(regexPath.addPrefix("/prefix").matches("/prefix/alpha/bravo"))
    }

    @Test fun `Regex is matched properly`() {
        TemplatePathPattern("/alpha/?*").apply {
            assert(matches("/alphabet/beta/p2"))
            assert(matches("/alphabet"))
        }

        TemplatePathPattern("/alpha*").apply {
            assert(matches("/alphabet/beta/p2"))
            assert(matches("/alphabet"))
        }

        TemplatePathPattern("/alpha(/*)?").apply {
            assert(matches("/alpha/beta/p2"))
            assert(matches("/alpha"))
        }

        TemplatePathPattern("/alpha(/a|/b)").apply {
            assert(matches("/alpha/a"))
            assert(matches("/alpha/b"))
            assertFalse(matches("/alpha/ba"))
        }

        TemplatePathPattern("/(this|that)/alpha/{param}/tango*").apply {
            assert(matches("/this/alpha/v11/tango"))
            assert(matches("/this/alpha/v12/tango1"))
            assert(matches("/this/alpha/v13/tango12"))
            assert(matches("/that/alpha/v21/tango"))
            assert(matches("/that/alpha/v22/tango1"))
            assert(matches("/that/alpha/v23/tango12"))

            assertEquals(
                mapOf("param" to "v11", "0" to "this", "1" to "v11", "2" to ""),
                extractParameters("/this/alpha/v11/tango")
            )
            assertEquals(
                mapOf("param" to "v12", "0" to "this", "1" to "v12", "2" to "1"),
                extractParameters("/this/alpha/v12/tango1")
            )
            assertEquals(
                mapOf("param" to "v13", "0" to "this", "1" to "v13", "2" to "12"),
                extractParameters("/this/alpha/v13/tango12")
            )

            assertEquals(
                mapOf("param" to "v21", "0" to "that", "1" to "v21", "2" to ""),
                extractParameters("/that/alpha/v21/tango")
            )
            assertEquals(
                mapOf("param" to "v22", "0" to "that", "1" to "v22", "2" to "1"),
                extractParameters("/that/alpha/v22/tango1")
            )
            assertEquals(
                mapOf("param" to "v23", "0" to "that", "1" to "v23", "2" to "12"),
                extractParameters("/that/alpha/v23/tango12")
            )
        }
    }

    @Test fun `A path without parameters do not have params table`() {
        val pathWithoutData = TemplatePathPattern("/alpha/bravo/tango")
        assertEquals("/alpha/bravo/tango", pathWithoutData.pattern)
        assert(pathWithoutData.extractParameters("/alpha/bravo/tango").isEmpty())

        assert(pathWithoutData.matches("/alpha/bravo/tango"))
        assert(!pathWithoutData.matches("/alpha/bravo/tango/zulu"))
        assert(!pathWithoutData.matches("/zulu/alpha/bravo/tango"))

        assert(pathWithoutData.extractParameters("/alpha/bravo/tango").isEmpty())

        val pathWithoutParams = TemplatePathPattern("/alpha/(bravo|charlie)/tango")
        assertEquals("/alpha/(bravo|charlie)/tango", pathWithoutParams.pattern)
        assertEquals("bravo", pathWithoutParams.extractParameters("/alpha/bravo/tango")["0"])
        assertEquals("charlie", pathWithoutParams.extractParameters("/alpha/charlie/tango")["0"])

        assert(pathWithoutParams.matches("/alpha/bravo/tango"))
        assert(!pathWithoutParams.matches("/alpha/bravo/tango/zulu"))
        assert(!pathWithoutParams.matches("/zulu/alpha/bravo/tango"))

        assert(pathWithoutParams.matches("/alpha/charlie/tango"))
        assert(!pathWithoutParams.matches("/alpha/charlie/tango/zulu"))
        assert(!pathWithoutParams.matches("/zulu/alpha/charlie/tango"))
    }

    @Test fun `Empty and wildcard path patterns are allowed`() {
        assertEquals("", TemplatePathPattern("").pattern)
        assertEquals("*", TemplatePathPattern("*").pattern)
        assertEquals("*/whatever", TemplatePathPattern("*/whatever").pattern)
    }

    @Test fun `Invalid path parameters`() {
        assertFailsWith<IllegalArgumentException> {
            TemplatePathPattern("alpha/bravo")
        }
    }

    @Test fun `Extract parameter from a pattern without parameters returns empty map`() {
        val pathWithoutData = TemplatePathPattern("/alpha/bravo/tango")
        assertFailsWith<IllegalArgumentException> {
            pathWithoutData.extractParameters("/alpha/bravo/tango/zulu")
        }
    }

    @Test fun `Extract parameter from a non matching url fails`() {
        assertFailsWith<IllegalArgumentException> {
            TemplatePathPattern("/alpha/{param}/tango").extractParameters("zulu/alpha/abc/tango")
        }
    }

    @Test fun `A path with parameters have regex and params table`() {
        assert(!TemplatePathPattern("/alpha/{param}/tango").matches("/alpha/a/tango/zulu"))

        val pathWith1Parameter = TemplatePathPattern("/alpha/{param}/tango*")
        assertEquals("/alpha/{param}/tango*", pathWith1Parameter.pattern)
        val expectedParams = mapOf("param" to "abc", "0" to "abc")
        assertEquals(
            expectedParams + ("1" to ""),
            pathWith1Parameter.extractParameters("/alpha/abc/tango")
        )
        assertEquals(
            expectedParams + ("1" to "/bravo"),
            pathWith1Parameter.extractParameters("/alpha/abc/tango/bravo")
        )
        assertFailsWith<IllegalArgumentException> {
            pathWith1Parameter.extractParameters("/beta/alpha/abc/tango/bravo")
        }

        assert(pathWith1Parameter.matches("/alpha/a/tango"))
        assert(pathWith1Parameter.matches("/alpha/abc/tango"))
        assert(!pathWith1Parameter.matches("/alpha//tango"))
        assert(!pathWith1Parameter.matches("/alpha/tango"))
        assert(pathWith1Parameter.matches("/alpha/a/tango/zulu"))

        val pathWith2Parameters = TemplatePathPattern("/alpha/{param}/tango/{arg}")
        assertEquals("/alpha/{param}/tango/{arg}", pathWith2Parameters.pattern)
        assertEquals(
            mapOf("param" to "abc", "arg" to "bravo", "0" to "abc", "1" to "bravo"),
            pathWith2Parameters.extractParameters("/alpha/abc/tango/bravo")
        )

        val params2 = pathWith2Parameters.extractParameters("/alpha/abc/tango/def")
        assertEquals(mapOf("param" to "abc", "arg" to "def", "0" to "abc", "1" to "def"), params2)
    }

    @Test fun `Path with a wildcard resolve parameters properly`() {
        TemplatePathPattern("/alpha/*/{param}/tango").let {
            assertEquals("/alpha/*/{param}/tango", it.pattern)
            assertEquals(
                mapOf("param" to "abc", "0" to "a", "1" to "abc"),
                it.extractParameters("/alpha/a/abc/tango")
            )
            assertEquals(
                mapOf("param" to "abc", "0" to "b", "1" to "abc"),
                it.extractParameters("/alpha/b/abc/tango")
            )
        }

        TemplatePathPattern("/alpha/{param}/tango/{arg}/*").let {
            assertEquals("/alpha/{param}/tango/{arg}/*", it.pattern)

            val parameters = mapOf("param" to "abc", "arg" to "def", "0" to "abc", "1" to "def")
            assertEquals(parameters + ("2" to "1"), it.extractParameters("/alpha/abc/tango/def/1"))
            assertEquals(parameters + ("2" to "2"), it.extractParameters("/alpha/abc/tango/def/2"))
        }
    }

    @Test fun `Path without parameters does not return any parameter`() {
        TemplatePathPattern("/*/alpha/*/tango").let {
            assertEquals("/*/alpha/*/tango", it.pattern)
            assertEquals(mapOf("0" to "a", "1" to "b"), it.extractParameters("/a/alpha/b/tango"))
            assertEquals(mapOf("0" to "c", "1" to "d"), it.extractParameters("/c/alpha/d/tango"))
        }
        TemplatePathPattern("/alpha/tango").let {
            assertEquals("/alpha/tango", it.pattern)
            assert(it.extractParameters("/alpha/tango").isEmpty())
        }
    }

    @Test fun `Path with many wildcards resolve parameters properly`() {
        TemplatePathPattern("/*/alpha/*/{param}/tango").let {
            assertEquals("/*/alpha/*/{param}/tango", it.pattern)
            assertEquals(
                mapOf("param" to "abc", "0" to "a", "1" to "b", "2" to "abc"),
                it.extractParameters("/a/alpha/b/abc/tango")
            )
            assertEquals(
                mapOf("param" to "abc", "0" to "c", "1" to "d", "2" to "abc"),
                it.extractParameters("/c/alpha/d/abc/tango")
            )
        }

        TemplatePathPattern("/alpha/*/{param}/tango/{arg}/*").let {
            assertEquals("/alpha/*/{param}/tango/{arg}/*", it.pattern)

            val parameters = mapOf("param" to "abc", "arg" to "def")

            val parameters1 = parameters + mapOf("0" to "a", "1" to "abc", "2" to "def", "3" to "b")
            assertEquals(parameters1, it.extractParameters("/alpha/a/abc/tango/def/b"))
            val parameters2 = parameters + mapOf("0" to "c", "1" to "abc", "2" to "def", "3" to "d")
            assertEquals(parameters2, it.extractParameters("/alpha/c/abc/tango/def/d"))
        }
    }

    @Test fun `Cover missing lines`() {
        assertEquals(TemplatePathPattern.WILDCARD_REGEX, TemplatePathPattern.WILDCARD_REGEX)
        assertEquals(TemplatePathPattern.PARAMETER_REGEX, TemplatePathPattern.PARAMETER_REGEX)
        assertEquals(TemplatePathPattern.PLACEHOLDER_REGEX, TemplatePathPattern.PLACEHOLDER_REGEX)
    }

    @Test fun `Adding a prefix keep the prefix flag`() {
        TemplatePathPattern("/a", true).addPrefix("/b").let {
            assertEquals("/b/a", it.pattern)
            assertTrue(it.prefix)
        }
        TemplatePathPattern("/a", false).addPrefix("/b").let {
            assertEquals("/b/a$", it.pattern)
            assertFalse(it.prefix)
        }
    }
}
