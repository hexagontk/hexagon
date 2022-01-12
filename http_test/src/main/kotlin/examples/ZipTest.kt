package com.hexagonkt.http.test.examples

import com.hexagonkt.core.multiMapOf
import com.hexagonkt.http.client.HttpClient
import com.hexagonkt.http.client.HttpClientPort
import com.hexagonkt.http.server.*
import com.hexagonkt.http.server.HttpServerFeature.ZIP
import com.hexagonkt.http.server.handlers.ServerHandler
import com.hexagonkt.http.server.handlers.path
import com.hexagonkt.http.test.BaseTest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.net.URL
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Suppress("FunctionName") // This class's functions are intended to be used only in tests
abstract class ZipTest(
    override val clientAdapter: () -> HttpClientPort,
    override val serverAdapter: () -> HttpServerPort
) : BaseTest() {

    override val handler: ServerHandler = path {}

    @Test fun `Use ZIP encoding example`() = runBlocking {

        // zip
        val serverSettings = HttpServerSettings(
            bindPort = 0,
            features = setOf(ZIP)
        )

        val server = HttpServer(serverAdapter(), serverSettings) {
            get("/hello") {
                ok("Hello World!")
            }
        }
        server.start()

        val client = HttpClient(clientAdapter(), URL("http://localhost:${server.runtimePort}"))
        client.start()

        client.get("/hello", multiMapOf("accept-encoding" to "gzip")).apply {
            assertEquals(body, "Hello World!")
            assert(headers["content-encoding"]?.contains("gzip") ?: false)
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

    @Test fun `Use ZIP encoding without enabling the feature example`() = runBlocking {

        val server = HttpServer(serverAdapter(), HttpServerSettings(bindPort = 0)) {
            get("/hello") {
                ok("Hello World!")
            }
        }
        server.start()

        val client = HttpClient(clientAdapter(), URL("http://localhost:${server.runtimePort}"))
        client.start()

        client.get("/hello", multiMapOf("accept-encoding" to "gzip")).apply {
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
