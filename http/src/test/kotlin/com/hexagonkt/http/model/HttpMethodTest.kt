package com.hexagonkt.http.model

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class HttpMethodTest {

    @Test fun `HTTP methods are tested (only for coverage)`() {
        assertEquals("DELETE", HttpMethod.DELETE.toString())
        assertEquals("GET", HttpMethod.GET.toString())
        assertEquals("HEAD", HttpMethod.HEAD.toString())
        assertEquals("OPTIONS", HttpMethod.OPTIONS.toString())
        assertEquals("PATCH", HttpMethod.PATCH.toString())
        assertEquals("POST", HttpMethod.POST.toString())
        assertEquals("PUT", HttpMethod.PUT.toString())
        assertEquals("TRACE", HttpMethod.TRACE.toString())
    }
}
