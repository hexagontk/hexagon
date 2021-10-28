package com.hexagonkt.http.server

import com.hexagonkt.http.server.ServerFeature.SESSIONS
import com.hexagonkt.serialization.JacksonMapper
import com.hexagonkt.serialization.Json
import com.hexagonkt.serialization.SerializationManager
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import java.net.InetAddress.getByName as address

@TestInstance(PER_CLASS)
internal class ServerTest {

    @BeforeAll fun initialize() {
        SerializationManager.formats = linkedSetOf(Json)
        SerializationManager.mapper = JacksonMapper
    }

    @Test fun `Default banner includes documentation URL`() {
        assert(Server.banner.contains("https://hexagonkt.com/http_server"))
    }

    @Test fun `Default parameters`() {
        val serverSettings = ServerSettings(address("localhost"), 9999, banner = "name")
        val server = Server(VoidAdapter, Router(), serverSettings)

        assert(server.settings.banner == "name")
        assert(server.portName == VoidAdapter.javaClass.simpleName)
        assert(server.settings.bindAddress == address("localhost"))
        assert(server.settings.bindPort == 9999)
    }

    @Test fun `Runtime port`() {
        val serverSettings = ServerSettings(address("localhost"), 9999, banner = "name")
        val server = Server(VoidAdapter, Router(), serverSettings)

        assertFailsWith<IllegalStateException>("Server is not running") { server.runtimePort }
        assert(!server.started())

        server.start()

        assert(server.started())
        assert(server.runtimePort == 12345)
    }

    @Test fun `Banner creation`() {
        val bannerPrefix = "Test Banner"
        val serverSettings = ServerSettings(
            address("localhost"),
            12345,
            banner = bannerPrefix,
            features = setOf(SESSIONS)
        )

        val server = serve(serverSettings, VoidAdapter) {}
        val createdBanner = server.createBanner(System.currentTimeMillis())
        server.stop()

        assertEquals(bannerPrefix, createdBanner.lines()[0].trimIndent())
        assertContains(createdBanner, "✅HTTP" )
        assertFalse(createdBanner.contains("✅HTTPS"))
        assertContains(createdBanner, "✅SESSIONS" )
        assertFalse(createdBanner.contains("✅ZIP"))
    }
}
