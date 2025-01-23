package com.hexagontk.http.model

import org.junit.jupiter.api.Test
import kotlin.test.*

internal class ParametersTest {

    @Test fun `HTTP parameters works correctly`() {
        val fields =
            Parameters(Parameter("a", "b"), Parameter("b", "c")) +
                Parameters(Parameter("a", 1), Parameter("b", 0)) +
                Parameter("a", true) +
                Parameter("b", false)

        assertEqualHttpFieldLists(
            listOf(
                Parameter("a", "b"),
                Parameter("b", "c"),
                Parameter("a", 1),
                Parameter("b", 0),
                Parameter("a", true),
                Parameter("b", false),
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
            fields + Parameters(Parameter("c", 0), Parameter("d", 1)),
            fields + Parameter("c", 0) + Parameter("d", 1)
        )

        assertEqualHttpFieldLists(fields, (fields + Field("c", 0)) - "c")
    }

    @Test fun `HTTP parameters works correctly with empty fields`() {
        val fields = Parameters(
            Parameter("a"),
            Parameter("b"),
        )

        assertNull(fields["a"]?.value)
        assertNull(fields["z"])

        assertNull(fields.require("a").value)
        assertFailsWith<IllegalStateException> { fields.require("z") }
    }

    @Test fun `Parameters can be retrieved in a case sensitive way`() {
        val hs = Parameters(Parameter("X-Accept", "a"), Parameter("x-ACCEPT", "b"))
        assertEquals("a", hs["X-Accept"]?.value)
        assertEquals("b", hs["x-ACCEPT"]?.value)
        assertNull(hs["X-ACCEPT"])
    }
}
