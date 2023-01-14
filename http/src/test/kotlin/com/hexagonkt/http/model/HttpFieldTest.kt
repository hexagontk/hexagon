package com.hexagonkt.http.model

import kotlin.test.Test
import kotlin.test.assertEquals

internal class HttpFieldTest {

    @Test fun `HTTP fields operators work properly`() {
        val header = Header("name", "v1")
        assertEquals("name", header.name)
        assert((header - "v1").values.isEmpty())
        assertEquals(listOf("v1"), header.values)
        assertEquals(listOf("v1", "v2", "v3"), header.values + "v2" + "v3")
        assertEquals(listOf("v1", "v2", 1, true), header.values + "v2" + 1 + true)

        val queryParameter = QueryParameter("name", "v1")
        assertEquals("name", queryParameter.name)
        assert((queryParameter - "v1").values.isEmpty())
        assertEquals(listOf("v1"), queryParameter.values)
        assertEquals(listOf("v1", "v2", "v3"), queryParameter.values + "v2" + "v3")
        assertEquals(listOf("v1", "v2", 1, true), queryParameter.values + "v2" + 1 + true)

        val formParameter = FormParameter("name", "v1")
        assertEquals("name", formParameter.name)
        assert((formParameter - "v1").values.isEmpty())
        assertEquals(listOf("v1"), formParameter.values)
        assertEquals(listOf("v1", "v2", "v3"), formParameter.values + "v2" + "v3")
        assertEquals(listOf("v1", "v2", 1, true), formParameter.values + "v2" + 1 + true)
    }
}
