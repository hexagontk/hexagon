package com.hexagonkt.http.model

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class HttpFieldTest {

    @Test fun `Empty HTTP fields values are fetched correctly`() {
        val header = Header("name")
        assertEquals("name", header.name)
        assertEquals(emptyList(), header.values)
        assertNull(header.value)
    }

    @Test fun `HTTP fields operators work properly`() {
        val header = Header("name", "v1")
        assertEquals("name", header.name)
        assert((header - "v1").values.isEmpty())
        assertEquals(listOf("v1"), header.values)
        assertEquals("v1", header.value)
        assertEquals("v1", header.string())
        assertEquals(listOf("v1", "v2", "v3"), header.values + "v2" + "v3")
        assertEquals(listOf("v1", "v2", 1, true), header.values + "v2" + 1 + true)
        assertEquals(listOf("v1", "v2", "v3"), header.strings() + "v2" + "v3")
        assertEquals(listOf("v1", "v2", 1, true), header.strings() + "v2" + 1 + true)

        val queryParameter = QueryParameter("name", "v1")
        assertEquals("name", queryParameter.name)
        assertEquals("v1", queryParameter.value)
        assertEquals("v1", queryParameter.string())
        assert((queryParameter - "v1").values.isEmpty())
        assertEquals(listOf("v1"), queryParameter.values)
        assertEquals(listOf("v1", "v2", "v3"), queryParameter.values + "v2" + "v3")
        assertEquals(listOf("v1", "v2", 1, true), queryParameter.values + "v2" + 1 + true)
        assertEquals(listOf("v1", "v2", "v3"), queryParameter.strings() + "v2" + "v3")
        assertEquals(listOf("v1", "v2", 1, true), queryParameter.strings() + "v2" + 1 + true)

        val formParameter = FormParameter("name", "v1")
        assertEquals("name", formParameter.name)
        assertEquals("v1", formParameter.value)
        assertEquals("v1", formParameter.string())
        assert((formParameter - "v1").values.isEmpty())
        assertEquals(listOf("v1"), formParameter.values)
        assertEquals(listOf("v1", "v2", "v3"), formParameter.values + "v2" + "v3")
        assertEquals(listOf("v1", "v2", 1, true), formParameter.values + "v2" + 1 + true)
        assertEquals(listOf("v1", "v2", "v3"), formParameter.strings() + "v2" + "v3")
        assertEquals(listOf("v1", "v2", 1, true), formParameter.strings() + "v2" + 1 + true)
    }
}
