package com.hexagonkt.server

import org.testng.annotations.Test
import kotlin.test.assertFailsWith

/**
 * TODO Check that URLs with blank parameters are not matched.
 * TODO Ie: /alfa/{param}/bravo is not matched by /alfa//bravo
 */
@Test class PathTest {
    fun `a path without parameters do not have regex neither params table` () {
        val pathWithoutData = Path("/alfa/bravo/tango")
        assert (pathWithoutData.path == "/alfa/bravo/tango")
        assert (!pathWithoutData.hasParameters)
        assert (pathWithoutData.regex == null)
        assert (pathWithoutData.parameterIndex.isEmpty())

        assert (pathWithoutData.matches ("/alfa/bravo/tango"))
        assert (!pathWithoutData.matches ("/alfa/bravo/tango/zulu"))
        assert (!pathWithoutData.matches ("/zulu/alfa/bravo/tango"))

        assert (pathWithoutData.extractParameters("/alfa/bravo/tango").isEmpty())
    }

    fun `invalid path parameters` () {
        val slashMessage = "'alfa/bravo' must start with '/'"
        assertFailsWith<IllegalArgumentException> (slashMessage) {
            Path("alfa/bravo")
        }

        val colonMessage = "Variables have {var} format. Path cannot have ':' alfa/bravo/:id"
        assertFailsWith<IllegalArgumentException> (colonMessage) {
            Path("alfa/bravo/:id")
        }
    }

    fun `extract parameter from a non matching url fails` () {
        val pathWithoutData = Path("/alfa/bravo/tango")
        assertFailsWith<IllegalArgumentException> ("URL '/alfa/bravo/tango/zulu' does not match path") {
            pathWithoutData.extractParameters ("/alfa/bravo/tango/zulu")
        }

        val pathWith1Parameter = Path("/alfa/{param}/tango")
        assertFailsWith<IllegalArgumentException> ("URL 'zulu/alfa/abc/tango' does not match path") {
            assert (pathWith1Parameter.extractParameters("zulu/alfa/abc/tango").isEmpty())
        }
    }

    fun `a path with parameters have regex and params table` () {
        assert (!Path("/alfa/{param}/tango").matches ("/alfa/a/tango/zulu"))

        val pathWith1Parameter = Path("/alfa/{param}/tango*")
        assert (pathWith1Parameter.path == "/alfa/{param}/tango*")
        assert (pathWith1Parameter.hasParameters)
        assert (pathWith1Parameter.regex?.pattern == "/alfa/(.+?)/tango(.*?)$")
        assert (pathWith1Parameter.parameterIndex == listOf ("param", ""))

        assert (pathWith1Parameter.matches ("/alfa/a/tango"))
        assert (pathWith1Parameter.matches ("/alfa/abc/tango"))
        assert (!pathWith1Parameter.matches ("/alfa//tango"))
        assert (!pathWith1Parameter.matches ("/alfa/tango"))
        assert (pathWith1Parameter.matches ("/alfa/a/tango/zulu"))

        val params = pathWith1Parameter.extractParameters("/alfa/abc/tango")
        assert (params == mapOf ("param" to "abc"))

        val pathWith2Parameters = Path("/alfa/{param}/tango/{arg}")
        assert (pathWith2Parameters.path == "/alfa/{param}/tango/{arg}")
        assert (pathWith2Parameters.hasParameters)
        assert (pathWith2Parameters.regex?.pattern == "/alfa/(.+?)/tango/(.+?)$")
        assert (pathWith2Parameters.parameterIndex == listOf ("param", "arg"))

        val params2 = pathWith2Parameters.extractParameters("/alfa/abc/tango/def")
        assert (params2 == mapOf ("param" to "abc", "arg" to "def"))
    }

    fun `path with a wildcard resolve parameters properly` () {
        val pathWith1Parameter = Path("/alfa/*/{param}/tango")
        assert (pathWith1Parameter.path == "/alfa/*/{param}/tango")
        assert (pathWith1Parameter.hasParameters)
        assert (pathWith1Parameter.regex?.pattern == "/alfa/(.*?)/(.+?)/tango$")
        assert (pathWith1Parameter.parameterIndex == listOf ("", "param"))

        val pathWith2Parameters = Path("/alfa/{param}/tango/{arg}/*")
        assert (pathWith2Parameters.path == "/alfa/{param}/tango/{arg}/*")
        assert (pathWith2Parameters.hasParameters)
        assert (pathWith2Parameters.regex?.pattern == "/alfa/(.+?)/tango/(.+?)/(.*?)$")
        assert (pathWith2Parameters.parameterIndex == listOf ("param", "arg", ""))
    }

    fun `path with many wildcards resolve parameters properly` () {
        val pathWith1Parameter = Path("/*/alfa/*/{param}/tango")
        assert (pathWith1Parameter.path == "/*/alfa/*/{param}/tango")
        assert (pathWith1Parameter.hasParameters)
        assert (pathWith1Parameter.regex?.pattern == "/(.*?)/alfa/(.*?)/(.+?)/tango$")
        assert (pathWith1Parameter.parameterIndex == listOf ("", "", "param"))

        val pathWith2Parameters = Path("/alfa/*/{param}/tango/{arg}/*")
        assert (pathWith2Parameters.path == "/alfa/*/{param}/tango/{arg}/*")
        assert (pathWith2Parameters.hasParameters)
        assert (pathWith2Parameters.regex?.pattern == "/alfa/(.*?)/(.+?)/tango/(.+?)/(.*?)$")
        assert (pathWith2Parameters.parameterIndex == listOf ("", "param", "arg", ""))
    }

    fun `create an url with path` () {
        val pathWith1Parameter = Path("/alfa/{param}/tango")
        assert (pathWith1Parameter.create ("param" to "bravo") == "/alfa/bravo/tango")

        val pathWith2Parameters = Path("/alfa/{param}/tango/{arg}")
        val url = pathWith2Parameters.create ("param" to "bravo", "arg" to "zulu")
        assert (url == "/alfa/bravo/tango/zulu")
    }

    fun `path with wildcards can not create url` () {
        assertFailsWith<IllegalStateException> {
            Path("/alfa/*/{param}/tango").create("param" to "val")
        }
    }

    fun `segments of a path are splitted correctly` () {
        val segments1 = Path("/alfa/{p1}/beta/{p2}").segments
        assert(segments1 == listOf ("/alfa/", "/beta/", ""))

        val segments2 = Path("/{p0}/alfa/{p1}/beta/{p2}").segments
        assert(segments2 == listOf ("/", "/alfa/", "/beta/", ""))

        val segments3 = Path("/alfa/{p1}/beta").segments
        assert(segments3 == listOf ("/alfa/", "/beta"))
    }
}
