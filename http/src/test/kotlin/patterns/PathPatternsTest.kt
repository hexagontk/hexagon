package com.hexagonkt.http.patterns

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class PathPatternsTest {

    @Test fun `'createPattern' creates the proper pattern instance`() {
        assertEquals(LiteralPathPattern("/a", true), createPathPattern("/a", true))
        assertEquals(LiteralPathPattern("/a", false), createPathPattern("/a", false))

        assertEquals(TemplatePathPattern("/(a|b)", true), createPathPattern("/(a|b)", true))
        assertEquals(TemplatePathPattern("/(a|b)", false), createPathPattern("/(a|b)", false))

        assertEquals(TemplatePathPattern("/*/a", true), createPathPattern("/*/a", true))
        assertEquals(TemplatePathPattern("/*/a", false), createPathPattern("/*/a", false))
    }

    @Test fun `Path patterns are described correctly`() {
        assertEquals("LiteralPathPattern (PREFIX) /a", LiteralPathPattern("/a", true).describe())
        assertEquals("RegexPathPattern /(a|b)$", TemplatePathPattern("/(a|b)", false).describe())
    }
}
