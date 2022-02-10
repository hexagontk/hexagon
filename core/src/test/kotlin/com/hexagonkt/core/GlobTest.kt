package com.hexagonkt.core

import org.junit.jupiter.api.Test
import java.lang.IllegalArgumentException
import java.util.regex.PatternSyntaxException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class GlobTest {

    @Test fun `Glob matches complying texts`() {

        val cases = mapOf(
            """a""" to listOf("a"),
            """\b""" to listOf("b"), // Escaping normal characters matches the character
            """.()+|^$@%""" to listOf(".()+|^$@%"), // Normal characters in glob, special in regex
            """ * """ to listOf("  ", " a ", " ab ", " a b "), // Spaces are taken into account
            """*""" to listOf("", " ", "a", "ab", "*"),
            """\*""" to listOf("*"),
            """?""" to listOf(" ", "a", "b", "?", "*"),
            """\?""" to listOf("?"),
            """\""" to listOf(""), // Only '' should match (not completed escape character)
            """\\""" to listOf("""\"""),
            """\{""" to listOf("{"),
            """}""" to listOf("}"),
            """\}""" to listOf("}"),
            """{}""" to listOf(""),
            """{\}}""" to listOf("}"),
            """,""" to listOf(","),
            """\,""" to listOf(","),
            """{\,}""" to listOf(","),
            """{a,b}""" to listOf("a", "b"),
            """log?.*.{log,txt}""" to listOf("log1.x.log", "log_..log", "log1.x.txt", "log_..txt"),
            """{*.log,*.txt}""" to listOf("a.log", "b.log", "a.txt", "b.txt"),
            """f?.{*.log,*.txt}""" to listOf("f1.a.log", "f2.b.log", "f3.a.txt", "f4.b.txt"),
        )

        cases.forEach { (pattern, samples) ->
            samples.forEach { sample ->
                val glob = Glob(pattern)
                val re = glob.regex.pattern
                assert(glob.matches(sample)) { "'$pattern' should match '$sample'. Regex '$re'" }
            }
        }
    }

    @Test fun `Glob does not match not complying texts`() {

        val cases = mapOf(
            """a""" to listOf("b", "*", "", " "),
            """\b""" to listOf("a", "*", "", " "),
            """.""" to listOf("a", "0", "_"),
            """ * """ to listOf(" "),
            """\*""" to listOf("a", "0", "_"),
            """?""" to listOf("  ", "a ", "b ", " ?", " *"),
            """\?""" to listOf("a", "0", "_"),
            """\""" to listOf(" ", "a"),
            """{}""" to listOf(" ", "a", "ab"),
            """{a,b}""" to listOf("ab", "ba"),
            """log?.*.{log,txt}""" to listOf(
                "_log1.x.log",
                "log_.log",
                "log1.x.txt1",
                "log_..txt1"
            ),
            """{*.log,*.txt}""" to listOf("a.log1", "b. log", "a.txt "),
            """f?.{*.log,*.txt}""" to listOf("f.a.log", "f2b.log"),
        )

        cases.forEach { (pattern, samples) ->
            samples.forEach { sample ->
                val glob = Glob(pattern)
                val re = glob.regex.pattern
                assert(!glob.matches(sample)) {
                    "'$pattern' should NOT match '$sample'. Regex '$re'"
                }
            }
        }
    }

    @Test fun `Not correct glob patterns throw an exception`() {

        val cases = listOf(
            """{""",
            """{\}""",
            """{{}\}""",
            """{{\}}""",
        )

        cases.forEach { pattern ->
            val e = assertFailsWith<IllegalArgumentException>("'$pattern' should raise an error") {
                Glob(pattern)
            }

            assertEquals("Pattern: '$pattern' is not a valid Glob", e.message)
            assert(e.cause is PatternSyntaxException)
        }
    }
}
