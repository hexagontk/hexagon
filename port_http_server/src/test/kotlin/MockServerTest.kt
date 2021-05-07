package com.hexagonkt.http.server

import com.hexagonkt.injection.InjectionManager
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MockServerTest {

    @BeforeAll fun setUp() {
        InjectionManager.bind<ServerPort>(VoidAdapter)
    }

    @Test fun `Incorrect OpenAPI spec causes error to be thrown`() {
        assertThrows<IllegalArgumentException> {
            MockServer("some illegal path")
        }
    }

    @ExperimentalStdlibApi // TODO Remove when using Kotlin 1.5
    @Test fun `Basic server is created correctly`() {
        val mockServer = MockServer("https://petstore3.swagger.io/api/v3/openapi.json")
        val server = mockServer.server

        assert(server.settings.bindAddress.hostAddress == "127.0.0.1")
    }

    @ExperimentalStdlibApi // TODO Remove when using Kotlin 1.5
    @Test fun `Server at specific port is created correctly`() {
        val mockServer = MockServer("https://petstore3.swagger.io/api/v3/openapi.json", port = 9090)
        val server = mockServer.server

        assert(server.settings.bindAddress.hostAddress == "127.0.0.1")
        assert(server.settings.bindPort == 9090)
    }
}
