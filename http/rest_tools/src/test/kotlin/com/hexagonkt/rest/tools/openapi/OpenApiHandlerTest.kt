package com.hexagonkt.rest.tools.openapi

import com.hexagonkt.core.urlOf
import com.hexagonkt.http.client.HttpClient
import com.hexagonkt.http.client.HttpClientSettings
import com.hexagonkt.http.client.jetty.JettyClientAdapter
import com.hexagonkt.http.model.*
import com.hexagonkt.http.server.HttpServer
import com.hexagonkt.http.server.jetty.JettyServletAdapter
import org.junit.jupiter.api.*
import kotlin.test.assertEquals

@Disabled // TODO Fix this functionality
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class OpenApiHandlerTest {

    private val server by lazy {
        HttpServer(JettyServletAdapter(), OpenApiHandler("openapi_test.json").createServer())
    }

    private val client by lazy {
        val settings = HttpClientSettings(urlOf("http://localhost:${server.runtimePort}"))
        HttpClient(JettyClientAdapter(), settings)
    }

    @BeforeAll fun setUp() {
        server.start()
        client.start()
        assert(server.started())
    }

    @AfterAll fun tearDown() {
        client.stop()
        server.stop()
    }

    @Test fun `Basic routes are created correctly`() {
        val response = client.get("/ping")
        assertEquals(OK_200, response.status)
        assertEquals("pong", response.body)
    }

    @Test fun `Examples are fetched from media-type schema correctly`() {
        val response = client.get("/get-example-from-schema")
        assertEquals(OK_200, response.status)
        assertEquals("response", response.body)
    }

    @Test fun `Examples are fetched from media type correctly`() {
        val response = client.get("/get-example-from-mediatype")
        assertEquals(OK_200, response.status)
        assertEquals("response", response.body)
    }

    @Test fun `Examples are fetched from multiple examples correctly`() {
        val response = client.get("/get-from-multiple-examples")
        assertEquals(OK_200, response.status)
        assert(response.body in listOf("foo", "bar"))
    }

    @Test fun `x-mock-response-example is fetched from multiple examples correctly`() {
        val headers = Headers(Header("x-mock-response-example", "example2"))
        val response = client.get("/get-from-multiple-examples", headers = headers)
        assertEquals(OK_200, response.status)
        assertEquals("bar", response.body)
    }

    @Test fun `Empty string is returned if no examples specified`() {
        val response = client.get("/get-from-no-examples")
        assertEquals(INTERNAL_SERVER_ERROR_500, response.status)
    }

    @Test fun `Paths not present in OpenAPI spec return 404`() {
        val response = client.get("/unknown-path")
        assertEquals(NOT_FOUND_404, response.status)
    }

    @Test fun `Required query params are verified correctly`() {
        val response1 = client.get("/check-query-param")
        assertEquals(BAD_REQUEST_400, response1.status)
        assertEquals("invalid or missing query param", response1.body)

        val response2 = client.get("/check-query-param?queryParam=aValidValue")
        assertEquals(OK_200, response2.status)
        assertEquals("success", response2.body)

        val response3 = client.get("/check-query-param?queryParam=anInvalidValue")
        assertEquals(BAD_REQUEST_400, response3.status)
        assertEquals("invalid or missing query param", response3.body)
    }

    @Test fun `Optional query params are verified correctly`() {
        val response1 = client.get("/check-optional-query-param")
        assertEquals(OK_200, response1.status)
        assertEquals("success", response1.body)

        val response2 = client.get("/check-optional-query-param?queryParam=aValidValue")
        assertEquals(OK_200, response2.status)
        assertEquals("success", response2.body)

        val response3 = client.get("/check-optional-query-param?queryParam=anInvalidValue")
        assertEquals(BAD_REQUEST_400, response3.status)
        assertEquals("invalid or missing query param", response3.body)
    }

    @Test fun `Path params are verified correctly`() {
        val response1 = client.get("/check-path-param/aValidValue")
        assertEquals(OK_200, response1.status)
        assertEquals("success", response1.body)

        val response2 = client.get("/check-path-param/anInvalidValue")
        assertEquals(BAD_REQUEST_400, response2.status)
        assertEquals("invalid or missing path param", response2.body)
    }

    @Test fun `Required header params are verified correctly`() {
        val response1 = client.get("/check-header-param")
        assertEquals(BAD_REQUEST_400, response1.status)
        assertEquals("invalid or missing header param", response1.body)

        val validHeaders = Headers(Header("header-param", "aValidValue"))
        val response2 = client.get("/check-header-param", headers = validHeaders)
        assertEquals(OK_200, response2.status)
        assertEquals("success", response2.body)

        val invalidHeaders = Headers(Header("header-param", "anInvalidValue"))
        val response3 = client.get("/check-header-param", headers = invalidHeaders)
        assertEquals(BAD_REQUEST_400, response3.status)
        assertEquals("invalid or missing header param", response3.body)
    }

    @Test fun `Optional header params are verified correctly`() {
        val response1 = client.get("/check-optional-header-param")
        assertEquals(OK_200, response1.status)
        assertEquals("success", response1.body)

        val validHeaders = Headers(Header("header-param", "aValidValue"))
        val response2 = client.get("/check-optional-header-param", headers = validHeaders)
        assertEquals(OK_200, response2.status)
        assertEquals("success", response2.body)

        val invalidHeaders = Headers(Header("header-param", "anInvalidValue"))
        val response3 = client.get("/check-optional-header-param", headers = invalidHeaders)
        assertEquals(BAD_REQUEST_400, response3.status)
        assertEquals("invalid or missing header param", response3.body)
    }

    @Test fun `Required cookies are verified correctly`() {
        client.cookies += Cookie("cookieParam", "aValidValue")
        val response1 = client.get("/check-cookie-param")
        assertEquals(OK_200, response1.status)
        assertEquals("success", response1.body)
        client.cookies = emptyList()

        client.cookies += Cookie("cookieParam", "anInvalidValue")
        val response2 = client.get("/check-cookie-param")
        assertEquals(BAD_REQUEST_400, response2.status)
        assertEquals("invalid or missing cookie param", response2.body)
        client.cookies = emptyList()

        val response3 = client.get("/check-cookie-param")
        assertEquals(BAD_REQUEST_400, response3.status)
        assertEquals("invalid or missing cookie param", response3.body)
    }

    @Test fun `Optional cookies are verified correctly`() {
        client.cookies += Cookie("cookieParam", "aValidValue")
        val response1 = client.get("/check-optional-cookie-param")
        assertEquals(OK_200, response1.status)
        assertEquals("success", response1.body)
        client.cookies = emptyList()

        client.cookies += Cookie("cookieParam", "anInvalidValue")
        val response2 = client.get("/check-optional-cookie-param")
        assertEquals(BAD_REQUEST_400, response2.status)
        assertEquals("invalid or missing cookie param", response2.body)
        client.cookies = emptyList()

        val response3 = client.get("/check-optional-cookie-param")
        assertEquals(OK_200, response3.status)
        assertEquals("success", response3.body)
    }

    @Test fun `Body is verified correctly`() {
        val response1 = client.get("/check-body", body = "Some body content")
        assertEquals(OK_200, response1.status)
        assertEquals("success", response1.body)

        val response2 = client.get("/check-body")
        assertEquals(BAD_REQUEST_400, response2.status)
        assertEquals("invalid or missing request body", response2.body)
    }

    @Test fun `If Authorization is optional, it is skipped`() {
        val response1 = client.get("/check-optional-auth")
        assertEquals(OK_200, response1.status)
        assertEquals("success", response1.body)

        val headers = Headers(Header("authorization", "Basic dGVzdDEwMDA6aW1vam8xMjM="))
        val response2 = client.get("/check-optional-auth", headers = headers)
        assertEquals(OK_200, response2.status)
        assertEquals("success", response2.body)
    }

    @Test fun `Basic HTTP Authentication is verified correctly`() {
        val response1 = client.get("/check-basic-auth")
        assertEquals(UNAUTHORIZED_401, response1.status)
        assertEquals("Invalid authorization credentials", response1.body)

        val headers = Headers(Header("authorization", "Basic dGVzdDEwMDA6aW1vam8xMjM="))
        val response2 = client.get("/check-basic-auth", headers = headers)
        assertEquals(OK_200, response2.status)
        assertEquals("success", response2.body)
    }

    @Test fun `Bearer HTTP Authentication is verified correctly`() {
        val response1 = client.get("/check-bearer-auth")
        assertEquals(UNAUTHORIZED_401, response1.status)
        assertEquals("Invalid authorization credentials", response1.body)

        val headers = Headers(Header("authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1aWQiOjI5NDc1LCJleHAiOjE3MDg2MDI1OTEsImlhdCI6MTYwMjA3MTM5MSwidXNlcl90eXBlIjoiMyJ9.oeeIax23lgfEY_rDt_iDXP5cONAXUgfoWZ43A4XCLIw"))
        val response2 = client.get("/check-bearer-auth", headers = headers)
        assertEquals(OK_200, response2.status)
        assertEquals("success", response2.body)
    }

    @Test fun `HTTP Authentication with unknown scheme throws error`() {
        val response = client.get("/check-unknown-auth")
        assertEquals(INTERNAL_SERVER_ERROR_500, response.status)
        assert(response.bodyString().contains("Currently the Mock Server only supports Basic and Bearer HTTP Authentication"))
    }

    @Test fun `Query param API Key Authentication is verified correctly`() {
        val response1 = client.get("/check-query-api-auth")
        assertEquals(UNAUTHORIZED_401, response1.status)
        assertEquals("Invalid authorization credentials", response1.body)

        val response2 = client.get("/check-query-api-auth?api-key=abcdefg")
        assertEquals(OK_200, response2.status)
        assertEquals("success", response2.body)
    }

    @Test fun `Header API Key Authentication is verified correctly`() {
        val response1 = client.get("/check-header-api-auth")
        assertEquals(UNAUTHORIZED_401, response1.status)
        assertEquals("Invalid authorization credentials", response1.body)

        val headers = Headers(Header("api-key", "abcdefg"))
        val response2 = client.get("/check-header-api-auth", headers = headers)
        assertEquals(OK_200, response2.status)
        assertEquals("success", response2.body)
    }

    @Test fun `Cookie API Key Authentication is verified correctly`() {
        val response1 = client.get("/check-cookie-api-auth")
        assertEquals(UNAUTHORIZED_401, response1.status)
        assertEquals("Invalid authorization credentials", response1.body)

        client.cookies += Cookie("api-key", "abcdefg")
        val response2 = client.get("/check-cookie-api-auth")
        assertEquals(OK_200, response2.status)
        assertEquals("success", response2.body)
        client.cookies = emptyList()
    }

    @Test fun `Unknown location API Key Authentication throws error`() {
        val response = client.get("/check-unknown-api-auth")
        assertEquals(INTERNAL_SERVER_ERROR_500, response.status)
        assert(response.bodyString().contains("Unknown `in` value found in OpenAPI Spec for security scheme"))
    }

    @Test fun `When there are multiple security mechanisms, any one needs to be satisfied`() {
        val response1 = client.get("/check-multiple-mechanisms")
        assertEquals(UNAUTHORIZED_401, response1.status)
        assertEquals("Invalid authorization credentials", response1.body)

        client.cookies += Cookie("api-key", "abcdefg")
        val response2 = client.get("/check-multiple-mechanisms")
        assertEquals(OK_200, response2.status)
        assertEquals("success", response2.body)
        client.cookies = emptyList()

        val headers = Headers(Header("authorization", "Basic dGVzdDEwMDA6aW1vam8xMjM="))
        val response3 = client.get("/check-multiple-mechanisms", headers = headers)
        assertEquals(OK_200, response3.status)
        assertEquals("success", response3.body)
    }

    @Test fun `When there are multiple security schemes, all of them need to be satisfied`() {
        val response1 = client.get("/check-multiple-mechanisms")
        assertEquals(UNAUTHORIZED_401, response1.status)
        assertEquals("Invalid authorization credentials", response1.body)

        client.cookies += Cookie("api-key", "abcdefg")
        val response2 = client.get("/check-multiple-schemes")
        assertEquals(UNAUTHORIZED_401, response2.status)
        assertEquals("Invalid authorization credentials", response2.body)
        client.cookies = emptyList()

        val headers = Headers(Header("authorization", "Basic dGVzdDEwMDA6aW1vam8xMjM="))
        val response3 = client.get("/check-multiple-schemes", headers = headers)
        assertEquals(UNAUTHORIZED_401, response3.status)
        assertEquals("Invalid authorization credentials", response3.body)

        client.cookies += Cookie("api-key", "abcdefg")
        val response4 = client.get("/check-multiple-schemes", headers = headers)
        assertEquals(OK_200, response4.status)
        assertEquals("success", response4.body)
        client.cookies = emptyList()
    }
}
