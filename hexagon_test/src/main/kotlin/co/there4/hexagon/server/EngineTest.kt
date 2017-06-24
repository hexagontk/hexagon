package co.there4.hexagon.server

import co.there4.hexagon.client.Client
import co.there4.hexagon.settings.SettingsManager
import java.net.InetAddress.getByName as address

abstract class EngineTest(serverEngine: ServerEngine) {
    protected val server: Server = Server(serverEngine, SettingsManager.settings)
    protected val client by lazy { Client ("http://localhost:${server.runtimePort}") }

    internal val modules: List<TestModule> by lazy {
        listOf(
            BooksModule(),
            CookiesModule(),
            GenericModule(),
            HexagonModule(),
            SessionModule()
        )
    }

    fun startServers () {
        modules.forEach { it.initialize(server.router) }
        server.run ()
    }

    fun stopServers () {
        server.stop()
    }

    fun validate() {
        modules.forEach { it.validate(client) }
    }
}
