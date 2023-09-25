package com.hexagonkt.http.server

import com.hexagonkt.http.model.HttpProtocol.HTTP
import com.hexagonkt.http.model.HttpProtocol.HTTP2
import org.junit.jupiter.api.condition.DisabledOnOs
import org.junit.jupiter.api.condition.OS.WINDOWS
import org.junit.jupiter.api.Test
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
            assert(it.bindUrl.toString().startsWith("http://"))
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
        }
    }

    @Test
    @DisabledOnOs(WINDOWS) // Hostname is resolved in a different way in Windows
    fun `Custom HTTP server settings contains the proper bind URL`() {
        val bindAddress = InetAddress.getByName("192.168.0.1")
        val settings = HttpServerSettings(zip = true, protocol = HTTP2, bindAddress = bindAddress)
        assertEquals("https://192.168.0.1", settings.bindUrl.toString())
    }
}
