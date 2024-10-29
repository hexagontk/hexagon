package com.hexagontk.http.model

import org.junit.jupiter.api.Test
import kotlin.test.*

internal class QueryParametersTest {

//    @Test fun `HTTP query parameters works correctly`() {
//        val fields = HttpCaseFields<QueryParameter>(
//            QueryParameter("a", "b", 1) + true,
//            QueryParameter("b", "c", 0) + false,
//        )
//
//        assertContentEquals(
//            listOf(QueryParameter("a", "b", 1, true), QueryParameter("b", "c", 0, false)),
//            fields.values
//        )
//        assertFalse(fields.isEmpty())
//        assertEquals("b", fields["a"]?.value)
//        assertNull(fields["z"])
//        assertEquals(QueryParameter("a", "b", 1, true), fields.httpFields["a"])
//        assertEquals(QueryParameter("b", "c", 0, false), fields.httpFields["b"])
//
//        assertEquals(fields + QueryParameter("b", "c", 0, false), fields)
//        assertEquals("0", (fields + QueryParameter("c", 0)).require("c").value)
//
//        assertEquals(
//            fields + HttpCaseFields<QueryParameter>(QueryParameter("c", 0), QueryParameter("d", 1)),
//            fields + QueryParameter("c", 0) + QueryParameter("d", 1)
//        )
//
//        assertEquals(fields, (fields + QueryParameter("c", 0)) - "c")
//    }

    @Test fun `HTTP query parameters works correctly with empty fields`() {
        val fields = Headers(
            Field("a"),
            Field("b"),
        )

        assertNull(fields["a"]?.value)
        assertNull(fields["z"])

        assertNull(fields.require("a").value)
        assertFailsWith<IllegalStateException> { fields.require("z") }
    }

    @Test fun `Query parameters can be retrieved in a case sensitive way`() {
        val hs = Parameters(Field("X-Accept", "a"), Field("x-ACCEPT", "b"))
        assertEquals("a", hs["X-Accept"]?.value)
        assertEquals("b", hs["x-ACCEPT"]?.value)
        assertNull(hs["X-ACCEPT"])
    }
}
