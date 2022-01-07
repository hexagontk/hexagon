package com.hexagonkt.http.model

import com.hexagonkt.http.model.HttpProtocol.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class HttpProtocolTest {

    @Test fun `HTTP protocols are tested (only for coverage)`() {
        assertEquals("http", HTTP.schema)
        assertEquals("https", HTTPS.schema)
        assertEquals("https", HTTP2.schema)
        assertEquals("ws", WS.schema)
        assertEquals("wss", WSS.schema)
        assertEquals("http", H2C.schema)
    }
}
