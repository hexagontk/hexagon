package com.hexagonkt.http.server.jetty

import com.hexagonkt.http.client.Client
import com.hexagonkt.http.client.ahc.AhcAdapter
import com.hexagonkt.http.server.Router
import com.hexagonkt.http.server.ServerSettings
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS

@TestInstance(PER_CLASS)
internal class HelloWorldTest {

    private val client: Client by lazy {
        Client(AhcAdapter(), "http://localhost:${server.runtimePort}")
    }

    @BeforeAll fun initialize() {
        main()
    }

    @AfterAll fun shutdown() {
        server.stop()
    }

    @Test fun `A request returns 200 and the greeting test`() {
        val result = client.get("/hello")
        assert(result.body == "Hello World!")
        assert(200 == result.status)
    }

    @Test fun `A request returns 200 and the greeting test (using a router)`() {

        val router = Router {
            get("/hello") {
                ok("Hello World!")
            }
        }

        val routerServer = serve(ServerSettings(bindPort = 0), router)
        val runtimePort = routerServer.runtimePort
        val routerServerClient = Client(AhcAdapter(), "http://localhost:$runtimePort")

        val result = routerServerClient.get("/hello")
        assert(result.body == "Hello World!")
        assert(200 == result.status)

        routerServer.stop()
    }
}
