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

    @Test fun `Examples are fetched from mediatype schema correctly`() {
        InjectionManager.bindObject<ServerPort>(JettyServletAdapter())

        val mockServer = MockServer("openapi_test.json")
        val server = mockServer.server
        server.start()

        val client = Client(AhcAdapter(), endpoint = "http://0.0.0.0:${server.runtimePort}")
        val response = client.get("/get-example-from-schema")
        assert(response.status == 200)
        assert(response.body == "response")
    }

    @Test fun `Examples are fetched from mediatype correctly`() {
        InjectionManager.bindObject<ServerPort>(JettyServletAdapter())

        val mockServer = MockServer("openapi_test.json")
        val server = mockServer.server
        server.start()

        val client = Client(AhcAdapter(), endpoint = "http://0.0.0.0:${server.runtimePort}")
        val response = client.get("/get-example-from-mediatype")
        assert(response.status == 200)
        assert(response.body == "response")
    }

    @Test fun `Paths not present in OpenAPI spec return 404`() {
        InjectionManager.bindObject<ServerPort>(JettyServletAdapter())

        val mockServer = MockServer("openapi_test.json")
        val server = mockServer.server
        server.start()

        val client = Client(AhcAdapter(), endpoint = "http://0.0.0.0:${server.runtimePort}")
        val response = client.get("/unknown-path")
        assert(response.status == 404)
    }

    @Test fun `Required query params are verified correctly`() {
        InjectionManager.bindObject<ServerPort>(JettyServletAdapter())

        val mockServer = MockServer("openapi_test.json")
        val server = mockServer.server
        server.start()

        val client = Client(AhcAdapter(), endpoint = "http://0.0.0.0:${server.runtimePort}")
        val response1 = client.get("/check-query-param")
        assert(response1.status == 400)
        assert(response1.body == "invalid or missing query param")

        val response2 = client.get("/check-query-param?queryParam=aValidValue")
        assert(response2.status == 200)
        assert(response2.body == "success")

        val response3 = client.get("/check-query-param?queryParam=anInvalidValue")
        assert(response3.status == 400)
        assert(response3.body == "invalid or missing query param")
    }

    @Test fun `Optional query params are ignored`() {
        InjectionManager.bindObject<ServerPort>(JettyServletAdapter())

        val mockServer = MockServer("openapi_test.json")
        val server = mockServer.server
        server.start()

        val client = Client(AhcAdapter(), endpoint = "http://0.0.0.0:${server.runtimePort}")
        val response1 = client.get("/check-optional-query-param")
        assert(response1.status == 200)
        assert(response1.body == "success")

        val response2 = client.get("/check-optional-query-param?queryParam=aValidValue")
        assert(response2.status == 200)
        assert(response2.body == "success")

        val response3 = client.get("/check-optional-query-param?queryParam=anInvalidValue")
        assert(response3.status == 200)
        assert(response3.body == "success")
    }
}
