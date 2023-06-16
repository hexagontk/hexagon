package com.hexagonkt.http.server

import com.hexagonkt.http.model.HttpProtocol.HTTP
import com.hexagonkt.http.model.HttpProtocol.HTTP2
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
            assertEquals(HttpServer.banner, it.banner)
            assertEquals(false, it.zip)
            assertEquals("http://localhost", it.base)
        }
    }

    @Test fun `Custom HTTP server settings contains the proper values`() {
        val bindAddress = InetAddress.getByName("192.168.0.1")
        HttpServerSettings(zip = true, protocol = HTTP2, bindAddress = bindAddress).let {
            assertEquals(bindAddress, it.bindAddress)
            assertEquals(2010, it.bindPort)
            assertEquals(HTTP2, it.protocol)
            assertNull(it.sslSettings)
            assertEquals(HttpServer.banner, it.banner)
            assertEquals(true, it.zip)
            assertEquals("https://192.168.0.1", it.base)
        }
    }
}
