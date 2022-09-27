package com.hexagonkt.http.server

import com.hexagonkt.http.model.HttpProtocol.HTTP
import com.hexagonkt.http.server.HttpServerFeature.*
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
            assertNull(it.banner)
            assertEquals(emptySet(), it.features)
        }
    }

    @Test fun `Custom HTTP server settings contains the proper values`() {
        HttpServerSettings(features = setOf(ZIP, ASYNC, WEB_SOCKETS)).let {
            assertEquals(InetAddress.getLoopbackAddress(), it.bindAddress)
            assertEquals(2010, it.bindPort)
            assertEquals(HTTP, it.protocol)
            assertNull(it.sslSettings)
            assertNull(it.banner)
            assertEquals(HttpServerFeature.values().toSet(), it.features)
        }
    }
}
