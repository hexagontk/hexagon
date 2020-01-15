package com.hexagonkt.http.server.examples

import com.hexagonkt.helpers.logger
import com.hexagonkt.http.Protocol.HTTP2
import com.hexagonkt.http.Protocol.HTTPS
import com.hexagonkt.http.SslSettings
import com.hexagonkt.http.client.Client
import com.hexagonkt.http.client.ClientSettings
import com.hexagonkt.http.server.*
import org.testng.annotations.Test
import java.net.URI

@Test abstract class HttpsTest(adapter: ServerPort) {

    private val serverAdapter = adapter

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
            response.setHeader("cert", request.certificateChain.first().subjectDN.name)
            ok("Hello World!")
        }
    }

    @Test fun `Serve HTTPS example`() {

        // https
        // Key store files
        val identity = "hexagonkt.p12"
        val trust = "trust.p12"

        // Key stores can be set as URIs to classpath resources (password is file name reversed)
        val keyStore = URI("resource://${identity.reversed()}/ssl/$identity")
        val trustStore = URI("resource://${trust.reversed()}/ssl/$trust")

        val sslSettings = SslSettings(
            keyStore = keyStore,
            trustStore = trustStore,
            clientAuth = true
        )

        val serverSettings = ServerSettings(
            bindPort = 0,
            protocol = HTTPS, // You can also use HTTP2
            sslSettings = sslSettings
        )

        val server = serve(serverSettings, serverAdapter) {
            get("/hello") {
                // We can access the certificate used by the client from the request
                val subjectDn = request.certificateChain.first().subjectDN.name
                response.setHeader("cert", subjectDn)
                ok("Hello World!")
            }
        }

        // We'll use the same certificate for the client (in a real scenario it would be different)
        val clientSettings = ClientSettings(sslSettings = sslSettings)

        // Create a HTTP client and make a HTTPS request
        Client("https://localhost:${server.runtimePort}", clientSettings).get("/hello") {
            logger.debug { responseBody }
            // Assure the certificate received (and returned) by the server is correct
            assert(headers["cert"].startsWith("CN=hexagonkt.com"))
            assert(responseBody == "Hello World!")
        }
        // https

        server.stop()
    }

    @Test fun `Serve HTTPS works properly`() {

        val server = Server(serverAdapter, router, serverSettings.copy(protocol = HTTPS))
        server.start()

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
