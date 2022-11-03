package com.hexagonkt.core.args

import com.hexagonkt.core.allowedTargetTypes
import org.junit.jupiter.api.Test
import java.lang.IllegalArgumentException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class OptionTest {

    @Test fun `Options with null optional values are correct`() {
        assertEquals(Option('b', Boolean::class), Option('b', Boolean::class, longName = null))
        assertEquals(Option('b', Boolean::class), Option('b', Boolean::class, description = null))
    }

    @Test fun `Invalid options raise errors`() {
        listOf('#', ' ').forEach {
            assertEquals(
                "Short name must be a letter or a digit: $it",
                assertFailsWith<IllegalArgumentException> { Option(it, String::class) }.message
            )
        }

        assertEquals(
            "Type ${Regex::class} not in allowed types: $allowedTargetTypes",
            assertFailsWith<IllegalArgumentException> { Option('a', Regex::class) }.message
        )

        listOf("", " ", " b ", "b").forEach {
            val e = assertFailsWith<IllegalArgumentException> { Option('a', String::class, it) }
            val message = e.message ?: ""
            assertEquals("Long name must be at least two characters: $it", message)
        }

        listOf("", " ", "  ").forEach {
            val e = assertFailsWith<IllegalArgumentException> {
                Option('a', String::class, description = it)
            }
            val message = e.message ?: ""
            assertEquals("Description cannot be blank", message)
        }
    }
}
