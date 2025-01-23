package com.hexagontk.http.patterns

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class PathPatternsTest {

    @Test fun `'createPattern' creates the proper pattern instance`() {
        assertEqualPathPatterns(LiteralPathPattern("/a", true), createPathPattern("/a", true))
        assertEqualPathPatterns(LiteralPathPattern("/a", false), createPathPattern("/a", false))

        assertEqualPathPatterns(
            TemplatePathPattern("/(a|b)", true),
            createPathPattern("/(a|b)", true)
        )
        assertEqualPathPatterns(
            TemplatePathPattern("/(a|b)", false),
            createPathPattern("/(a|b)", false)
        )

        assertEqualPathPatterns(TemplatePathPattern("/*/a", true), createPathPattern("/*/a", true))
        assertEqualPathPatterns(
            TemplatePathPattern("/*/a", false),
            createPathPattern("/*/a", false)
        )
    }

    @Test fun `Path patterns are described correctly`() {
        assertEquals("Literal (PREFIX) '/a'", LiteralPathPattern("/a", true).describe())
        assertEquals("Regex '/(a|b)$'", TemplatePathPattern("/(a|b)", false).describe())
    }

    private fun assertEqualPathPatterns(a: PathPattern, b: PathPattern) {
        if (a.javaClass != b.javaClass) assert(false)
        if (a.pattern != b.pattern) assert(false)
        if (a.prefix != b.prefix) assert(false)
    }
}
