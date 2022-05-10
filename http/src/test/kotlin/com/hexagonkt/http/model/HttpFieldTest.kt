package com.hexagonkt.http.model

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class HttpFieldTest {

    @Test fun `HTTP fields operators work properly`() {
        val header = HttpHeader("name", "v1")
        assertEquals("name", header.name)
        assertNull((header - "v1").value)
        assert((header - "v1").values.isEmpty())
        assertEquals(listOf("v1"), header.values)
        assertEquals("v1", header.value)
        assertEquals(listOf("v1", "v2", "v3"), header.values + "v2" + "v3")
        assertEquals(listOf("v1", "v2", 1, true), header.values + "v2" + 1 + true)

        val queryParameter = HttpQueryParameter("name", "v1")
        assertEquals("name", queryParameter.name)
        assertNull((queryParameter - "v1").value)
        assert((queryParameter - "v1").values.isEmpty())
        assertEquals(listOf("v1"), queryParameter.values)
        assertEquals("v1", queryParameter.value)
        assertEquals(listOf("v1", "v2", "v3"), queryParameter.values + "v2" + "v3")
        assertEquals(listOf("v1", "v2", 1, true), queryParameter.values + "v2" + 1 + true)

        val formParameter = HttpFormParameter("name", "v1")
        assertEquals("name", formParameter.name)
        assertNull((formParameter - "v1").value)
        assert((formParameter - "v1").values.isEmpty())
        assertEquals(listOf("v1"), formParameter.values)
        assertEquals("v1", formParameter.value)
        assertEquals(listOf("v1", "v2", "v3"), formParameter.values + "v2" + "v3")
        assertEquals(listOf("v1", "v2", 1, true), formParameter.values + "v2" + 1 + true)
    }
}
