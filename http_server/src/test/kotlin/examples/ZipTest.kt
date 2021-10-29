package com.hexagonkt.http.server.examples

import com.hexagonkt.http.client.ahc.AhcAdapter
import com.hexagonkt.http.client.Client
import com.hexagonkt.http.server.*
import com.hexagonkt.http.server.ServerFeature.ZIP
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

abstract class ZipTest(adapter: ServerPort) {

    private val serverAdapter = adapter

    @Test fun `Use ZIP encoding example`() {

        // zip
        val serverSettings = ServerSettings(
            bindPort = 0,
            features = setOf(ZIP)
        )

        val server = serve(serverSettings, serverAdapter) {
            get("/hello") {
                ok("Hello World!")
            }
        }

        val client = Client(AhcAdapter(), "http://localhost:${server.runtimePort}")

        client.get("/hello", mapOf("Accept-Encoding" to listOf("gzip"))).apply {
            assertEquals(body, "Hello World!")
            assert(headers["content-encoding"]?.contains("gzip") ?: false)
        }

        client.get("/hello").apply {
            assertEquals(body, "Hello World!")
            assertEquals(null, headers["content-encoding"])
            assertEquals(null, headers["Content-Encoding"])
        }
        // zip

        server.stop()
    }

    @Test fun `Use ZIP encoding without enabling the feature example`() {

        val server = serve(ServerSettings(bindPort = 0), serverAdapter) {
            get("/hello") {
                ok("Hello World!")
            }
        }

        val client = Client(AhcAdapter(), "http://localhost:${server.runtimePort}")

        client.get("/hello", mapOf("Accept-Encoding" to listOf("gzip"))).apply {
            assertEquals(body, "Hello World!")
            assertEquals(null, headers["content-encoding"])
            assertEquals(null, headers["Content-Encoding"])
        }

        client.get("/hello").apply {
            assertEquals(body, "Hello World!")
            assertEquals(null, headers["content-encoding"])
            assertEquals(null, headers["Content-Encoding"])
        }

        server.stop()
    }
}
