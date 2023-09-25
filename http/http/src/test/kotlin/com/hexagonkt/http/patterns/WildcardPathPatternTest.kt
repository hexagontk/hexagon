package com.hexagonkt.http.patterns

import org.junit.jupiter.api.Test
import kotlin.test.*

internal class WildcardPathPatternTest {

    @Test fun `Insert parameters on path`() {
        val e = assertFailsWith<IllegalStateException> {
            WildcardPathPattern().insertParameters(mapOf("a" to "b"))
        }
        assertEquals("Wildcard path pattern does not accept parameters", e.message)
    }

    @Test fun `Prefixes are matched if pattern is prefix`() {
        val regexPath = WildcardPathPattern()
        assert(regexPath.matches("/alpha/bravo/tango"))

        val regexPathPrefix = WildcardPathPattern(true)
        assert(regexPathPrefix.matches("/alpha/bravo/tango"))
    }

    @Test fun `Prefixes can be appended to patterns`() {
        val regexPath = WildcardPathPattern()
        assert(regexPath.addPrefix(null).matches("/alpha/bravo"))
        assertFalse(regexPath.addPrefix("/prefix").matches("/alpha/bravo"))
        assertTrue(regexPath.addPrefix("/prefix").matches("/prefix/alpha/bravo"))
    }

    @Test fun `A wildcard path have a single parameter`() {
        val pathWithoutData = WildcardPathPattern()
        assertEquals("*", pathWithoutData.pattern)
        assertEquals(mapOf("1" to "/alpha"), pathWithoutData.extractParameters("/alpha"))
    }

    @Test fun `Adding a prefix keep the prefix flag`() {
        WildcardPathPattern(true).addPrefix("/b").let {
            assertEquals("/b*", it.pattern)
            assertTrue(it.prefix)
        }
        WildcardPathPattern(false).addPrefix("/b").let {
            assertEquals("/b*", it.pattern)
            assertFalse(it.prefix)
        }
    }
}
