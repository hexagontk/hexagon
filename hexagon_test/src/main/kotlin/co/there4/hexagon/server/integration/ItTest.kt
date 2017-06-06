package co.there4.hexagon.server.integration

import co.there4.hexagon.client.Client
import co.there4.hexagon.server.Router

import co.there4.hexagon.server.Server
import co.there4.hexagon.server.ServerEngine
import co.there4.hexagon.settings.SettingsManager
import org.asynchttpclient.Response
import java.net.InetAddress.getByName as address

/*
 * TODO Fix errors with several threads
 */
//abstract class ItTest(serverEngine: ServerEngine) {
abstract class ItTest {
//    protected val server: Server = Server(serverEngine, SettingsManager.settings)

    protected abstract fun Router.initialize ()

    fun startServers () {
//        server.stop()
//        server.router.initialize ()
//        server.run ()
    }

    fun stopServers () {
//        server.stop()
    }

    protected fun withClients(lambda: Client.() -> Unit) {
//        val client = Client ("http://localhost:${server.runtimePort}")
//        client.cookies.clear()
//        client.(lambda) ()
    }

    protected fun assertResponseEquals(response: Response?, status: Int, content: String) {
        assert (response?.statusCode == status)
        assert (response?.responseBody == content)
    }

    protected fun assertResponseContains(response: Response?, status: Int, vararg content: String) {
        assert (response?.statusCode == status)
        content.forEach {
            assert (response?.responseBody?.contains (it) ?: false)
        }
    }
}
