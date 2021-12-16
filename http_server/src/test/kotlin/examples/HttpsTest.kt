package com.hexagonkt.http.server.examples

import com.hexagonkt.core.logging.logger
import com.hexagonkt.core.helpers.require
import com.hexagonkt.core.security.getPrivateKey
import com.hexagonkt.core.security.getPublicKey
import com.hexagonkt.core.security.loadKeyStore
import com.hexagonkt.http.Protocol.HTTP2
import com.hexagonkt.http.Protocol.HTTPS
import com.hexagonkt.http.SslSettings
import com.hexagonkt.http.client.ahc.AhcAdapter
import com.hexagonkt.http.client.Client
import com.hexagonkt.http.client.ClientSettings
import com.hexagonkt.http.server.*
import org.junit.jupiter.api.Test
import java.net.URL
import kotlin.test.assertFails
import kotlin.test.assertNotNull

abstract class HttpsTest(adapter: ServerPort) {

    private val serverAdapter = adapter

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

    private val serverSettings = ServerSettings(
        bindPort = 0,
        protocol = HTTP2,
        sslSettings = sslSettings
    )

    private val clientSettings = ClientSettings(sslSettings = sslSettings)

    private val router = Router {
        get("/hello") {
            response.headers["cert"] = request.certificateChain.firstOrNull()?.subjectDN?.name
            ok("Hello World!")
        }
    }

    @Test fun `Serve HTTPS example`() {

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

        val serverSettings = ServerSettings(
            bindPort = 0,
            protocol = HTTPS, // You can also use HTTP2
            sslSettings = sslSettings
        )

        val server = serve(serverSettings, serverAdapter) {
            get("/hello") {
                // We can access the certificate used by the client from the request
                val subjectDn = request.certificate?.subjectDN?.name
                response.headers["cert"] = subjectDn
                ok("Hello World!")
            }
        }

        // We'll use the same certificate for the client (in a real scenario it would be different)
        val clientSettings = ClientSettings(sslSettings = sslSettings)

        // Create an HTTP client and make an HTTPS request
        val client = Client(AhcAdapter(), "https://localhost:${server.runtimePort}", clientSettings)
        client.get("/hello").apply {
            logger.debug { body }
            // Assure the certificate received (and returned) by the server is correct
            assert(headers.require("cert").first().startsWith("CN=hexagonkt.com"))
            assert(body == "Hello World!")
        }
        // https

        server.stop()
    }

    @Test fun `Serve HTTPS works properly`() {

        val server = Server(serverAdapter, router, serverSettings.copy(protocol = HTTPS))
        server.start()

        val client = Client(AhcAdapter(), "https://localhost:${server.runtimePort}", clientSettings)
        client.get("/hello").apply {
            logger.debug { body }
            assert(headers.require("cert").first().startsWith("CN=hexagonkt.com"))
            assert(body == "Hello World!")
        }

        server.stop()
    }

    @Test fun `Serve HTTP2 works properly`() {

        val server = serve(serverSettings, router, serverAdapter)

        val client = Client(AhcAdapter(), "https://localhost:${server.runtimePort}", clientSettings)
        client.get("/hello").apply {
            logger.debug { body }
            assert(headers.require("cert").first().startsWith("CN=hexagonkt.com"))
            assert(body == "Hello World!")
        }

        server.stop()
    }

    @Test fun `Serve insecure HTTPS example`() {

        val identity = "hexagonkt.p12"
        val trust = "trust.p12"

        // keyStoreSettings
        val keyStoreSettings = SslSettings(
            keyStore = URL("classpath:ssl/$identity"),
            keyStorePassword = identity.reversed()
        )
        // keyStoreSettings

        val serverSettings = ServerSettings(
            bindPort = 0,
            protocol = HTTPS,
            sslSettings = keyStoreSettings
        )

        val server = serve(serverSettings, serverAdapter) {
            get("/hello") {
                ok("Hello World!")
            }
        }

        // trustStoreSettings
        val trustStoreSettings = SslSettings(
            trustStore = URL("classpath:ssl/$trust"),
            trustStorePassword = trust.reversed()
        )
        // trustStoreSettings

        val clientSettings = ClientSettings(
            sslSettings = trustStoreSettings
        )

        // Create an HTTP client and make an HTTPS request
        val client = Client(AhcAdapter(), "https://localhost:${server.runtimePort}", clientSettings)
        client.get("/hello").apply {
            assert(body == "Hello World!")
        }

        assertFails {
            val adapter = AhcAdapter()
            val noTrustStore = ClientSettings()
            Client(adapter, "https://localhost:${server.runtimePort}", noTrustStore).get("/hello")
        }

        assertFails {
            val adapter = AhcAdapter()
            Client(adapter, "https://127.0.0.1:${server.runtimePort}", clientSettings).get("/hello")
        }

        val insecureClient = Client(
            AhcAdapter(),
            "https://localhost:${server.runtimePort}",
            clientSettings.copy(insecure = true, sslSettings = SslSettings())
        )
        insecureClient.get("/hello").apply {
            assert(body == "Hello World!")
        }

        Client(
            AhcAdapter(),
            "https://localhost:${server.runtimePort}",
            clientSettings.copy(insecure = false, sslSettings = SslSettings())
        ).apply {
            val throwable = assertFails { get("/hello") }
            assert(throwable.message?.contains("java.net.ConnectException") ?: false)
        }

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
