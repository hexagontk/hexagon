package com.hexagonkt.http.server

import com.hexagonkt.injection.forceBindObject
import com.hexagonkt.injection.InjectionManager.bindObject
import com.hexagonkt.serialization.convertToObject
import com.hexagonkt.settings.SettingsManager
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith
import java.net.InetAddress.getByName as address

class ServerTest {

    @Test fun `Injected parameters`() {
        forceBindObject<ServerPort>(VoidAdapter)
        bindObject<ServerSettings>(SettingsManager.settings.convertToObject())

        val server = Server {}

        assert(server.settings.serverName == "Hexagon Tests")
        assert(server.portName == VoidAdapter.javaClass.simpleName)
        assert(server.settings.bindAddress.hostAddress == "0.0.0.0")
        assert(server.settings.bindPort == 0)
    }

    @Test fun `Default parameters`() {
        val serverSettings = ServerSettings("name", address("localhost"), 9999)
        val server = Server(VoidAdapter, Router(), serverSettings)

        assert(server.settings.serverName == "name")
        assert(server.portName == VoidAdapter.javaClass.simpleName)
        assert(server.settings.bindAddress == address("localhost"))
        assert(server.settings.bindPort == 9999)
    }

    @Test fun `Runtime port`() {
        val serverSettings = ServerSettings("name", address("localhost"), 9999)
        val server = Server(VoidAdapter, Router(), serverSettings)

        assertFailsWith<IllegalStateException>("Server is not running") { server.runtimePort }
        assert(!server.started())

        server.start()

        assert(server.started())
        assert(server.runtimePort == 12345)
    }
}
