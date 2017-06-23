package co.there4.hexagon.server.integration

import co.there4.hexagon.server.Router
import org.asynchttpclient.Response
import java.net.InetAddress.getByName as address

internal abstract class ItModule {
    internal abstract fun initialize (router: Router)
    internal abstract fun validate()

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
