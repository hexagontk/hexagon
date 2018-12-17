package com.hexagonkt.server

import com.hexagonkt.http.client.Client
import org.asynchttpclient.Response
import java.net.InetAddress.getByName as address

internal abstract class TestModule {
    abstract fun initialize(): Router
    abstract fun validate(client: Client)

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
