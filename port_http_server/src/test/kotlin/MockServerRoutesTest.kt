package com.hexagonkt.http.server

import com.hexagonkt.http.Cookie
import com.hexagonkt.http.client.Client
import com.hexagonkt.http.client.ahc.AhcAdapter
import com.hexagonkt.http.server.jetty.JettyServletAdapter
import com.hexagonkt.injection.forceBind
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@ExperimentalStdlibApi // TODO Remove when using Kotlin 1.5
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MockServerRoutesTest {

    val server by lazy {
        MockServer("openapi_test.json").server
    }

    val client by lazy {
        Client(AhcAdapter(), endpoint = "http://localhost:${server.runtimePort}")
    }

    @BeforeAll fun setUp() {
        forceBind(ServerPort::class, JettyServletAdapter())
        server.start()
        assert(server.started())
    }

    @Test fun `Basic routes are created correctly`() {
        val response = client.get("/ping")
        assert(response.status == 200)
        assert(response.body == "pong")
    }

    @Test fun `Examples are fetched from media-type schema correctly`() {
        val response = client.get("/get-example-from-schema")
        assert(response.status == 200)
        assert(response.body == "response")
    }

    @Test fun `Examples are fetched from mediatype correctly`() {
        val response = client.get("/get-example-from-mediatype")
        assert(response.status == 200)
        assert(response.body == "response")
    }

    @Test fun `Examples are fetched from multiple examples correctly`() {
        val response = client.get("/get-from-multiple-examples")
        assert(response.status == 200)
        assert(response.body in listOf("foo", "bar"))
    }

    @Test fun `X-Mock-Response-Example is fetched from multiple examples correctly`() {
        val headers = mapOf("X-Mock-Response-Example" to listOf("example2"))
        val response = client.get("/get-from-multiple-examples", headers = headers)
        assert(response.status == 200)
        assert(response.body == "bar")
    }

    @Test fun `Empty string is returned if no examples specified`() {
        val response = client.get("/get-from-no-examples")
        assert(response.status == 500)
    }

    @Test fun `Paths not present in OpenAPI spec return 404`() {
        val response = client.get("/unknown-path")
        assert(response.status == 404)
    }

    @Test fun `Required query params are verified correctly`() {
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

    @Test fun `Optional query params are verified correctly`() {
        val response1 = client.get("/check-optional-query-param")
        assert(response1.status == 200)
        assert(response1.body == "success")

        val response2 = client.get("/check-optional-query-param?queryParam=aValidValue")
        assert(response2.status == 200)
        assert(response2.body == "success")

        val response3 = client.get("/check-optional-query-param?queryParam=anInvalidValue")
        assert(response3.status == 400)
        assert(response3.body == "invalid or missing query param")
    }

    @Test fun `Path params are verified correctly`() {
        val response1 = client.get("/check-path-param/aValidValue")
        assert(response1.status == 200)
        assert(response1.body == "success")

        val response2 = client.get("/check-path-param/anInvalidValue")
        assert(response2.status == 400)
        assert(response2.body == "invalid or missing path param")
    }

    @Test fun `Required header params are verified correctly`() {
        val response1 = client.get("/check-header-param")
        assert(response1.status == 400)
        assert(response1.body == "invalid or missing header param")

        val validHeaders = mapOf("headerParam" to listOf("aValidValue"))
        val response2 = client.get("/check-header-param", headers = validHeaders)
        assert(response2.status == 200)
        assert(response2.body == "success")

        val invalidHeaders = mapOf("headerParam" to listOf("anInvalidValue"))
        val response3 = client.get("/check-header-param", headers = invalidHeaders)
        assert(response3.status == 400)
        assert(response3.body == "invalid or missing header param")
    }

    @Test fun `Optional header params are verified correctly`() {
        val response1 = client.get("/check-optional-header-param")
        assert(response1.status == 200)
        assert(response1.body == "success")

        val validHeaders = mapOf("headerParam" to listOf("aValidValue"))
        val response2 = client.get("/check-optional-header-param", headers = validHeaders)
        assert(response2.status == 200)
        assert(response2.body == "success")

        val invalidHeaders = mapOf("headerParam" to listOf("anInvalidValue"))
        val response3 = client.get("/check-optional-header-param", headers = invalidHeaders)
        assert(response3.status == 400)
        assert(response3.body == "invalid or missing header param")
    }

    @Test fun `Required cookies are verified correctly`() {
        client.cookies["cookieParam"] = Cookie("cookieParam", "aValidValue")
        val response1 = client.get("/check-cookie-param")
        assert(response1.status == 200)
        assert(response1.body == "success")
        client.cookies.clear()

        client.cookies["cookieParam"] = Cookie("cookieParam", "anInvalidValue")
        val response2 = client.get("/check-cookie-param")
        assert(response2.status == 400)
        assert(response2.body == "invalid or missing cookie param")
        client.cookies.clear()

        val response3 = client.get("/check-cookie-param")
        assert(response3.status == 400)
        assert(response3.body == "invalid or missing cookie param")
    }

    @Test fun `Optional cookies are verified correctly`() {
        client.cookies["cookieParam"] = Cookie("cookieParam", "aValidValue")
        val response1 = client.get("/check-optional-cookie-param")
        assert(response1.status == 200)
        assert(response1.body == "success")
        client.cookies.clear()

        client.cookies["cookieParam"] = Cookie("cookieParam", "anInvalidValue")
        val response2 = client.get("/check-optional-cookie-param")
        assert(response2.status == 400)
        assert(response2.body == "invalid or missing cookie param")
        client.cookies.clear()

        val response3 = client.get("/check-optional-cookie-param")
        assert(response3.status == 200)
        assert(response3.body == "success")
    }

    @Test fun `Body is verified correctly`() {
        val response1 = client.get("/check-body", body = "Some body content")
        assert(response1.status == 200)
        assert(response1.body == "success")

        val response2 = client.get("/check-body")
        assert(response2.status == 400)
        assert(response2.body == "invalid or missing request body")
    }

    @Test fun `If Authorization is optional, it is skipped`() {
        val response1 = client.get("/check-optional-auth")
        assert(response1.status == 200)
        assert(response1.body == "success")

        val headers = mapOf("Authorization" to listOf("Basic dGVzdDEwMDA6aW1vam8xMjM="))
        val response2 = client.get("/check-optional-auth", headers = headers)
        assert(response2.status == 200)
        assert(response2.body == "success")
    }

    @Test fun `Basic HTTP Authentication is verified correctly`() {
        val response1 = client.get("/check-basic-auth")
        assert(response1.status == 401)
        assert(response1.body == "Invalid authorization credentials")

        val headers = mapOf("Authorization" to listOf("Basic dGVzdDEwMDA6aW1vam8xMjM="))
        val response2 = client.get("/check-basic-auth", headers = headers)
        assert(response2.status == 200)
        assert(response2.body == "success")
    }

    @Test fun `Bearer HTTP Authentication is verified correctly`() {
        val response1 = client.get("/check-bearer-auth")
        assert(response1.status == 401)
        assert(response1.body == "Invalid authorization credentials")

        val headers = mapOf("Authorization" to listOf("Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1aWQiOjI5NDc1LCJleHAiOjE3MDg2MDI1OTEsImlhdCI6MTYwMjA3MTM5MSwidXNlcl90eXBlIjoiMyJ9.oeeIax23lgfEY_rDt_iDXP5cONAXUgfoWZ43A4XCLIw"))
        val response2 = client.get("/check-bearer-auth", headers = headers)
        assert(response2.status == 200)
        assert(response2.body == "success")
    }

    @Test fun `HTTP Authentication with unknown scheme throws error`() {
        val response = client.get("/check-unknown-auth")
        assert(response.status == 500)
        assert(response.body != null)
        response.body?.let {
            assert(it.contains("Currently the Mock Server only supports Basic and Bearer HTTP Authentication"))
        }
    }

    @Test fun `Query param API Key Authentication is verified correctly`() {
        val response1 = client.get("/check-query-api-auth")
        assert(response1.status == 401)
        assert(response1.body == "Invalid authorization credentials")

        val response2 = client.get("/check-query-api-auth?api_key=abcdefg")
        assert(response2.status == 200)
        assert(response2.body == "success")
    }

    @Test fun `Header API Key Authentication is verified correctly`() {
        val response1 = client.get("/check-header-api-auth")
        assert(response1.status == 401)
        assert(response1.body == "Invalid authorization credentials")

        val headers = mapOf("api_key" to listOf("abcdefg"))
        val response2 = client.get("/check-header-api-auth", headers = headers)
        assert(response2.status == 200)
        assert(response2.body == "success")
    }

    @Test fun `Cookie API Key Authentication is verified correctly`() {
        val response1 = client.get("/check-cookie-api-auth")
        assert(response1.status == 401)
        assert(response1.body == "Invalid authorization credentials")

        client.cookies["api_key"] = Cookie("api_key", "abcdefg")
        val response2 = client.get("/check-cookie-api-auth")
        assert(response2.status == 200)
        assert(response2.body == "success")
        client.cookies.clear()
    }

    @Test fun `Unknown location API Key Authentication throws error`() {
        val response = client.get("/check-unknown-api-auth")
        assert(response.status == 500)
        assert(response.body != null)
        response.body?.let {
            assert(it.contains("Unknown `in` value found in OpenAPI Spec for security scheme"))
        }
    }

    @Test fun `When there are multiple security mechanisms, any one needs to be satisfied`() {
        val response1 = client.get("/check-multiple-mechanisms")
        assert(response1.status == 401)
        assert(response1.body == "Invalid authorization credentials")

        client.cookies["api_key"] = Cookie("api_key", "abcdefg")
        val response2 = client.get("/check-multiple-mechanisms")
        assert(response2.status == 200)
        assert(response2.body == "success")
        client.cookies.clear()

        val headers = mapOf("Authorization" to listOf("Basic dGVzdDEwMDA6aW1vam8xMjM="))
        val response3 = client.get("/check-multiple-mechanisms", headers = headers)
        assert(response3.status == 200)
        assert(response3.body == "success")
    }

    @Test fun `When there are multiple security schemes, all of them need to be satisfied`() {
        val response1 = client.get("/check-multiple-mechanisms")
        assert(response1.status == 401)
        assert(response1.body == "Invalid authorization credentials")

        client.cookies["api_key"] = Cookie("api_key", "abcdefg")
        val response2 = client.get("/check-multiple-schemes")
        assert(response2.status == 401)
        assert(response2.body == "Invalid authorization credentials")
        client.cookies.clear()

        val headers = mapOf("Authorization" to listOf("Basic dGVzdDEwMDA6aW1vam8xMjM="))
        val response3 = client.get("/check-multiple-schemes", headers = headers)
        assert(response3.status == 401)
        assert(response3.body == "Invalid authorization credentials")

        client.cookies["api_key"] = Cookie("api_key", "abcdefg")
        val response4 = client.get("/check-multiple-schemes", headers = headers)
        assert(response4.status == 200)
        assert(response4.body == "success")
        client.cookies.clear()
    }

    @AfterAll fun tearDown() {
        server.stop()
    }
}
