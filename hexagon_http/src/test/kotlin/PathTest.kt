package com.hexagonkt.http

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

internal class PathTest {

    @Test fun `URLs with blank parameters are matched`() {
        val regexPath = Path("/alpha/{param}/bravo")
        assert(regexPath.matches("/alpha//bravo"))
        assertTrue(regexPath.extractParameters("/alpha//bravo")["param"]?.isEmpty() ?: false)
    }

    @Test fun `Regex is matched properly`() {
        val regexPath = Path("/alpha/?*")
        assert(regexPath.matches("/alphabet/beta/p2"))
        assert(regexPath.matches("/alphabet"))

        Path("/alpha(/*)?").apply {
            assert(matches("/alpha/beta/p2"))
            assert(matches("/alpha"))
        }

        Path("/(this|that)/alpha/{param}/tango*").apply {
            assert(matches("/this/alpha/v11/tango"))
            assert(matches("/this/alpha/v12/tango1"))
            assert(matches("/this/alpha/v13/tango12"))
            assert(matches("/that/alpha/v21/tango"))
            assert(matches("/that/alpha/v22/tango1"))
            assert(matches("/that/alpha/v23/tango12"))

            assertEquals("v11", extractParameters("/this/alpha/v11/tango")["param"])
            assertEquals("v12", extractParameters("/this/alpha/v12/tango1")["param"])
            assertEquals("v13", extractParameters("/this/alpha/v13/tango12")["param"])
            assertEquals("v21", extractParameters("/that/alpha/v21/tango")["param"])
            assertEquals("v22", extractParameters("/that/alpha/v22/tango1")["param"])
            assertEquals("v23", extractParameters("/that/alpha/v23/tango12")["param"])
        }
    }

    @Test fun `A path without parameters do not have regex neither params table`() {
        val pathWithoutData = Path("/alpha/bravo/tango")
        assert(pathWithoutData.pattern == "/alpha/bravo/tango")
        assert(!pathWithoutData.hasParameters)
        assert(!pathWithoutData.hasWildcards)
        assert(pathWithoutData.regex == null)
        assert(pathWithoutData.parameterIndex.isEmpty())

        assert(pathWithoutData.matches("/alpha/bravo/tango"))
        assert(!pathWithoutData.matches("/alpha/bravo/tango/zulu"))
        assert(!pathWithoutData.matches("/zulu/alpha/bravo/tango"))

        assert(pathWithoutData.extractParameters("/alpha/bravo/tango").isEmpty())
    }

    @Test fun `Invalid path parameters`() {
        assertFailsWith<IllegalArgumentException> {
            Path("alpha/bravo")
        }

        assertFailsWith<IllegalArgumentException> {
            Path("/alpha/bravo/:id")
        }
    }

    @Test fun `Extract parameter from a non matching url fails`() {
        val pathWithoutData = Path("/alpha/bravo/tango")
        assertFailsWith<IllegalArgumentException> {
            pathWithoutData.extractParameters("/alpha/bravo/tango/zulu")
        }

        val pathWith1Parameter = Path("/alpha/{param}/tango")
        assertFailsWith<IllegalArgumentException> {
            assert(pathWith1Parameter.extractParameters("zulu/alpha/abc/tango").isEmpty())
        }
    }

    @Test fun `A path with parameters have regex and params table`() {
        assert(!Path("/alpha/{param}/tango").matches("/alpha/a/tango/zulu"))

        val pathWith1Parameter = Path("/alpha/{param}/tango*")
        assert(pathWith1Parameter.pattern == "/alpha/{param}/tango*")
        assert(pathWith1Parameter.hasParameters)
        assert(pathWith1Parameter.hasWildcards)
        assert(pathWith1Parameter.regex?.pattern == "/alpha/(.+?)/tango(.*?)$")
        assert(pathWith1Parameter.parameterIndex == listOf("param", ""))

        assert(pathWith1Parameter.matches("/alpha/a/tango"))
        assert(pathWith1Parameter.matches("/alpha/abc/tango"))
        assert(!pathWith1Parameter.matches("/alpha//tango"))
        assert(!pathWith1Parameter.matches("/alpha/tango"))
        assert(pathWith1Parameter.matches("/alpha/a/tango/zulu"))

        val params = pathWith1Parameter.extractParameters("/alpha/abc/tango")
        assert(params == mapOf("param" to "abc"))

        val pathWith2Parameters = Path("/alpha/{param}/tango/{arg}")
        assert(pathWith2Parameters.pattern == "/alpha/{param}/tango/{arg}")
        assert(pathWith2Parameters.hasParameters)
        assert(pathWith2Parameters.regex?.pattern == "/alpha/(.*?)/tango/(.*?)$")
        assert(pathWith2Parameters.parameterIndex == listOf("param", "arg"))

        val params2 = pathWith2Parameters.extractParameters("/alpha/abc/tango/def")
        assert(params2 == mapOf("param" to "abc", "arg" to "def"))
    }

    @Test fun `Path with a wildcard resolve parameters properly`() {
        val pathWith1Parameter = Path("/alpha/*/{param}/tango")
        assert(pathWith1Parameter.pattern == "/alpha/*/{param}/tango")
        assert(pathWith1Parameter.hasParameters)
        assert(pathWith1Parameter.regex?.pattern == "/alpha/(.*?)/(.+?)/tango$")
        assert(pathWith1Parameter.parameterIndex == listOf("", "param"))

        val pathWith2Parameters = Path("/alpha/{param}/tango/{arg}/*")
        assert(pathWith2Parameters.pattern == "/alpha/{param}/tango/{arg}/*")
        assert(pathWith2Parameters.hasParameters)
        assert(pathWith2Parameters.regex?.pattern == "/alpha/(.+?)/tango/(.+?)/(.*?)$")
        assert(pathWith2Parameters.parameterIndex == listOf("param", "arg", ""))
    }

    @Test fun `Path with many wildcards resolve parameters properly`() {
        val pathWith1Parameter = Path("/*/alpha/*/{param}/tango")
        assert(pathWith1Parameter.pattern == "/*/alpha/*/{param}/tango")
        assert(pathWith1Parameter.hasParameters)
        assert(pathWith1Parameter.regex?.pattern == "/(.*?)/alpha/(.*?)/(.+?)/tango$")
        assert(pathWith1Parameter.parameterIndex == listOf("", "", "param"))

        val pathWith2Parameters = Path("/alpha/*/{param}/tango/{arg}/*")
        assert(pathWith2Parameters.pattern == "/alpha/*/{param}/tango/{arg}/*")
        assert(pathWith2Parameters.hasParameters)
        assert(pathWith2Parameters.regex?.pattern == "/alpha/(.*?)/(.+?)/tango/(.+?)/(.*?)$")
        assert(pathWith2Parameters.parameterIndex == listOf("", "param", "arg", ""))
    }

    @Test fun `Segments of a path are split correctly`() {
        val segments1 = Path("/alpha/{p1}/beta/{p2}").segments
        assert(segments1 == listOf("/alpha/", "/beta/", ""))

        val segments2 = Path("/{p0}/alpha/{p1}/beta/{p2}").segments
        assert(segments2 == listOf("/", "/alpha/", "/beta/", ""))

        val segments3 = Path("/alpha/{p1}/beta").segments
        assert(segments3 == listOf("/alpha/", "/beta"))
    }
}
