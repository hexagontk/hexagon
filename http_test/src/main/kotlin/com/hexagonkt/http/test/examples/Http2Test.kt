package com.hexagonkt.http.test.examples

import com.hexagonkt.http.client.HttpClient
import com.hexagonkt.http.client.HttpClientPort
import com.hexagonkt.http.client.HttpClientSettings
import com.hexagonkt.http.model.Header
import com.hexagonkt.http.model.Headers
import com.hexagonkt.http.model.HttpProtocol.*
import com.hexagonkt.http.server.*
import com.hexagonkt.http.server.handlers.HttpHandler
import com.hexagonkt.http.server.handlers.path
import com.hexagonkt.http.test.BaseTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.URL

@Suppress("FunctionName") // This class's functions are intended to be used only in tests
abstract class Http2Test(
    final override val clientAdapter: () -> HttpClientPort,
    final override val serverAdapter: () -> HttpServerPort,
    final override val serverSettings: HttpServerSettings = HttpServerSettings(),
) : BaseTest() {
    private val h2cServerSettings = serverSettings.copy(
        bindPort = 0,
        protocol = H2C,
    )

    private val clientSettings = HttpClientSettings()

    private val h2cHeaders: Headers = Headers(
        Header("Upgrade", "h2c"),
        Header("Connection", "Upgrade, HTTP2-Settings"),
        Header("HTTP2-Settings", "")
    )

    private val router = path {
        get("/hello") { ok("Hello World!") }
    }

    override val handler: HttpHandler = router

    @Test fun `Serve H2C example`() {
        val server = serve(serverAdapter(), handler, h2cServerSettings)

        val contextPath = URL("http://localhost:${server.runtimePort}")
        val client = HttpClient(clientAdapter(), contextPath, clientSettings)

        client.start()
        client.get("/hello", h2cHeaders).apply {
            assertEquals("Hello World!", body)
        }

        client.stop()
        server.stop()
    }
}

