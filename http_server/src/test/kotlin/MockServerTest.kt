package com.hexagonkt.http.server

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class MockServerTest {

    @Test fun `Incorrect OpenAPI spec causes error to be thrown`() {
        assertThrows<IllegalArgumentException> {
            MockServer(VoidAdapter, "some illegal path")
        }
    }

    @Test fun `Basic server is created correctly`() {
        val mockServer = MockServer(VoidAdapter, "petstore_openapi.json")
        val server = mockServer.server

        assert(server.settings.bindAddress.hostAddress == "127.0.0.1")
    }

    @Test fun `Server at specific port is created correctly`() {
        val mockServer = MockServer(VoidAdapter, "petstore_openapi.json", port = 9090)
        val server = mockServer.server

        assert(server.settings.bindAddress.hostAddress == "127.0.0.1")
        assert(server.settings.bindPort == 9090)
    }
}
