package com.hexagonkt.http.server

import com.hexagonkt.injection.InjectionManager.bindObject
import org.testng.annotations.Test
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import java.net.InetAddress.getByName as address

@Test class ServerTest {

    @Test fun `Injected parameters`() {
        bindObject<ServerPort>(VoidAdapter)

        val server = Server {}

        assert(server.serverSettings.serverName == "Hexagon Tests")
        assert(server.portName == VoidAdapter.javaClass.simpleName)
        assert(server.serverSettings.bindAddress.hostAddress == "0.0.0.0")
        assert(server.serverSettings.bindPort == 0)
    }

    @Test fun `Default parameters`() {
        val serverSettings = ServerSettings("name", address("localhost"), 9999)
        val server = Server(VoidAdapter, Router(), serverSettings)

        assert(server.serverSettings.serverName == "name")
        assert(server.portName == VoidAdapter.javaClass.simpleName)
        assert(server.serverSettings.bindAddress == address("localhost"))
        assert(server.serverSettings.bindPort == 9999)
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
        server1.serverSettings.serverName == server2.serverSettings.serverName &&
        server1.serverSettings.bindAddress == server2.serverSettings.bindAddress &&
        server1.serverSettings.bindPort == server2.serverSettings.bindPort
}
