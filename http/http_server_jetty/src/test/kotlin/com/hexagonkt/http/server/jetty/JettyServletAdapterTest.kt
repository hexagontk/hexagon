package com.hexagonkt.http.server.jetty

import com.hexagonkt.core.media.TEXT_PLAIN
import com.hexagonkt.core.urlOf
import com.hexagonkt.http.client.HttpClient
import com.hexagonkt.http.client.HttpClientSettings
import com.hexagonkt.http.client.jetty.ws.JettyWsClientAdapter
import com.hexagonkt.http.model.ContentType
import com.hexagonkt.http.model.OK_200
import com.hexagonkt.http.handlers.path
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledForJreRange
import org.junit.jupiter.api.condition.JRE.JAVA_17
import org.junit.jupiter.api.condition.JRE.JAVA_19
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class JettyServletAdapterTest {

    @Test
    @EnabledForJreRange(min = JAVA_19)
    fun `Virtual threads can be enabled`() {
        JettyServletAdapter(useVirtualThreads = true)
    }

    @Test
    @EnabledForJreRange(max = JAVA_17)
    fun `Virtual threads are disabled`() {
        assertFailsWith<IllegalStateException> { JettyServletAdapter(useVirtualThreads = true) }
    }

    @Test fun `Stop method works if called before running`() {
        val adapter = JettyServletAdapter()
        assert(!adapter.started())
        adapter.shutDown()
        assert(!adapter.started())
    }

    @Test fun `Getting the runtime port on stopped instance raises an exception`() {
        assertFailsWith<IllegalStateException> {
            JettyServletAdapter().runtimePort()
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
            HttpClient(JettyWsClientAdapter(), settings).use {
                it.start()
                val result = it.get("/hello/Ada")
                assertEquals("Hello Ada!", result.body)
                assertEquals(OK_200, result.status)
                assertEquals(ContentType(TEXT_PLAIN), result.contentType)
            }
        }
    }
}
