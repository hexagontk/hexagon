package com.hexagonkt.http.test.examples

import com.hexagonkt.core.fail
import com.hexagonkt.core.logging.logger
import com.hexagonkt.core.require
import com.hexagonkt.core.security.getPrivateKey
import com.hexagonkt.core.security.getPublicKey
import com.hexagonkt.core.security.loadKeyStore
import com.hexagonkt.http.SslSettings
import com.hexagonkt.http.client.HttpClient
import com.hexagonkt.http.client.HttpClientPort
import com.hexagonkt.http.client.HttpClientSettings
import com.hexagonkt.http.model.HttpProtocol.HTTP2
import com.hexagonkt.http.model.HttpProtocol.HTTPS
import com.hexagonkt.http.server.*
import com.hexagonkt.http.server.handlers.ServerHandler
import com.hexagonkt.http.server.handlers.path
import com.hexagonkt.http.test.BaseTest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.net.URL
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNotNull

@Suppress("FunctionName") // This class's functions are intended to be used only in tests
abstract class HttpsTest(
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

    private val router = path {
        get("/hello") {
            val certificateSubject = request.certificate()?.subjectX500Principal?.name ?: fail
            val headers = response.headers + ("cert" to certificateSubject)
            ok("Hello World!", headers = headers)
        }
    }

    override val handler: ServerHandler = router

    @Test fun `Serve HTTPS example`() = runBlocking {

        // https
        // Key store files
        val identity = "hexagonkt.p12"
        val trust = "trust.p12"

        // Default passwords are file name reversed
        val keyStorePassword = identity.reversed()
        val trustStorePassword = trust.reversed()

        // Key stores can be set as URIs to classpath resources (the triple slash is needed)
        val keyStore = URL("classpath:ssl/$identity")
        val trustStore = URL("classpath:ssl/$trust")

        val sslSettings = SslSettings(
            keyStore = keyStore,
            keyStorePassword = keyStorePassword,
            trustStore = trustStore,
            trustStorePassword = trustStorePassword,
            clientAuth = true // Requires a valid certificate from the client (mutual TLS)
        )

        val serverSettings = HttpServerSettings(
            bindPort = 0,
            protocol = HTTPS, // You can also use HTTP2
            sslSettings = sslSettings
        )

        val server = HttpServer(serverAdapter(), serverSettings) {
            get("/hello") {
                // We can access the certificate used by the client from the request
                val subjectDn = request.certificate()?.subjectX500Principal?.name ?: ""
                val h = response.headers + ("cert" to subjectDn)
                ok("Hello World!", headers = h)
            }
        }
        server.start()

        // We'll use the same certificate for the client (in a real scenario it would be different)
        val clientSettings = HttpClientSettings(sslSettings = sslSettings)

        // Create an HTTP client and make an HTTPS request
        val contextPath = URL("https://localhost:${server.runtimePort}")
        val client = HttpClient(clientAdapter(), contextPath, clientSettings)
        client.start()
        client.get("/hello").apply {
            logger.debug { body }
            // Assure the certificate received (and returned) by the server is correct
            assert(headers.require("cert").startsWith("CN=hexagonkt.com"))
            assertEquals("Hello World!", body)
        }
        // https

        client.stop()
        server.stop()
    }

    @Test fun `Serve HTTPS works properly`() = runBlocking {

        val server = serve(serverAdapter(), handler, http2ServerSettings.copy(protocol = HTTPS))

        val contextPath = URL("https://localhost:${server.runtimePort}")
        val client = HttpClient(clientAdapter(), contextPath, clientSettings)
        client.start()
        client.get("/hello").apply {
            logger.debug { body }
            assert(headers.require("cert").startsWith("CN=hexagonkt.com"))
            assertEquals("Hello World!", body)
        }

        client.stop()
        server.stop()
    }

    @Test fun `Serve HTTP2 works properly`() = runBlocking {

        val server = serve(serverAdapter(), handler, http2ServerSettings)

        val contextPath = URL("https://localhost:${server.runtimePort}")
        val client = HttpClient(clientAdapter(), contextPath, clientSettings)
        client.start()
        client.get("/hello").apply {
            logger.debug { body }
            assert(headers.require("cert").startsWith("CN=hexagonkt.com"))
            assertEquals("Hello World!", body)
        }

        client.stop()
        server.stop()
    }

    @Test fun `Serve insecure HTTPS example`() = runBlocking {

        val identity = "hexagonkt.p12"
        val trust = "trust.p12"

        // keyStoreSettings
        val keyStoreSettings = SslSettings(
            keyStore = URL("classpath:ssl/$identity"),
            keyStorePassword = identity.reversed()
        )
        // keyStoreSettings

        val serverSettings = serverSettings.copy(
            bindPort = 0,
            protocol = HTTPS,
            sslSettings = keyStoreSettings
        )

        val server = HttpServer(serverAdapter(), serverSettings) {
            get("/hello") {
                ok("Hello World!")
            }
        }
        server.start()

        // trustStoreSettings
        val trustStoreSettings = SslSettings(
            trustStore = URL("classpath:ssl/$trust"),
            trustStorePassword = trust.reversed()
        )
        // trustStoreSettings

        val clientSettings = HttpClientSettings(
            sslSettings = trustStoreSettings
        )

        // Create an HTTP client and make an HTTPS request
        val contextPath = URL("https://localhost:${server.runtimePort}")
        val client = HttpClient(clientAdapter(), contextPath, clientSettings)
        client.start()
        client.get("/hello").apply {
            assertEquals("Hello World!", body)
        }

        assertFails {
            val adapter = clientAdapter()
            val noTrustStore = HttpClientSettings()
            HttpClient(adapter, contextPath, noTrustStore).use {
                it.start()
                it.get("/hello")
            }
        }

        assertFails {
            val adapter = clientAdapter()
            val contextPath1 = URL("https://127.0.0.1:${server.runtimePort}")
            HttpClient(adapter, contextPath1, clientSettings).use {
                it.start()
                it.get("/hello")
            }
        }

        val insecureClient = HttpClient(
            clientAdapter(),
            contextPath,
            clientSettings.copy(insecure = true, sslSettings = SslSettings())
        )

        insecureClient.use {
            it.start()
            it.get("/hello").apply {
                assertEquals("Hello World!", body)
            }
        }

        HttpClient(
            clientAdapter(),
            contextPath,
            clientSettings.copy(insecure = false, sslSettings = SslSettings())
        ).use {
            it.start()
            val throwable = assertFails { it.get("/hello") }
            throwable.printStackTrace()
        }

        client.stop()
        server.stop()
    }

    @Test fun `Key stores contains the proper aliases`() {

        loadKeyStore(keyStore, keyStorePassword).apply {
            assertNotNull(getPrivateKey("hexagonkt", keyStorePassword))
            assertNotNull(getPublicKey("hexagonkt"))
        }

        loadKeyStore(trustStore, trustStorePassword).apply {
            assertNotNull(getPublicKey("ca"))
        }
    }
}
