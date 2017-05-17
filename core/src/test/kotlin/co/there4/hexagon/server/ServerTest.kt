package co.there4.hexagon.server

import co.there4.hexagon.server.engine.ServerEngine
import co.there4.hexagon.server.engine.servlet.JettyServletEngine
import org.testng.annotations.Test
import kotlin.test.assertFailsWith
import java.net.InetAddress.getByName as address

@Test class ServerTest {
    object FakeEngine : ServerEngine {
        var started = false

        override fun runtimePort() = 12345
        override fun started() = started
        override fun startup(server: Server, settings: Map<String, *>) { started = true }
        override fun shutdown() { started = false }
    }

    fun default_parameters() {
        val server = Server(JettyServletEngine(), "name", address("localhost"), 9999, router {})

        assert(server.serverName == "name")
        assert(server.bindAddress == address("localhost"))
        assert(server.bindPort == 9999)
    }

    fun runtime_port() {
        val server = Server(FakeEngine, "name", address("localhost"), 9999, router {})

        assertFailsWith<IllegalStateException>("Server is not running") { server.runtimePort }
        assert(!server.started())

        server.run()

        assert(server.started())
        assert(server.runtimePort == 12345)
    }

    fun parameters_map() {
        val router = router {}
        val server = Server(FakeEngine, router = router)
        assert(equal (server, Server(FakeEngine, mapOf<String, Any>(), router)))
        val invalidSettings = mapOf("serviceName" to 0, "bindAddress" to 1, "bindPort" to true)
        assert(equal(server, Server(FakeEngine, invalidSettings)))

        val settings = mapOf(
            "serviceName" to "name",
            "bindAddress" to "localhost",
            "bindPort" to 12345
        )
        val server1 = Server(FakeEngine, "name", address("localhost"), 12345, router)
        assert(equal(server1, Server(FakeEngine, settings)))
    }

    private fun equal(server1: Server, server2: Server) =
        server1.serverName == server2.serverName &&
        server1.bindAddress == server2.bindAddress &&
        server1.bindPort == server2.bindPort
}
