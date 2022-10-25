package com.hexagonkt.http.model

import com.hexagonkt.core.require
import org.junit.jupiter.api.Test
import kotlin.test.*

internal class HttpFieldsTest {

    @Test fun `HTTP fields works correctly`() {
        val fields = HttpFields(
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

        assertNull(fields["a"]?.value)
        assertNull(fields["z"])

        assertNull(fields.require("a").value)
        assertFailsWith<IllegalStateException> { fields.require("z") }
    }
}
