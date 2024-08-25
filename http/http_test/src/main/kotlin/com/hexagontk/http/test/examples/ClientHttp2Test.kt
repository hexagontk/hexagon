package com.hexagontk.http.test.examples

import com.hexagontk.core.require
import com.hexagontk.core.media.APPLICATION_JSON
import com.hexagontk.core.urlOf
import com.hexagontk.http.SslSettings
import com.hexagontk.http.client.HttpClient
import com.hexagontk.http.client.HttpClientPort
import com.hexagontk.http.client.HttpClientSettings
import com.hexagontk.http.formatQueryString
import com.hexagontk.http.model.*
import com.hexagontk.http.server.*
import com.hexagontk.http.handlers.HttpCallbackType
import com.hexagontk.http.handlers.HttpHandler
import com.hexagontk.http.handlers.path
import com.hexagontk.http.model.HttpProtocol.HTTP2
import com.hexagontk.http.test.BaseTest
import com.hexagontk.serialization.SerializationFormat
import com.hexagontk.serialization.SerializationManager
import org.junit.jupiter.api.*
import org.junit.jupiter.api.condition.DisabledIf
import java.net.URL

import kotlin.test.assertEquals

@Suppress("FunctionName") // This class's functions are intended to be used only in tests
abstract class ClientHttp2Test(
    final override val clientAdapter: () -> HttpClientPort,
    final override val serverAdapter: () -> HttpServerPort,
    private val serializationFormats: List<SerializationFormat>,
    final override val serverSettings: HttpServerSettings = HttpServerSettings(),
) : BaseTest() {

    private var callback: HttpCallbackType = { this }

    override val handler: HttpHandler = path {
        post("*") { callback() }
        get("*") { callback() }
        head("*") { callback() }
        put("*") { callback() }
        delete("*") { callback() }
        trace("*") { callback() }
        options("*") { callback() }
        patch("*") { callback() }
    }

    @BeforeAll fun setUpSerializationFormats() {
        SerializationManager.formats = serializationFormats.toSet()
    }

    @BeforeEach fun resetHandler() {
        callback = {
            val contentType = ContentType(APPLICATION_JSON, charset = Charsets.UTF_8)
            val bodyString = request.bodyString()
            val bodyHeader =
                if (bodyString.endsWith("\n") || bodyString.contains("{")) "json"
                else bodyString

            ok(
                body = bodyString,
                headers = response.headers
                    + Header("body", bodyHeader)
                    + Header("ct", request.contentType?.text ?: "")
                    + Header("query-parameters", formatQueryString(queryParameters)),
                contentType = contentType,
            )
        }
    }

    @Test
    @DisabledIf("nativeMac")
    fun `Request HTTPS example`() {

        val serverAdapter = serverAdapter()

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

        val serverSettings = serverSettings.copy(
            bindPort = 0,
            protocol = HTTP2,
            sslSettings = sslSettings
        )

        val server = serve(serverAdapter, serverSettings) {
            get("/hello") {
                // We can access the certificate used by the client from the request
                val subjectDn = request.certificate()?.subjectX500Principal?.name ?: ""
                ok("Hello World!", headers = response.headers + Header("cert", subjectDn) )
            }
        }

        // We'll use the same certificate for the client (in a real scenario it would be different)
        val baseUrl = serverBase(server)
        val clientSettings = HttpClientSettings(baseUrl = baseUrl, sslSettings = sslSettings)

        // Create an HTTP client and make an HTTPS request
        val client = HttpClient(clientAdapter(), clientSettings)
        client.start()
        client.get("/hello").apply {
            // Assure the certificate received (and returned) by the server is correct
            assert(headers.require("cert").string()?.startsWith("CN=hexagontk.com") ?: false)
            assertEquals(body, "Hello World!")
        }

        client.stop()
        server.stop()
    }

    private fun serverBase(server: HttpServer): URL =
        urlOf("${server.binding.protocol}://localhost:${server.runtimePort}")

    @Suppress("MemberVisibilityCanBePrivate") // Public access required by JUnit
    fun nativeMac(): Boolean =
        System.getProperty("os.name").lowercase().contains("mac")
            && System.getProperty("org.graalvm.nativeimage.imagecode") != null
}
