package com.hexagonkt.http.model

import com.hexagonkt.core.require
import org.junit.jupiter.api.Test
import kotlin.test.*

internal class HeadersTest {

    @Test
    fun `HTTP headers works correctly`() {
        val fields = Headers(
            Header("a", "b", 1, true),
            Header("b", "c", 0, false),
        )

        assertContentEquals(
            listOf(Header("a", "b", 1, true), Header("b", "c", 0, false)),
            fields.values
        )
        assertFalse(fields.isEmpty())
        assertEquals("b", fields["a"]?.value)
        assertNull(fields["z"])
        assertEquals(Header("a", "b", 1, true), fields.httpFields["a"])
        assertEquals(Header("b", "c", 0, false), fields.httpFields["b"])

        assertEquals(fields + Header("b", "c", 0, false), fields)
        assertEquals("0", (fields + Header("c", 0)).require("c").value)

        assertEquals(
            fields + Headers(Header("c", 0), Header("d", 1)),
            fields + Header("c", 0) + Header("d", 1)
        )

        assertEquals(fields, (fields + Header("c", 0)) - "c")
    }

    @Test
    fun `HTTP fields works correctly with empty fields`() {
        val fields = Headers(
            Header("a"),
            Header("b"),
        )

        assertNull(fields["a"]?.value)
        assertNull(fields["z"])

        assertNull(fields.require("a").value)
        assertFailsWith<IllegalStateException> { fields.require("z") }
    }

    @Test
    fun `Headers can be retrieved in a case insensitive way`() {
        val hs = Headers(Header("X-Accept", "a"), Header("x-ACCEPT", "b"))
        assertEquals("b", hs["X-Accept"]?.value)
        assertEquals("b", hs["x-accept"]?.value)
        assertEquals("b", hs["X-ACCEPT"]?.value)
    }
}
