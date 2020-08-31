package com.hexagonkt.http.server

import com.hexagonkt.injection.InjectionManager.forceBindObject
import org.junit.jupiter.api.Test
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import java.net.InetAddress.getByName as address

class ServerTest {

    @Test fun `Injected parameters`() {
        forceBindObject<ServerPort>(VoidAdapter)

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

    @Test fun `Parameters map`() {
        val router = Router {}
        val server = Server(VoidAdapter, router = router)
        assert(equal (server, Server(VoidAdapter, router, mapOf<String, Any>())))
        val invalidSettings = mapOf("serverName" to 0, "bindAddress" to 1, "bindPort" to true)
        assertFails { Server(VoidAdapter, Router(), invalidSettings) }

        val settings = mapOf(
            "serverName" to "name",
            "bindAddress" to "localhost",
            "bindPort" to 12345
        )
        val serverSettings = ServerSettings("name", address("localhost"), 12345)
        val server1 = Server(VoidAdapter, router, serverSettings)
        assert(equal(server1, Server(VoidAdapter, Router(), settings)))
    }

    private fun equal(server1: Server, server2: Server) =
        server1.settings.serverName == server2.settings.serverName &&
        server1.settings.bindAddress == server2.settings.bindAddress &&
        server1.settings.bindPort == server2.settings.bindPort
}
