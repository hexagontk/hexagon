package com.hexagonkt.http.server.async

import com.hexagonkt.handlers.async.done
import com.hexagonkt.http.model.HttpProtocol.H2C
import com.hexagonkt.http.handlers.async.OnHandler
import com.hexagonkt.http.handlers.async.PathHandler
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import java.net.InetAddress.getByName as address

internal class HttpServerTest {

    @Test fun `Default banner includes documentation URL`() {
        assert(HttpServer.banner.contains("https://hexagonkt.com/http_server"))
    }

    @Test fun `Default parameters`() {
        val serverSettings = HttpServerSettings(address("localhost"), 9999, banner = "name")
        val server = HttpServer(com.hexagonkt.http.server.async.VoidAdapter, PathHandler(), serverSettings)

        assertEquals("name", server.settings.banner)
        assertEquals(com.hexagonkt.http.server.async.VoidAdapter.javaClass.simpleName, server.portName)
        assertEquals(address("localhost"), server.settings.bindAddress)
        assertEquals(9999, server.settings.bindPort)
    }

    @Test fun `Runtime port`() {
        val serverSettings = HttpServerSettings(address("localhost"), 9999, banner = "name")
        val server = HttpServer(com.hexagonkt.http.server.async.VoidAdapter, PathHandler(), serverSettings)

        assertFailsWith<IllegalStateException>("Server is not running") { server.runtimePort }
        assert(!server.started())

        server.start()

        assert(server.started())
        assertEquals(12345, server.runtimePort)
    }

    @Test fun `Banner creation`() {
        val bannerPrefix = "Test Banner"
        val serverSettings = HttpServerSettings(
            address("localhost"),
            12345,
            banner = bannerPrefix,
        )

        val banners = listOf(
            serve(com.hexagonkt.http.server.async.VoidAdapter, serverSettings) {},
            serve(com.hexagonkt.http.server.async.VoidAdapter, serverSettings.copy(vmInformation = true)) {}
        )
        .map {
            it.createBanner(System.currentTimeMillis())
        }
        .map {
            assertEquals(bannerPrefix, it.lines()[0].trimIndent())
            assertContains(it, "✅HTTP" )
            assertContains(it, "HTTPS")
            assertFalse(it.contains("ZIP"))
            assertFalse(it.contains("✅HTTPS"))
            it
        }
        assertContains(banners.first(), "(excluding VM)")
        assertFalse(banners.last().contains("(excluding VM)"))
    }

    @Test fun `Banner creation with enabled features and custom options`() {
        val serverSettings = HttpServerSettings(
            address("localhost"),
            12345,
        )

        val server = serve(com.hexagonkt.http.server.async.VoidAdapter, serverSettings) {}
        val createdBanner = server.createBanner(System.currentTimeMillis())
        server.stop()

        assertContains(createdBanner, "✅HTTP" )
        assertContains(createdBanner, "HTTPS")
        assertContains(createdBanner, "SSE")
        assertContains(createdBanner, "option1(1)")
        assertContains(createdBanner, "option2(2)")
        assertFalse(createdBanner.contains("✅HTTPS"))
    }

    @Test fun `Server can not be created with ZIP compression if not supported by its adapter`() {
        assertFailsWith<IllegalStateException> {
            HttpServer(com.hexagonkt.http.server.async.VoidAdapter, OnHandler { this.done() }, HttpServerSettings(zip = true))
        }.let {
            val errorMessage = "Requesting ZIP compression with an adapter without support:"
            assertContains(it.message ?: "", errorMessage)
        }
    }

    @Test fun `Server can not be created with a protocol not supported by its adapter`() {
        assertFailsWith<IllegalStateException> {
            HttpServer(com.hexagonkt.http.server.async.VoidAdapter, OnHandler { this.done() }, HttpServerSettings(protocol = H2C))
        }.let {
            val message = it.message ?: ""
            assertContains(message, "Requesting unsupported protocol. Adapter's protocols:")
        }
    }
}
