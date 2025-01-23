package com.hexagontk.http.model

import com.hexagontk.http.model.HttpProtocol.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class HttpProtocolTest {

    @Test fun `HTTP protocols are tested (only for coverage)`() {
        assertEquals("http", HTTP.schema)
        assertEquals("https", HTTPS.schema)
        assertEquals("https", HTTP2.schema)
        assertEquals("http", H2C.schema)
        assertFalse(HTTP.secure)
        assertTrue(HTTPS.secure)
        assertTrue(HTTP2.secure)
        assertFalse(H2C.secure)
    }
}
