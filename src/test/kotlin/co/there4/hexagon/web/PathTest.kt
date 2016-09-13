package co.there4.hexagon.web

import org.testng.annotations.Test
import kotlin.test.assertFailsWith

/**
 * TODO Check that URLs with blank parameters are not matched.
 * TODO Ie: /alfa/{param}/bravo is not matched by /alfa//bravo
 */
@Test class PathTest {
    fun a_path_without_parameters_do_not_have_regex_neither_params_table () {
        val pathWithoutData = Path("/alfa/bravo/tango")
        assert (pathWithoutData.path == "/alfa/bravo/tango")
        assert (!pathWithoutData.hasParameters)
        assert (pathWithoutData.regex == null)
        assert (pathWithoutData.parameterIndex.size == 0)

        assert (pathWithoutData.matches ("/alfa/bravo/tango"))
        assert (!pathWithoutData.matches ("/alfa/bravo/tango/zulu"))
        assert (!pathWithoutData.matches ("/zulu/alfa/bravo/tango"))

        assert (pathWithoutData.extractParameters ("/alfa/bravo/tango").size == 0)
    }


    fun extract_parameter_from_a_non_matching_url_fails () {
        val pathWithoutData = Path("/alfa/bravo/tango")
        assertFailsWith<IllegalArgumentException> ("URL '/alfa/bravo/tango/zulu' does not match path") {
            pathWithoutData.extractParameters ("/alfa/bravo/tango/zulu")
        }

        val pathWith1Parameter = Path("/alfa/{param}/tango")
        assertFailsWith<IllegalArgumentException> ("URL 'zulu/alfa/abc/tango' does not match path") {
            assert (pathWith1Parameter.extractParameters ("zulu/alfa/abc/tango").size == 0)
        }
    }

    fun a_path_with_parameters_have_regex_and_params_table () {
        val pathWith1Parameter = Path("/alfa/{param}/tango")
        assert (pathWith1Parameter.path == "/alfa/{param}/tango")
        assert (pathWith1Parameter.hasParameters)
        assert (pathWith1Parameter.regex?.pattern == "/alfa/(.+?)/tango$")
        assert (pathWith1Parameter.parameterIndex == listOf ("param"))

        assert (pathWith1Parameter.matches ("/alfa/a/tango"))
        assert (pathWith1Parameter.matches ("/alfa/abc/tango"))
        assert (!pathWith1Parameter.matches ("/alfa//tango"))
        assert (!pathWith1Parameter.matches ("/alfa/tango"))
        assert (!pathWith1Parameter.matches ("/alfa/a/tango/zulu"))
        assert (!pathWith1Parameter.matches ("zulu/alfa/abc/tango"))

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

    fun path_with_a_wildcard_resolve_parameters_properly () {
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

    fun path_with_many_wildcards_resolve_parameters_properly () {
        val pathWith1Parameter = Path("*/alfa/*/{param}/tango")
        assert (pathWith1Parameter.path == "*/alfa/*/{param}/tango")
        assert (pathWith1Parameter.hasParameters)
        assert (pathWith1Parameter.regex?.pattern == "(.*?)/alfa/(.*?)/(.+?)/tango$")
        assert (pathWith1Parameter.parameterIndex == listOf ("", "", "param"))

        val pathWith2Parameters = Path("/alfa/*/{param}/tango/{arg}/*")
        assert (pathWith2Parameters.path == "/alfa/*/{param}/tango/{arg}/*")
        assert (pathWith2Parameters.hasParameters)
        assert (pathWith2Parameters.regex?.pattern == "/alfa/(.*?)/(.+?)/tango/(.+?)/(.*?)$")
        assert (pathWith2Parameters.parameterIndex == listOf ("", "param", "arg", ""))
    }

    fun create_a_url_with_path () {
        val pathWith1Parameter = Path("/alfa/{param}/tango")
        assert (pathWith1Parameter.create ("param" to "bravo") == "/alfa/bravo/tango")

        val pathWith2Parameters = Path("/alfa/{param}/tango/{arg}")
        val url = pathWith2Parameters.create ("param" to "bravo", "arg" to "zulu")
        assert (url == "/alfa/bravo/tango/zulu")
    }

    fun path_with_wildcards_can_not_create_url () {
        assertFailsWith<IllegalStateException> {
            Path("/alfa/*/{param}/tango").create("param" to "val")
        }
    }

    fun segments_of_a_path_are_splitted_correctly () {
        val segments1 = Path("/alfa/{p1}/beta/{p2}").segments
        assert(segments1 == listOf ("/alfa/", "/beta/", ""))

        val segments2 = Path("{p0}/alfa/{p1}/beta/{p2}").segments
        assert(segments2 == listOf ("", "/alfa/", "/beta/", ""))

        val segments3 = Path("/alfa/{p1}/beta").segments
        assert(segments3 == listOf ("/alfa/", "/beta"))
    }
}
