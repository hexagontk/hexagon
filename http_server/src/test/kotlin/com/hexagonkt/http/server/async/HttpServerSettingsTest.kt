package com.hexagonkt.http.server.async

import com.hexagonkt.http.model.HttpProtocol.HTTP
import kotlin.test.Test
import java.net.InetAddress
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class HttpServerSettingsTest {

    @Test fun `Default HTTP server settings contains the proper values`() {
        HttpServerSettings().let {
            assertEquals(InetAddress.getLoopbackAddress(), it.bindAddress)
            assertEquals(2010, it.bindPort)
            assertEquals(HTTP, it.protocol)
            assertNull(it.sslSettings)
            assertNull(it.banner)
            assertEquals(false, it.zip)
        }
    }

    @Test fun `Custom HTTP server settings contains the proper values`() {
        HttpServerSettings(zip = true).let {
            assertEquals(InetAddress.getLoopbackAddress(), it.bindAddress)
            assertEquals(2010, it.bindPort)
            assertEquals(HTTP, it.protocol)
            assertNull(it.sslSettings)
            assertNull(it.banner)
            assertEquals(true, it.zip)
        }
    }
}
