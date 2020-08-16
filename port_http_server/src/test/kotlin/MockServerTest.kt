package com.hexagonkt.http.server

import com.hexagonkt.http.client.Client
import com.hexagonkt.http.client.ahc.AhcAdapter
import com.hexagonkt.http.server.jetty.JettyServletAdapter
import com.hexagonkt.injection.InjectionManager
import org.junit.jupiter.api.Test

class MockServerTest {

    @Test fun `Injected parameters`() {
        InjectionManager.bindObject<ServerPort>(VoidAdapter)

        val mockServer = MockServer("https://petstore3.swagger.io/api/v3/openapi.json")
        val server = mockServer.server

        assert(server.settings.bindAddress.hostAddress == "0.0.0.0")
        assert(server.settings.bindPort == 0)
    }

    @Test fun `Basic routes are created correctly`() {
        InjectionManager.bindObject<ServerPort>(JettyServletAdapter())

        val mockServer = MockServer("openapi_test.json")
        val server = mockServer.server
        server.start()

        val client = Client(AhcAdapter(), endpoint = "http://0.0.0.0:${server.runtimePort}")
        val response = client.get("/ping")
        assert(response.status == 200)
        assert(response.body == "pong")
    }
}
