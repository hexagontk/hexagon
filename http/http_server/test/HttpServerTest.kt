package com.hexagontk.http.server

import com.hexagontk.http.model.HttpProtocol.H2C
import com.hexagontk.http.handlers.OnHandler
import com.hexagontk.http.handlers.PathHandler
import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import java.net.InetAddress.getByName as address

internal class HttpServerTest {

    @Test fun `Default parameters`() {
        val serverSettings = HttpServerSettings(address("localhost"), 9999)
        val server = HttpServer(VoidHttpServer, PathHandler(), serverSettings)

        assertEquals(VoidHttpServer.javaClass.simpleName, server.portName)
        assertEquals(address("localhost"), server.settings.bindAddress)
        assertEquals(9999, server.settings.bindPort)
    }

    @Test fun `Runtime port`() {
        val serverSettings = HttpServerSettings(address("localhost"), 9999)
        val server = HttpServer(VoidHttpServer, PathHandler(), serverSettings)

        assertFailsWith<IllegalStateException>("Server is not running") { server.runtimePort }
        assert(!server.started())

        server.start()

        assert(server.started())
        assertEquals(12345, server.runtimePort)
    }

    @Test fun `Banner creation`() {
        val serverSettings = HttpServerSettings(
            address("localhost"),
            12345,
        )

        val s = serve(VoidHttpServer, serverSettings) {}
        s.createBanner(0).let {
            assert(it.contains(" ms"))
            assertContains(it, "✅HTTP")
            assertContains(it, "HTTPS")
            assertFalse(it.contains("ZIP"))
            assertFalse(it.contains("✅HTTPS"))
        }
    }

    @Test fun `Detailed banner creation with enabled features and custom options`() {
        val serverSettings = HttpServerSettings(
            address("localhost"),
            12345,
        )

        val server = serve(VoidHttpServer, serverSettings) {}
        val createdBanner = server.createBanner(System.currentTimeMillis(), detailed = true)
        server.stop()

        assertContains(createdBanner, "✅HTTP" )
        assertContains(createdBanner, "HTTPS")
        assertContains(createdBanner, "SSE")
        assertContains(createdBanner, "option1(1)")
        assertContains(createdBanner, "option2(2)")
        assertFalse(createdBanner.contains("✅HTTPS"))
    }

    @Test fun `Brief banner creation with enabled features and custom options`() {
        val serverSettings = HttpServerSettings(
            address("localhost"),
            12345,
        )

        val server = serve(VoidHttpServer, serverSettings) {}
        val createdBanner = server.createBanner(System.currentTimeMillis(), detailed = false)
        server.stop()

        assertContains(createdBanner, "✅HTTP" )
        assertContains(createdBanner, "HTTPS")
        assert(!createdBanner.contains("SSE"))
        assert(!createdBanner.contains("option1(1)"))
        assert(!createdBanner.contains("option2(2)"))
        assertFalse(createdBanner.contains("✅HTTPS"))
    }

    @Test fun `Server can not be created with ZIP compression if not supported by its adapter`() {
        assertFailsWith<IllegalStateException> {
            HttpServer(VoidHttpServer, OnHandler { this }, HttpServerSettings(zip = true))
        }.let {
            val errorMessage = "Requesting ZIP compression with an adapter without support:"
            assertContains(it.message ?: "", errorMessage)
        }
    }

    @Test fun `Server can not be created with a protocol not supported by its adapter`() {
        assertFailsWith<IllegalStateException> {
            HttpServer(VoidHttpServer, OnHandler { this }, HttpServerSettings(protocol = H2C))
        }.let {
            val message = it.message ?: ""
            assertContains(message, "Requesting unsupported protocol. Adapter's protocols:")
        }
    }
}
