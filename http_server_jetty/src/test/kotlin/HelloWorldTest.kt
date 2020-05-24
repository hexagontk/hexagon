package com.hexagonkt.http.server.jetty

import com.hexagonkt.http.client.Client
import com.hexagonkt.http.client.ahc.AhcAdapter
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS

@TestInstance(PER_CLASS)
class HelloWorldTest {

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
}
