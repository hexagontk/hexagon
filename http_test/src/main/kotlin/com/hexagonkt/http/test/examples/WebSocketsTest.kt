package com.hexagonkt.http.test.examples

import com.hexagonkt.core.logging.logger
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
import kotlin.IllegalStateException
import java.net.URL
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

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

    // TODO Add WebSockets samples: auth, request access, store sessions, close sessions...
    private val clientSettings = HttpClientSettings(sslSettings = sslSettings)

    // ws_server
    private var sessions = emptyMap<Int, List<WsSession>>()

    override val handler: HttpHandler = path {
        ws("/ws/{id}") {
            // Path parameters can also be accessed on WS handlers like `onText`
            val idParameter = pathParameters.require("id")

            // Request is handled like other HTTP methods, if errors, no WS connection is made
            val id = idParameter.toIntOrNull()
                ?: return@ws badRequest("ID must be a number: $idParameter")

            // Accepted returns the callbacks to handle WS requests
            accepted(
                // All callbacks have their session as the receiver
                onConnect = {
                    val se = sessions[id] ?: emptyList()
                    sessions = sessions + (id to se + this)
                },

                onBinary = { bytes ->
                    if (bytes.isEmpty()) {
                        // The HTTP request data can be queried from the WS session
                        val certificateSubject = request.certificate()?.subjectX500Principal?.name
                        send(certificateSubject?.toByteArray() ?: byteArrayOf())
                    }
                },

                onText = { text ->
                    val se = sessions[id] ?: emptyList()
                    for (s in se)
                        // It is allowed to send data on previously stored sessions
                        s.send(text)
                },

                // Ping requests helps to maintain WS sessions opened
                onPing = { bytes -> pong(bytes) },

                // Pong handlers should be used to check sent pings
                onPong= { bytes -> send(bytes) },

                // Callback executed when WS sessions are closed (on the server or client side)
                onClose = { status, reason ->
                    logger.info { "$status: $reason" }
                    val se = sessions[id] ?: emptyList()
                    sessions = sessions + (id to se - this)
                }
            )
        }
    }
    // ws_server

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

    @Test fun `WebSockets connections can be checked before session is created`() {
        assertFails {
            client.ws(
                path = "/ws/a",
                onText = {
                    if (it.lowercase().contains("bye"))
                        close(NORMAL, "Thanks")
                }
            )
        }
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

        // ws_client
        val results = mutableMapOf<Int, Set<String>>()

        val ws = client.ws(
            path = "/ws/1",
            onText = { text ->
                synchronized(results) {
                    results[1] = (results[1] ?: emptySet()) + text
                }
            },
            onClose = { status, reason ->
                logger.info { "Closed with: $status - $reason" }
            }
        )
        // ws_client

        val ws1 = client.ws(
            path = "/ws/1",
            onText = {
                synchronized(results) {
                    results[1] = (results[1] ?: emptySet()) + "$it#"
                }
                if (it.lowercase().contains("bye"))
                    close(NORMAL, "Thanks")
            }
        )

        val ws2 = client.ws(
            path = "/ws/2",
            onText = {
                results[2] = (results[2] ?: emptySet()) + "$it@"
                if (it.lowercase().contains("bye"))
                    close(NORMAL, "Thanks")
            }
        )

        ws1.send("Hello")
        Thread.sleep(300)
        assertEquals(setOf("Hello", "Hello#"), results[1])
        assertNull(results[2])
        ws1.send("Goodbye")
        Thread.sleep(300)
        ws1.close()
        ws.close()
        assertEquals(setOf("Hello", "Hello#", "Goodbye", "Goodbye#"), results[1])
        assertNull(results[2])

        ws2.send("Hello")
        Thread.sleep(300)
        assertEquals(setOf("Hello@"), results[2])
        ws2.send("Goodbye")
        Thread.sleep(300)
        ws2.close()
        assertEquals(setOf("Hello@", "Goodbye@"), results[2])

        client.stop()
        server.stop()
    }
}
