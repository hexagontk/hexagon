package com.hexagonkt.http.test.examples

import com.hexagonkt.core.fail
import com.hexagonkt.core.require
import com.hexagonkt.http.SslSettings
import com.hexagonkt.http.client.HttpClient
import com.hexagonkt.http.client.HttpClientPort
import com.hexagonkt.http.client.HttpClientSettings
import com.hexagonkt.http.model.HttpProtocol.*
import com.hexagonkt.http.model.ws.CloseStatus.NORMAL
import com.hexagonkt.http.model.ws.WsSession
import com.hexagonkt.http.server.*
import com.hexagonkt.http.server.handlers.HttpHandler
import com.hexagonkt.http.server.handlers.path
import com.hexagonkt.http.test.BaseTest
import org.junit.jupiter.api.Test
import java.lang.IllegalStateException
import java.net.URL
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Suppress("FunctionName") // This class's functions are intended to be used only in tests
abstract class WebSocketsTest(
    final override val clientAdapter: () -> HttpClientPort,
    final override val serverAdapter: () -> HttpServerPort,
    final override val serverSettings: HttpServerSettings = HttpServerSettings(),
) : BaseTest() {

    private val identity = "hexagonkt.p12"
    private val trust = "trust.p12"
    private val keyStore = URL("classpath:ssl/$identity")
    private val trustStore = URL("classpath:ssl/$trust")
    private val keyStorePassword = identity.reversed()
    private val trustStorePassword = trust.reversed()

    private val sslSettings = SslSettings(
        keyStore = keyStore,
        keyStorePassword = keyStorePassword,
        trustStore = trustStore,
        trustStorePassword = trustStorePassword,
        clientAuth = true
    )

    private val http2ServerSettings = serverSettings.copy(
        bindPort = 0,
        protocol = HTTP2,
        sslSettings = sslSettings
    )

    private val clientSettings = HttpClientSettings(sslSettings = sslSettings)

    private var sessions = emptyMap<String, List<WsSession>>()

    // TODO Add WebSockets samples: auth, request access, store sessions, close sessions...
    override val handler: HttpHandler = path {
        ws("/ws/{id}") {
            val id = pathParameters.require("id")

            accepted(
                onConnect = {
                    val se = sessions[id] ?: emptyList()
                    sessions = sessions + (id to se + this)
                },

                onBinary = {
                    val certificateSubject = request.certificate()?.subjectX500Principal?.name ?: fail
                    val text = String(it)
                },

                onText = {
                    val se = sessions[id] ?: emptyList()
                    for (s in se)
                        s.send(it)
                },

                onPing = {
                    val text = String(it)
                },

                onPong= {
                    val text = String(it)
                },

                onClose = { status, reason ->
                    assertEquals(NORMAL, status)
                    assertEquals("test", reason)
                    val se = sessions[id] ?: emptyList()
                    sessions = sessions + (id to se - this)
                }
            )
        }
    }

    @Test fun `WebSockets client check start and stop states`() {
        val client = HttpClient(clientAdapter(), "https://localhost:9999", clientSettings)

        assertEquals(
            "HTTP client *MUST BE STARTED* before connecting to WS",
            assertFailsWith<IllegalStateException> { client.ws("/ws/1") }.message
        )
        assertEquals(
            "HTTP client *MUST BE STARTED* before shut-down",
            assertFailsWith<IllegalStateException> { client.stop() }.message
        )
    }

    @Test fun `Serve WS works properly`() {
        wsTest("http", serverSettings.copy(bindPort = 0), clientSettings)
    }

    @Test fun `Serve WSS works properly`() {
        wsTest("https", http2ServerSettings.copy(protocol = HTTPS), clientSettings)
    }

    @Test fun `Serve WSS over HTTP2 works properly`() {
        wsTest("https", http2ServerSettings, clientSettings)
    }

    private fun wsTest(
        protocol: String,
        serverSettings: HttpServerSettings,
        clientSettings: HttpClientSettings,
    ) {
        val server = serve(serverAdapter(), handler, serverSettings)

        val contextPath = URL("$protocol://localhost:${server.runtimePort}")
        val client = HttpClient(clientAdapter(), contextPath, clientSettings)
        client.start()

        var result = ""

        val ws = client.ws(
            path = "/ws/1",
            onText = {
                result = "$it#"
                if (it.lowercase().contains("bye")) {
                    close(NORMAL, "Thanks")
                }
            }
        )

        ws.send("Hello")
        Thread.sleep(300)
        assertEquals("Hello#", result)
        ws.send("Goodbye")
        Thread.sleep(300)
        ws.close()
        assertEquals("Goodbye#", result)

        client.stop()
        server.stop()
    }
}
