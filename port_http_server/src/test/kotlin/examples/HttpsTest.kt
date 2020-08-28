package com.hexagonkt.http.server.examples

import com.hexagonkt.helpers.logger
import com.hexagonkt.helpers.require
import com.hexagonkt.http.Protocol.HTTP2
import com.hexagonkt.http.Protocol.HTTPS
import com.hexagonkt.http.SslSettings
import com.hexagonkt.http.client.ahc.AhcAdapter
import com.hexagonkt.http.client.Client
import com.hexagonkt.http.client.ClientSettings
import com.hexagonkt.http.server.*
import org.junit.jupiter.api.Test
import java.net.URI
import kotlin.test.assertFails

abstract class HttpsTest(adapter: ServerPort) {

    private val serverAdapter = adapter

    private val identity = "hexagonkt.p12"
    private val trust = "trust.p12"
    private val keyStore = URI("resource:///ssl/$identity")
    private val trustStore = URI("resource:///ssl/$trust")
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
        val keyStore = URI("resource:///ssl/$identity")
        val trustStore = URI("resource:///ssl/$trust")

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

        // Create a HTTP client and make a HTTPS request
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
            keyStore = URI("resource:///ssl/$identity"),
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
            trustStore = URI("resource:///ssl/$trust"),
            trustStorePassword = trust.reversed()
        )
        // trustStoreSettings

        val clientSettings = ClientSettings(
            sslSettings = trustStoreSettings
        )

        // Create a HTTP client and make a HTTPS request
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
            "https://127.0.0.1:${server.runtimePort}",
            clientSettings.copy(insecure = true)
        )
        insecureClient.get("/hello").apply { assert(body == "Hello World!") }

        server.stop()
    }
}
