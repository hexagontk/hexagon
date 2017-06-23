package co.there4.hexagon.server.integration

import co.there4.hexagon.client.Client
import co.there4.hexagon.server.Server
import co.there4.hexagon.server.ServerEngine
import co.there4.hexagon.settings.SettingsManager
import java.net.InetAddress.getByName as address

/*
 * TODO Fix errors with several threads
 */
abstract class ItTest(serverEngine: ServerEngine) {
    protected val server: Server = Server(serverEngine, SettingsManager.settings)
    protected val client by lazy { Client ("http://localhost:${server.runtimePort}") }

    private val modules: List<ItModule> = listOf(
//        BooksIT(server.router, client),
//        CookiesIT(server.router, client),
//        GenericIT(server.router, client),
//        HexagonIT(server.router, client),
//        SessionIT(server.router, client)
    )

    fun startServers () {
        modules.forEach {
            it.initialize(server.router)
        }
        server.run ()
    }

    fun stopServers () {
        server.stop()
    }
}
