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
abstract class ItTest(serverEngine: ServerEngine) {
    protected val server: Server = Server(serverEngine, SettingsManager.settings)
    protected val client by lazy { Client ("http://localhost:${server.runtimePort}") }

    protected abstract fun Router.initialize ()
    protected abstract fun validate()

    fun startServers () {
        server.router.initialize ()
        server.run ()
    }

    fun stopServers () {
        server.stop()
    }

    protected fun assertResponseEquals(response: Response?, content: String, status: Int = 200) {
        assert (response?.statusCode == status)
        assert (response?.responseBody == content)
    }

    protected fun assertResponseContains(response: Response?, status: Int, vararg content: String) {
        assert (response?.statusCode == status)
        content.forEach {
            assert (response?.responseBody?.contains (it) ?: false)
        }
    }

    protected fun assertResponseContains(response: Response?, vararg content: String) {
        assertResponseContains(response, 200, *content)
    }
}
