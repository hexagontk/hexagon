package com.hexagonkt.http.server.jetty

import com.hexagonkt.helpers.logger
import com.hexagonkt.http.Protocol.HTTP2
import com.hexagonkt.http.Protocol.HTTPS
import com.hexagonkt.http.client.Client
import com.hexagonkt.http.server.ServerPort
import com.hexagonkt.http.server.ServerSettings
import com.hexagonkt.http.SslSettings
import com.hexagonkt.http.client.ClientSettings
import com.hexagonkt.http.server.Router
import com.hexagonkt.http.server.serve
import com.hexagonkt.injection.InjectionManager.bindObject
import org.testng.annotations.Test
import java.net.URI

@Test class HttpsTest {

    private val identity = "hexagonkt.p12"
    private val trust = "trust.p12"
    private val keyStore = URI("resource://${identity.reversed()}/ssl/$identity")
    private val trustStore = URI("resource://${trust.reversed()}/ssl/$trust")

    private val sslSettings = SslSettings(
        keyStore = keyStore,
        trustStore = trustStore,
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
            val certChain = request.certificateChain
            val cert = certChain.first()
            response.setHeader("cert", request.certificateChain.first().subjectDN.name)
            ok("Hello World!")
        }
    }

    init {
        bindObject<ServerPort>(JettyServletAdapter())
    }

    @Test fun `Serve HTTPS works properly`() {

        val server = serve(serverSettings.copy(protocol = HTTPS), router)

        Client("https://localhost:${server.runtimePort}", clientSettings).get("/hello") {
            logger.debug { responseBody }
            assert(headers["cert"].startsWith("CN=hexagonkt.com"))
            assert(responseBody == "Hello World!")
        }

        server.stop()
    }

    @Test fun `Serve HTTP2 works properly`() {

        val server = serve(serverSettings, router)

        Client("https://localhost:${server.runtimePort}", clientSettings).get("/hello") {
            logger.debug { responseBody }
            assert(headers["cert"].startsWith("CN=hexagonkt.com"))
            assert(responseBody == "Hello World!")
        }

        server.stop()
    }
}
