package com.hexagontk.http.model

import org.junit.jupiter.api.Test
import kotlin.test.*

// TODO Rename to HttpFieldsTest and test headers outside
internal class HeadersTest {

    @Test
    fun `HTTP headers works correctly`() {
        val fields =
            Headers(Field("a", "b"), Field("b", "c")) +
                Headers(Field("a", 1), Field("b", 0)) +
                Field("a", true) +
                Field("b", false)

        assertContentEquals(
            listOf(
                Field("a", "b"),
                Field("b", "c"),
                Field("a", 1),
                Field("b", 0),
                Field("a", true),
                Field("b", false),
            ),
            fields.fields
        )
        assertFalse(fields.isEmpty())
        assertEquals("b", fields["a"]?.value)
        assertNull(fields["z"])
        assertEquals(listOf(Field("a", "b"), Field("a", 1), Field("a", true)), fields.all("a"))
        assertEquals(listOf(Field("b", "c"), Field("b", 0), Field("b", false)), fields.all("b"))

        assertEquals(0, (fields + Field("c", 0)).require("c").value)
        assertEquals("0", (fields + Field("c", 0)).require("c").text)
        assertEquals("c", (fields + Field("c", 0)).require("c").name)

        assertEquals(
            fields + Headers(Field("c", 0), Field("d", 1)),
            fields + Field("c", 0) + Field("d", 1)
        )

        assertEquals(fields, (fields + Field("c", 0)) - "c")
    }

    @Test
    fun `HTTP fields works correctly with empty fields`() {
        val fields = Headers(
            Field("a"),
            Field("b"),
        )

        assertNull(fields["a"]?.value)
        assertNull(fields["z"])

        assertNull(fields.require("a").value)
        assertFailsWith<IllegalStateException> { fields.require("z") }
    }

    @Test
    fun `Headers can be retrieved in a case insensitive way`() {
        val hs = Headers(Field("X-Accept", "a"), Field("x-ACCEPT", "b"))
        assertEquals("a", hs["X-Accept"]?.value)
        assertEquals("a", hs["x-accept"]?.value)
        assertEquals("a", hs["X-ACCEPT"]?.value)
    }
}
