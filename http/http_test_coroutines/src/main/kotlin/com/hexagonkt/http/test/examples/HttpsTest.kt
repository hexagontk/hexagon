package com.hexagonkt.http.test.examples

import com.hexagonkt.core.fail
import com.hexagonkt.core.require
import com.hexagonkt.core.security.getPrivateKey
import com.hexagonkt.core.security.getPublicKey
import com.hexagonkt.core.security.loadKeyStore
import com.hexagonkt.core.urlOf
import com.hexagonkt.http.SslSettings
import com.hexagonkt.http.client.HttpClient
import com.hexagonkt.http.client.HttpClientPort
import com.hexagonkt.http.client.HttpClientSettings
import com.hexagonkt.http.model.Header
import com.hexagonkt.http.model.HttpProtocol.HTTP2
import com.hexagonkt.http.model.HttpProtocol.HTTPS
import com.hexagonkt.http.handlers.coroutines.HttpHandler
import com.hexagonkt.http.handlers.coroutines.path
import com.hexagonkt.http.server.coroutines.HttpServer
import com.hexagonkt.http.server.coroutines.HttpServerPort
import com.hexagonkt.http.server.coroutines.HttpServerSettings
import com.hexagonkt.http.server.coroutines.serve
import com.hexagonkt.http.test.BaseTest
import org.junit.jupiter.api.Test
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
    private val keyStore = urlOf("classpath:ssl/$identity")
    private val trustStore = urlOf("classpath:ssl/$trust")
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
            val headers = response.headers + Header("cert", certificateSubject)
            ok("Hello World!", headers = headers)
        }
    }

    override val handler: HttpHandler = router

    @Test fun `Serve HTTPS example`() {

        // https
        // Key store files
        val identity = "hexagonkt.p12"
        val trust = "trust.p12"

        // Default passwords are file name reversed
        val keyStorePassword = identity.reversed()
        val trustStorePassword = trust.reversed()

        // Key stores can be set as URIs to classpath resources (the triple slash is needed)
        val keyStore = urlOf("classpath:ssl/$identity")
        val trustStore = urlOf("classpath:ssl/$trust")

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
                val h = response.headers + Header("cert", subjectDn)
                ok("Hello World!", headers = h)
            }
        }
        server.start()

        // We'll use the same certificate for the client (in a real scenario it would be different)
        val clientSettings = HttpClientSettings(sslSettings = sslSettings)

        // Create an HTTP client and make an HTTPS request
        val client = HttpClient(clientAdapter(), clientSettings.copy(baseUrl = server.binding))
        client.start()
        client.get("/hello").apply {
            // Assure the certificate received (and returned) by the server is correct
            assert(headers.require("cert").string()?.startsWith("CN=hexagonkt.com") ?: false)
            assertEquals("Hello World!", body)
        }
        // https

        client.stop()
        server.stop()
    }

    @Test fun `Serve HTTPS works properly`() {

        val server = serve(serverAdapter(), handler, http2ServerSettings.copy(protocol = HTTPS))

        val client = HttpClient(clientAdapter(), clientSettings.copy(baseUrl = server.binding))
        client.start()
        client.get("/hello").apply {
            assert(headers.require("cert").string()?.startsWith("CN=hexagonkt.com") ?: false)
            assertEquals("Hello World!", body)
        }

        client.stop()
        server.stop()
    }

    @Test fun `Serve HTTP2 works properly`() {

        val server = serve(serverAdapter(), handler, http2ServerSettings)

        val client = HttpClient(clientAdapter(), clientSettings.copy(baseUrl = server.binding))
        client.start()
        client.get("/hello").apply {
            assert(headers.require("cert").string()?.startsWith("CN=hexagonkt.com") ?: false)
            assertEquals("Hello World!", body)
        }

        client.stop()
        server.stop()
    }

    @Test fun `Serve insecure HTTPS example`() {

        val identity = "hexagonkt.p12"
        val trust = "trust.p12"

        // keyStoreSettings
        val keyStoreSettings = SslSettings(
            keyStore = urlOf("classpath:ssl/$identity"),
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
            trustStore = urlOf("classpath:ssl/$trust"),
            trustStorePassword = trust.reversed()
        )
        // trustStoreSettings

        val clientSettings = HttpClientSettings(
            sslSettings = trustStoreSettings
        )

        // Create an HTTP client and make an HTTPS request
        val contextPath = server.binding
        val client = HttpClient(clientAdapter(), clientSettings.copy(baseUrl = contextPath))
        client.start()
        client.get("/hello").apply {
            assertEquals("Hello World!", body)
        }

        assertFails {
            val adapter = clientAdapter()
            val noTrustStore = HttpClientSettings()
            HttpClient(adapter, noTrustStore.copy(baseUrl = contextPath)).use {
                it.start()
                it.get("/hello")
            }
        }

        assertFails {
            val adapter = clientAdapter()
            val contextPath1 = urlOf("https://127.0.0.1:${server.runtimePort}")
            HttpClient(adapter, clientSettings.copy(baseUrl = contextPath1)).use {
                it.start()
                it.get("/hello")
            }
        }

        val insecureClient = HttpClient(
            clientAdapter(),
            clientSettings.copy(baseUrl = contextPath, insecure = true, sslSettings = SslSettings())
        )

        insecureClient.use {
            it.start()
            it.get("/hello").apply {
                assertEquals("Hello World!", body)
            }
        }

        val settings = clientSettings.copy(
            baseUrl = contextPath,
            insecure = false,
            sslSettings = SslSettings()
        )
        HttpClient(clientAdapter(), settings).use {
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
