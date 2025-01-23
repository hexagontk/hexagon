package com.hexagontk.http.test.examples

import com.hexagontk.http.client.HttpClient
import com.hexagontk.http.client.HttpClientPort
import com.hexagontk.http.client.HttpClientSettings
import com.hexagontk.http.server.*
import com.hexagontk.http.handlers.HttpHandler
import com.hexagontk.http.handlers.path
import com.hexagontk.http.model.Header
import com.hexagontk.http.model.Headers
import com.hexagontk.http.test.BaseTest
import org.junit.jupiter.api.Test
import java.net.URI
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Suppress("FunctionName") // This class's functions are intended to be used only in tests
abstract class ZipTest(
    final override val clientAdapter: () -> HttpClientPort,
    final override val serverAdapter: () -> HttpServerPort,
    final override val serverSettings: HttpServerSettings = HttpServerSettings(),
) : BaseTest() {

    override val handler: HttpHandler = path {}

    @Test fun `Use ZIP encoding example`() {

        // zip
        val serverSettings = HttpServerSettings(
            bindPort = 0,
            zip = true,
        )

        val server = HttpServer(serverAdapter(), serverSettings) {
            get("/hello") {
                ok("Hello World!")
            }
        }
        server.start()

        val settings = HttpClientSettings(URI("http://localhost:${server.runtimePort}"))
        val client = HttpClient(clientAdapter(), settings)
        client.start()

        client.get("/hello", Headers(Header("accept-encoding", "gzip"))).apply {
            assertEquals(body, "Hello World!")
            // 'content-encoding' cannot be checked, the header is removed when response is decoded
        }

        client.get("/hello").apply {
            assertEquals(body, "Hello World!")
            assertNull(headers["content-encoding"])
            assertNull(headers["Content-Encoding"])
        }
        // zip

        client.stop()
        server.stop()
    }

    @Test open fun `Use ZIP encoding without enabling the feature example`() {

        val server = HttpServer(serverAdapter(), serverSettings.with(bindPort = 0)) {
            get("/hello") {
                ok("Hello World!")
            }
        }
        server.start()

        val settings = HttpClientSettings(URI("http://localhost:${server.runtimePort}"))
        val client = HttpClient(clientAdapter(), settings)
        client.start()

        client.get("/hello", Headers(Header("accept-encoding", "gzip"))).apply {
            assertEquals(body, "Hello World!")
            assertNull(headers["content-encoding"])
            assertNull(headers["Content-Encoding"])
        }

        client.get("/hello").apply {
            assertEquals(body, "Hello World!")
            assertNull(headers["content-encoding"])
            assertNull(headers["Content-Encoding"])
        }

        client.stop()
        server.stop()
    }
}
