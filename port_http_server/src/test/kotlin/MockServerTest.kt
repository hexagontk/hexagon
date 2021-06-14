package com.hexagonkt.http.server

import com.hexagonkt.injection.InjectionManager.module
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class MockServerTest {

    @BeforeAll fun setUp() {
        module.bind<ServerPort>(VoidAdapter)
    }

    @Test fun `Incorrect OpenAPI spec causes error to be thrown`() {
        assertThrows<IllegalArgumentException> {
            MockServer("some illegal path")
        }
    }

    @Test fun `Basic server is created correctly`() {
        val mockServer = MockServer("petstore_openapi.json")
        val server = mockServer.server

        assert(server.settings.bindAddress.hostAddress == "127.0.0.1")
    }

    @Test fun `Server at specific port is created correctly`() {
        val mockServer = MockServer("petstore_openapi.json", port = 9090)
        val server = mockServer.server

        assert(server.settings.bindAddress.hostAddress == "127.0.0.1")
        assert(server.settings.bindPort == 9090)
    }
}
