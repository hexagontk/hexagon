package com.hexagontk.http.server.jetty

import com.hexagontk.core.media.TEXT_PLAIN
import com.hexagontk.http.client.HttpClient
import com.hexagontk.http.client.HttpClientSettings
import com.hexagontk.http.client.jetty.ws.JettyWsHttpClient
import com.hexagontk.http.model.ContentType
import com.hexagontk.http.model.OK_200
import com.hexagontk.http.handlers.path
import com.hexagontk.http.server.HttpServerSettings
import com.hexagontk.http.server.serve
import org.junit.jupiter.api.Test
import java.net.URI
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class JettyServletHttpServerTest {

    @Test fun `Stop method works if called before running`() {
        val adapter = JettyServletHttpServer()
        assert(!adapter.started())
        adapter.shutDown()
        assert(!adapter.started())
    }

    @Test fun `Getting the runtime port on stopped instance raises an exception`() {
        assertFailsWith<IllegalStateException> {
            JettyServletHttpServer().runtimePort()
        }
    }

    @Test fun `Serve helper works properly`() {

        val path = path {
            get("/hello/{name}") {
                val name = pathParameters["name"]
                ok("Hello $name!", contentType = ContentType(TEXT_PLAIN))
            }
        }
        val server = serve(JettyServletHttpServer(), path, HttpServerSettings(bindPort = 0))

        server.use { s ->
            val settings = HttpClientSettings(URI("http://localhost:${s.runtimePort}"))
            HttpClient(JettyWsHttpClient(), settings).use {
                it.start()
                val result = it.get("/hello/Ada")
                assertEquals("Hello Ada!", result.body)
                assertEquals(OK_200, result.status)
                assertEquals(ContentType(TEXT_PLAIN).text, result.contentType?.text)
            }
        }
    }
}
