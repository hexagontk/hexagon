package com.hexagonkt.http.model

import org.junit.jupiter.api.Test
import kotlin.test.*

internal class HttpFieldsTest {

    @Test fun `HTTP fields works correctly`() {
        val fields = HttpFields(
            Header("a", "b", 1, true),
            Header("b", "c", 0, false),
        )

        assertEquals(
            listOf("a" to "b", "a" to "1", "a" to "true", "b" to "c", "b" to "0", "b" to "false"),
            fields.allPairs
        )
        assertEquals(
            mapOf("a" to listOf("b", "1", "true"), "b" to listOf("c", "0", "false")),
            fields.allValues
        )
        assertFalse(fields.isEmpty())
        assertEquals("b", fields["a"])
        assertNull(fields["z"])
        assertEquals(Header("a", "b", 1, true), fields.httpFields["a"])
        assertEquals(Header("b", "c", 0, false), fields.httpFields["b"])

        assertEquals(fields + Header("b", "c", 0, false), fields)
        assertEquals("0", (fields + Header("c", 0)).require("c"))

        assertEquals(
            fields + HttpFields(Header("c", 0), Header("d", 1)),
            fields + Header("c", 0) + Header("d", 1)
        )

        assertEquals(fields, (fields + Header("c", 0)) - "c")
    }

    @Test fun `HTTP fields works correctly with empty fields`() {
        val fields = HttpFields(
            Header("a"),
            Header("b"),
        )

        assertNull(fields["a"])
        assertNull(fields["z"])

        assertTrue(fields.values.isEmpty())
        assertEquals("5", (fields + Header("c", 5)).values["c"])

        assertFailsWith<NoSuchElementException> { fields.require("a") }
        assertFailsWith<IllegalStateException> { fields.require("z") }
    }
}
