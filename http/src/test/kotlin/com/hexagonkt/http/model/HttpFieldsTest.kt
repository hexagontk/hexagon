package com.hexagonkt.http.model

import com.hexagonkt.core.require
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class HttpFieldsTest {

    @Test fun `HTTP fields works correctly`() {
        val fields = HttpFields(
            HttpHeader("a", "b", 1, true),
            HttpHeader("b", "c", 0, false),
        )

        assertEquals(HttpHeader("a", "b", 1, true), fields["a"])
        assertEquals(HttpHeader("b", "c", 0, false), fields["b"])

        assertEquals(fields + HttpHeader("b", "c", 0, false), fields)
        assertEquals("0", (fields + HttpHeader("c", 0)).require("c").value)
    }
}
