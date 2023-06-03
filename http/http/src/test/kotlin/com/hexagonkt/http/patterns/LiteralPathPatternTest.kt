package com.hexagonkt.http.patterns

import kotlin.test.Test
import kotlin.test.*

internal class LiteralPathPatternTest {

    @Test fun `Insert parameters on path`() {
        val e = assertFailsWith<IllegalStateException> {
            LiteralPathPattern("/alpha/bravo").insertParameters(mapOf("a" to "b"))
        }
        assertEquals("Literal path pattern does not accept parameters", e.message)
    }

    @Test fun `Prefixes are matched if pattern is prefix`() {
        val regexPath = LiteralPathPattern("/alpha/bravo")
        assertFalse(regexPath.matches("/alpha/bravo/tango"))

        val regexPathPrefix = LiteralPathPattern("/alpha/bravo", true)
        assert(regexPathPrefix.matches("/alpha/bravo"))
        assert(regexPathPrefix.matches("/alpha/bravo/tango"))
        assertFalse(regexPathPrefix.matches("/prefix/alpha/bravo"))
    }

    @Test fun `Prefixes can be appended to patterns`() {
        val regexPath = LiteralPathPattern("/alpha/bravo")
        assert(regexPath.addPrefix(null).matches("/alpha/bravo"))
        assertFalse(regexPath.addPrefix("/prefix").matches("/alpha/bravo"))
        assertTrue(regexPath.addPrefix("/prefix").matches("/prefix/alpha/bravo"))
    }

    @Test fun `A path without parameters do not have params table`() {
        val pathWithoutData = LiteralPathPattern("/alpha/bravo/tango")
        assertEquals("/alpha/bravo/tango", pathWithoutData.pattern)
        assert(pathWithoutData.extractParameters("/alpha/bravo/tango").isEmpty())

        assert(pathWithoutData.matches("/alpha/bravo/tango"))
        assert(!pathWithoutData.matches("/alpha/bravo/tango/zulu"))
        assert(!pathWithoutData.matches("/zulu/alpha/bravo/tango"))

        assert(pathWithoutData.extractParameters("/alpha/bravo/tango").isEmpty())
    }

    @Test fun `Empty path patterns are allowed`() {
        assertEquals("", LiteralPathPattern("").pattern)
    }

    @Test fun `Invalid path parameters`() {
        assertFailsWith<IllegalArgumentException> {
            LiteralPathPattern("alpha/bravo")
        }
        assertFailsWith<IllegalArgumentException> {
            LiteralPathPattern("*/bravo")
        }
    }

    @Test fun `Extract parameter from a pattern without parameters returns empty map`() {
        val pathWithoutData = LiteralPathPattern("/alpha/bravo/tango")
        assertEquals(emptyMap(), pathWithoutData.extractParameters("/alpha/bravo/tango/zulu"))
    }

    @Test fun `Path without parameters does not return any parameter`() {
        LiteralPathPattern("/alpha/tango").let {
            assertEquals("/alpha/tango", it.pattern)
            assert(it.extractParameters("/alpha/tango").isEmpty())
        }
    }

    @Test fun `Adding a prefix keep the prefix flag`() {
        LiteralPathPattern("/a", true).addPrefix("/b").let {
            assertEquals("/b/a", it.pattern)
            assertTrue(it.prefix)
        }
        LiteralPathPattern("/a", false).addPrefix("/b").let {
            assertEquals("/b/a", it.pattern)
            assertFalse(it.prefix)
        }
    }
}
