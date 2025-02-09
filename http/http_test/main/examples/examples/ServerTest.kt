package com.hexagontk.http.test.examples.examples

import com.hexagontk.http.HttpFeature
import com.hexagontk.http.client.HttpClient
import com.hexagontk.http.client.HttpClientPort
import com.hexagontk.http.client.HttpClientSettings
import com.hexagontk.http.server.HttpServerPort
import com.hexagontk.http.server.HttpServerSettings
import com.hexagontk.http.handlers.*
import com.hexagontk.http.server.serve
import com.hexagontk.http.test.BaseTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

abstract class ServerTest(
    override val clientAdapter: () -> HttpClientPort,
    override val serverAdapter: () -> HttpServerPort,
    override val serverSettings: HttpServerSettings = HttpServerSettings(),
    override val handler: HttpHandler = Get { ok() },
    private val options: Collection<String>,
    private val features: Set<HttpFeature>,
) : BaseTest() {

    @Test fun serverNotImplemented() {
        val server = serve(serverAdapter(), HttpServerSettings(bindPort = 0)) {
            get {
                assertNull(request.pathPattern)
                assertFailsWith<UnsupportedOperationException> { request.pathParameters }
                assertFailsWith<UnsupportedOperationException> {
                    request.with(certificateChain = emptyList())
                }
                ok("Get greeting")
            }
        }

        server.use { s ->
            HttpClient(clientAdapter(), HttpClientSettings(s.binding)).use {
                it.start()
                assertEquals("Get greeting", it.get().body)
            }
        }
    }

    @Test fun serverOptions() {
        val server = serverAdapter()
        assertEquals(options, server.options().keys)
        assertEquals(features, server.supportedFeatures())
    }
}
