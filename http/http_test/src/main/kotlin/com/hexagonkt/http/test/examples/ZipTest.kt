package com.hexagonkt.http.test.examples

import com.hexagonkt.core.urlOf
import com.hexagonkt.http.client.HttpClient
import com.hexagonkt.http.client.HttpClientPort
import com.hexagonkt.http.client.HttpClientSettings
import com.hexagonkt.http.model.Header
import com.hexagonkt.http.model.Headers
import com.hexagonkt.http.server.*
import com.hexagonkt.http.handlers.HttpHandler
import com.hexagonkt.http.handlers.path
import com.hexagonkt.http.test.BaseTest
import org.junit.jupiter.api.Test
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

        val settings = HttpClientSettings(urlOf("http://localhost:${server.runtimePort}"))
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

        val server = HttpServer(serverAdapter(), serverSettings.copy(bindPort = 0)) {
            get("/hello") {
                ok("Hello World!")
            }
        }
        server.start()

        val settings = HttpClientSettings(urlOf("http://localhost:${server.runtimePort}"))
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
