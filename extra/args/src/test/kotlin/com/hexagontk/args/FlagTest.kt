package com.hexagontk.args

import kotlin.test.Test
import kotlin.IllegalArgumentException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class FlagTest {

    @Test fun `Flags with null optional values are correct`() {
        assertEquals(1, Flag(name = "long").names.size)
        assertEquals(Flag('b'), Flag('b', name = null))
        assertEquals(Flag('b'), Flag('b', description = null))
    }

    @Test fun `Invalid flags raise errors`() {
        listOf('#', ' ').forEach {
            assertEquals(
                "Names must comply with ${Option.optionRegex} regex: [$it]",
                assertFailsWith<IllegalArgumentException> { Flag(it) }.message
            )
        }
    }
}
