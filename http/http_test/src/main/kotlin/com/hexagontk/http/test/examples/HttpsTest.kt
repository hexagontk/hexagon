package com.hexagontk.http.test.examples

import com.hexagontk.core.fail
import com.hexagontk.core.require
import com.hexagontk.core.security.getPrivateKey
import com.hexagontk.core.security.getPublicKey
import com.hexagontk.core.security.loadKeyStore
import com.hexagontk.core.urlOf
import com.hexagontk.http.SslSettings
import com.hexagontk.http.client.HttpClient
import com.hexagontk.http.client.HttpClientPort
import com.hexagontk.http.client.HttpClientSettings
import com.hexagontk.http.model.Header
import com.hexagontk.http.model.HttpProtocol.HTTP2
import com.hexagontk.http.model.HttpProtocol.HTTPS
import com.hexagontk.http.server.*
import com.hexagontk.http.handlers.HttpHandler
import com.hexagontk.http.handlers.path
import com.hexagontk.http.test.BaseTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIf
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

    private val identity = "hexagontk.p12"
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

    @Test
    @DisabledIf("nativeMac")
    fun `Serve HTTPS example`() {

        // https
        // Key store files
        val identity = "hexagontk.p12"
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
        val client = HttpClient(clientAdapter(), clientSettings.copy(baseUrl = serverBase(server)))
        client.start()
        client.get("/hello").apply {
            // Assure the certificate received (and returned) by the server is correct
            assert(headers.require("cert").string()?.startsWith("CN=hexagontk.com") ?: false)
            assertEquals("Hello World!", body)
        }
        // https

        client.stop()
        server.stop()
    }

    @Test
    @DisabledIf("nativeMac")
    fun `Serve HTTPS works properly`() {

        val server = serve(serverAdapter(), handler, http2ServerSettings.copy(protocol = HTTPS))

        val client = HttpClient(clientAdapter(), clientSettings.copy(baseUrl = serverBase(server)))
        client.start()
        client.get("/hello").apply {
            assert(headers.require("cert").string()?.startsWith("CN=hexagontk.com") ?: false)
            assertEquals("Hello World!", body)
        }

        client.stop()
        server.stop()
    }

    @Test
    @DisabledIf("nativeMac")
    fun `Serve HTTP2 works properly`() {

        val server = serve(serverAdapter(), handler, http2ServerSettings)

        val client = HttpClient(clientAdapter(), clientSettings.copy(baseUrl = serverBase(server)))
        client.start()
        client.get("/hello").apply {
            assert(headers.require("cert").string()?.startsWith("CN=hexagontk.com") ?: false)
            assertEquals("Hello World!", body)
        }

        client.stop()
        server.stop()
    }

    @Test
    @DisabledIf("nativeMac")
    fun `Serve insecure HTTPS example`() {

        val identity = "hexagontk.p12"
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
        val base = serverBase(server)
        val client = HttpClient(clientAdapter(), clientSettings.copy(baseUrl = base))
        client.start()
        client.get("/hello").apply {
            assertEquals("Hello World!", body)
        }

        assertFails {
            val adapter = clientAdapter()
            val noTrustStore = HttpClientSettings()
            HttpClient(adapter, noTrustStore.copy(baseUrl = base)).use {
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
            clientSettings.copy(baseUrl = base, insecure = true, sslSettings = SslSettings())
        )

        insecureClient.use {
            it.start()
            it.get("/hello").apply {
                assertEquals("Hello World!", body)
            }
        }

        val settings = clientSettings.copy(
            baseUrl = base,
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
            assertNotNull(getPrivateKey("hexagontk", keyStorePassword))
            assertNotNull(getPublicKey("hexagontk"))
        }

        loadKeyStore(trustStore, trustStorePassword).apply {
            assertNotNull(getPublicKey("ca"))
        }
    }

    private fun serverBase(server: HttpServer): URL =
        urlOf("${server.binding.protocol}://localhost:${server.runtimePort}")

    @Suppress("MemberVisibilityCanBePrivate") // Public access required by JUnit
    fun nativeMac(): Boolean =
        System.getProperty("os.name").lowercase().contains("mac")
            && System.getProperty("org.graalvm.nativeimage.imagecode") != null
}
