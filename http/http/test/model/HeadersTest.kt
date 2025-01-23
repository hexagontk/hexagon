package com.hexagontk.http.model

import org.junit.jupiter.api.Test
import kotlin.test.*

internal class HeadersTest {

    @Test
    fun `HTTP headers works correctly`() {
        val fields =
            Headers(Header("a", "b"), Header("b", "c")) +
                Headers(Header("a", 1), Header("b", 0)) +
                Header("a", true) +
                Header("b", false)

        assertEqualHttpFieldLists(
            listOf(
                Header("a", "b"),
                Header("b", "c"),
                Header("a", 1),
                Header("b", 0),
                Header("a", true),
                Header("b", false),
            ),
            fields.fields
        )
        assertFalse(fields.isEmpty())
        assertEquals("b", fields["a"]?.value)
        assertEquals("b", fields["a"]?.text)
        assertEquals("b", fields.getText("a"))
        assertNull(fields["z"])
        assertNull(fields.getText("z"))
        assertEqualHttpFieldLists(
            listOf(Field("a", "b"), Field("a", 1), Field("a", true)), fields.getAll("a")
        )
        assertEquals(listOf("b", "1", "true"), fields.getTexts("a"))
        assertEqualHttpFieldLists(
            listOf(Field("b", "c"), Field("b", 0), Field("b", false)), fields.getAll("b")
        )
        assertEquals(listOf("c", "0", "false"), fields.getTexts("b"))
        assertEquals(emptyList(), fields.getAll("z"))

        assertEquals(0, (fields + Field("c", 0)).require("c").value)
        assertEquals("0", (fields + Field("c", 0)).require("c").text)
        assertEquals("c", (fields + Field("c", 0)).require("c").name)
        assertEquals("0", (fields + Field("c", 0)).requireText("c"))

        assertEqualHttpFieldLists(
            fields + Headers(Header("c", 0), Header("d", 1)),
            fields + Header("c", 0) + Header("d", 1)
        )

        assertEqualHttpFieldLists(fields, (fields + Field("c", 0)) - "c")
    }

    @Test
    fun `HTTP headers works correctly with empty fields`() {
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
        assertEquals("a", hs["X-Accept"]?.value)
        assertEquals("a", hs["x-accept"]?.value)
        assertEquals("a", hs["X-ACCEPT"]?.value)
    }
}
