package com.hexagontk.http.test.examples

import com.hexagontk.core.fail
import com.hexagontk.core.urlOf
import com.hexagontk.http.SslSettings
import com.hexagontk.http.client.HttpClient
import com.hexagontk.http.client.HttpClientPort
import com.hexagontk.http.client.HttpClientSettings
import com.hexagontk.http.handlers.HttpHandler
import com.hexagontk.http.handlers.path
import com.hexagontk.http.model.Field
import com.hexagontk.http.model.HttpProtocol
import com.hexagontk.http.server.HttpServer
import com.hexagontk.http.server.HttpServerPort
import com.hexagontk.http.server.HttpServerSettings
import com.hexagontk.http.server.serve
import com.hexagontk.http.test.BaseTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIf
import java.net.URI
import kotlin.test.assertEquals

@Suppress("FunctionName") // This class's functions are intended to be used only in tests
abstract class Http2Test(
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

    private val http2ServerSettings = serverSettings.with(
        bindPort = 0,
        protocol = HttpProtocol.HTTP2,
        sslSettings = sslSettings
    )

    private val clientSettings = HttpClientSettings(sslSettings = sslSettings)

    private val router = path {
        get("/hello") {
            val certificateSubject = request.certificate()?.subjectX500Principal?.name ?: fail
            val headers = response.headers + Field("cert", certificateSubject)
            ok("Hello World!", headers = headers)
        }
    }

    override val handler: HttpHandler = router

    @Test
    @DisabledIf("nativeMac")
    fun `Serve HTTP2 works properly`() {

        val server = serve(serverAdapter(), handler, http2ServerSettings)

        val client = HttpClient(clientAdapter(), clientSettings.with(baseUri = serverBase(server)))
        client.start()
        client.get("/hello").apply {
            assert(headers.require("cert").text.startsWith("CN=hexagontk.com"))
            assertEquals("Hello World!", body)
        }

        client.stop()
        server.stop()
    }

    private fun serverBase(server: HttpServer): URI =
        URI("${server.binding.scheme}://localhost:${server.runtimePort}")

    @Suppress("MemberVisibilityCanBePrivate") // Public access required by JUnit
    fun nativeMac(): Boolean =
        System.getProperty("os.name").lowercase().contains("mac")
            && System.getProperty("org.graalvm.nativeimage.imagecode") != null
}
