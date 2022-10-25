package com.hexagonkt.http.model

import com.hexagonkt.core.require
import org.junit.jupiter.api.Test
import kotlin.test.*

internal class FormParametersTest {

    @Test fun `HTTP form parameters works correctly`() {
        val fields = FormParameters(
            FormParameter("a", "b", 1, true),
            FormParameter("b", "c", 0, false),
        )

        assertContentEquals(
            listOf(FormParameter("a", "b", 1, true), FormParameter("b", "c", 0, false)),
            fields.values
        )
        assertFalse(fields.isEmpty())
        assertEquals("b", fields["a"]?.value)
        assertNull(fields["z"])
        assertEquals(FormParameter("a", "b", 1, true), fields.httpFields["a"])
        assertEquals(FormParameter("b", "c", 0, false), fields.httpFields["b"])

        assertEquals(fields + FormParameter("b", "c", 0, false), fields)
        assertEquals("0", (fields + FormParameter("c", 0)).require("c").value)

        assertEquals(
            fields + FormParameters(FormParameter("c", 0), FormParameter("d", 1)),
            fields + FormParameter("c", 0) + FormParameter("d", 1)
        )

        assertEquals(fields, (fields + FormParameter("c", 0)) - "c")
    }

    @Test fun `HTTP fields works correctly with empty fields`() {
        val fields = FormParameters(
            FormParameter("a"),
            FormParameter("b"),
        )

        assertNull(fields["a"]?.value)
        assertNull(fields["z"])

        assertNull(fields.require("a").value)
        assertFailsWith<IllegalStateException> { fields.require("z") }
    }

    @Test fun `Form parameters can be retrieved in a case sensitive way`() {
        val hs = FormParameters(FormParameter("X-Accept", "a"), FormParameter("x-ACCEPT", "b"))
        assertEquals("a", hs["X-Accept"]?.value)
        assertEquals("b", hs["x-ACCEPT"]?.value)
        assertNull(hs["X-ACCEPT"])
    }
}
