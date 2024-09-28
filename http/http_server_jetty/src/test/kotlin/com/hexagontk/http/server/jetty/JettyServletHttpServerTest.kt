package com.hexagontk.http.server.jetty

import com.hexagontk.core.media.TEXT_PLAIN
import com.hexagontk.core.urlOf
import com.hexagontk.http.client.HttpClient
import com.hexagontk.http.client.HttpClientSettings
import com.hexagontk.http.client.jetty.ws.JettyWsHttpClient
import com.hexagontk.http.model.ContentType
import com.hexagontk.http.model.OK_200
import com.hexagontk.http.handlers.path
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledForJreRange
import org.junit.jupiter.api.condition.JRE.JAVA_17
import org.junit.jupiter.api.condition.JRE.JAVA_19
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class JettyServletHttpServerTest {

    @Test
    @EnabledForJreRange(min = JAVA_19)
    fun `Virtual threads can be enabled`() {
        JettyServletHttpServer(useVirtualThreads = true)
    }

    @Test
    @EnabledForJreRange(max = JAVA_17)
    fun `Virtual threads are disabled`() {
        assertFailsWith<IllegalStateException> { JettyServletHttpServer(useVirtualThreads = true) }
    }

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
        val server = serve(handlers = path)

        server.use { s ->
            val settings = HttpClientSettings(urlOf("http://localhost:${s.runtimePort}"))
            HttpClient(JettyWsHttpClient(), settings).use {
                it.start()
                val result = it.get("/hello/Ada")
                assertEquals("Hello Ada!", result.body)
                assertEquals(OK_200, result.status)
                assertEquals(ContentType(TEXT_PLAIN), result.contentType)
            }
        }
    }
}
