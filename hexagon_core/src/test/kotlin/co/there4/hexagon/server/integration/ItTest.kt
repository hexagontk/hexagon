package co.there4.hexagon.server.integration

import co.there4.hexagon.client.Client
import co.there4.hexagon.server.Router
import org.testng.annotations.Test

import co.there4.hexagon.server.Server
import co.there4.hexagon.server.jetty.JettyServletEngine
import co.there4.hexagon.settings.SettingsManager
import org.asynchttpclient.Response
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import java.net.InetAddress.getByName as address

internal const val THREADS = 1
internal const val TIMES = 1

/*
 * TODO Fix errors with several threads
 */
@Test (threadPoolSize = THREADS, invocationCount = TIMES)
abstract class ItTest {
    open val servers: List<Server> = listOf(
        Server(JettyServletEngine(), SettingsManager.settings)
    )

    protected abstract fun Router.initialize ()

    @BeforeClass fun startServers () {
        servers.forEach {
            it.stop()
            it.router.initialize ()
            it.run ()
        }
    }

    @AfterClass fun stopServers () {
        servers.forEach(Server::stop)
    }

    protected fun withClients(lambda: Client.() -> Unit) {
        servers.forEach {
            val client = Client ("http://localhost:${it.runtimePort}")
            client.cookies.clear()
            client.(lambda) ()
        }
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
