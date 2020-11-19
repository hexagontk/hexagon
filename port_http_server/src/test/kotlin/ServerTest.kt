package com.hexagonkt.http.server

import com.hexagonkt.injection.forceBind
import com.hexagonkt.injection.InjectionManager.bind
import com.hexagonkt.settings.SettingsManager
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith
import com.hexagonkt.serialization.convertToObject
import java.net.InetAddress.getByName as address

class ServerTest {

    @Test fun `Injected parameters`() {
        forceBind(ServerPort::class, VoidAdapter)
        bind(SettingsManager.instance<Map<*, *>>().convertToObject<ServerSettings>())

        val server = Server {}

        assert(server.settings.banner == "Hexagon Tests")
        assert(server.portName == VoidAdapter.javaClass.simpleName)
        assert(server.settings.bindAddress.hostAddress == "0.0.0.0")
        assert(server.settings.bindPort == 0)
    }

    @Test fun `Default banner includes documentation URL`() {
        assert(Server.banner.contains("https://hexagonkt.com/port_http_server"))
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
}
