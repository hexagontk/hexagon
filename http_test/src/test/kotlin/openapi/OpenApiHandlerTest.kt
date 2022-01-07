package com.hexagonkt.http.test.openapi

import com.hexagonkt.core.helpers.multiMapOf
import com.hexagonkt.http.client.HttpClient
import com.hexagonkt.http.client.jetty.JettyClientAdapter
import com.hexagonkt.http.model.ClientErrorStatus.*
import com.hexagonkt.http.model.HttpCookie
import com.hexagonkt.http.model.ServerErrorStatus.INTERNAL_SERVER_ERROR
import com.hexagonkt.http.model.SuccessStatus.OK
import com.hexagonkt.http.server.HttpServer
import com.hexagonkt.http.server.jetty.JettyServletAdapter
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import java.net.URL

@Disabled
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class OpenApiHandlerTest {

    private val server by lazy {
        HttpServer(JettyServletAdapter(), OpenApiHandler("openapi_test.json").createServer())
    }

    private val client by lazy {
        HttpClient(JettyClientAdapter(), URL("http://localhost:${server.runtimePort}"))
    }

    @BeforeAll fun setUp() {
        server.start()
        assert(server.started())
    }

    @AfterAll fun tearDown() {
        server.stop()
    }

    @Test fun `Basic routes are created correctly`() = runBlocking {
        val response = client.get("/ping")
        assert(response.status == OK)
        assert(response.body == "pong")
    }

    @Test fun `Examples are fetched from media-type schema correctly`() = runBlocking {
        val response = client.get("/get-example-from-schema")
        assert(response.status == OK)
        assert(response.body == "response")
    }

    @Test fun `Examples are fetched from media type correctly`() = runBlocking {
        val response = client.get("/get-example-from-mediatype")
        assert(response.status == OK)
        assert(response.body == "response")
    }

    @Test fun `Examples are fetched from multiple examples correctly`() = runBlocking {
        val response = client.get("/get-from-multiple-examples")
        assert(response.status == OK)
        assert(response.body in listOf("foo", "bar"))
    }

    @Test fun `X-Mock-Response-Example is fetched from multiple examples correctly`() = runBlocking {
        val headers = multiMapOf("X-Mock-Response-Example" to "example2")
        val response = client.get("/get-from-multiple-examples", headers = headers)
        assert(response.status == OK)
        assert(response.body == "bar")
    }

    @Test fun `Empty string is returned if no examples specified`() = runBlocking {
        val response = client.get("/get-from-no-examples")
        assert(response.status == INTERNAL_SERVER_ERROR)
    }

    @Test fun `Paths not present in OpenAPI spec return 404`() = runBlocking {
        val response = client.get("/unknown-path")
        assert(response.status == NOT_FOUND)
    }

    @Test fun `Required query params are verified correctly`() = runBlocking {
        val response1 = client.get("/check-query-param")
        assert(response1.status == BAD_REQUEST)
        assert(response1.body == "invalid or missing query param")

        val response2 = client.get("/check-query-param?queryParam=aValidValue")
        assert(response2.status == OK)
        assert(response2.body == "success")

        val response3 = client.get("/check-query-param?queryParam=anInvalidValue")
        assert(response3.status == BAD_REQUEST)
        assert(response3.body == "invalid or missing query param")
    }

    @Test fun `Optional query params are verified correctly`() = runBlocking {
        val response1 = client.get("/check-optional-query-param")
        assert(response1.status == OK)
        assert(response1.body == "success")

        val response2 = client.get("/check-optional-query-param?queryParam=aValidValue")
        assert(response2.status == OK)
        assert(response2.body == "success")

        val response3 = client.get("/check-optional-query-param?queryParam=anInvalidValue")
        assert(response3.status == BAD_REQUEST)
        assert(response3.body == "invalid or missing query param")
    }

    @Test fun `Path params are verified correctly`() = runBlocking {
        val response1 = client.get("/check-path-param/aValidValue")
        assert(response1.status == OK)
        assert(response1.body == "success")

        val response2 = client.get("/check-path-param/anInvalidValue")
        assert(response2.status == BAD_REQUEST)
        assert(response2.body == "invalid or missing path param")
    }

    @Test fun `Required header params are verified correctly`() = runBlocking {
        val response1 = client.get("/check-header-param")
        assert(response1.status == BAD_REQUEST)
        assert(response1.body == "invalid or missing header param")

        val validHeaders = multiMapOf("headerParam" to "aValidValue")
        val response2 = client.get("/check-header-param", headers = validHeaders)
        assert(response2.status == OK)
        assert(response2.body == "success")

        val invalidHeaders = multiMapOf("headerParam" to "anInvalidValue")
        val response3 = client.get("/check-header-param", headers = invalidHeaders)
        assert(response3.status == BAD_REQUEST)
        assert(response3.body == "invalid or missing header param")
    }

    @Test fun `Optional header params are verified correctly`() = runBlocking {
        val response1 = client.get("/check-optional-header-param")
        assert(response1.status == OK)
        assert(response1.body == "success")

        val validHeaders = multiMapOf("headerParam" to "aValidValue")
        val response2 = client.get("/check-optional-header-param", headers = validHeaders)
        assert(response2.status == OK)
        assert(response2.body == "success")

        val invalidHeaders = multiMapOf("headerParam" to "anInvalidValue")
        val response3 = client.get("/check-optional-header-param", headers = invalidHeaders)
        assert(response3.status == BAD_REQUEST)
        assert(response3.body == "invalid or missing header param")
    }

    @Test fun `Required cookies are verified correctly`() = runBlocking {
        client.cookies += HttpCookie("cookieParam", "aValidValue")
        val response1 = client.get("/check-cookie-param")
        assert(response1.status == OK)
        assert(response1.body == "success")
        client.cookies = emptyList()

        client.cookies += HttpCookie("cookieParam", "anInvalidValue")
        val response2 = client.get("/check-cookie-param")
        assert(response2.status == BAD_REQUEST)
        assert(response2.body == "invalid or missing cookie param")
        client.cookies = emptyList()

        val response3 = client.get("/check-cookie-param")
        assert(response3.status == BAD_REQUEST)
        assert(response3.body == "invalid or missing cookie param")
    }

    @Test fun `Optional cookies are verified correctly`() = runBlocking {
        client.cookies += HttpCookie("cookieParam", "aValidValue")
        val response1 = client.get("/check-optional-cookie-param")
        assert(response1.status == OK)
        assert(response1.body == "success")
        client.cookies = emptyList()

        client.cookies += HttpCookie("cookieParam", "anInvalidValue")
        val response2 = client.get("/check-optional-cookie-param")
        assert(response2.status == BAD_REQUEST)
        assert(response2.body == "invalid or missing cookie param")
        client.cookies = emptyList()

        val response3 = client.get("/check-optional-cookie-param")
        assert(response3.status == OK)
        assert(response3.body == "success")
    }

    @Test fun `Body is verified correctly`() = runBlocking {
        val response1 = client.get("/check-body", body = "Some body content")
        assert(response1.status == OK)
        assert(response1.body == "success")

        val response2 = client.get("/check-body")
        assert(response2.status == BAD_REQUEST)
        assert(response2.body == "invalid or missing request body")
    }

    @Test fun `If Authorization is optional, it is skipped`() = runBlocking {
        val response1 = client.get("/check-optional-auth")
        assert(response1.status == OK)
        assert(response1.body == "success")

        val headers = multiMapOf("Authorization" to "Basic dGVzdDEwMDA6aW1vam8xMjM=")
        val response2 = client.get("/check-optional-auth", headers = headers)
        assert(response2.status == OK)
        assert(response2.body == "success")
    }

    @Test fun `Basic HTTP Authentication is verified correctly`() = runBlocking {
        val response1 = client.get("/check-basic-auth")
        assert(response1.status == UNAUTHORIZED)
        assert(response1.body == "Invalid authorization credentials")

        val headers = multiMapOf("Authorization" to "Basic dGVzdDEwMDA6aW1vam8xMjM=")
        val response2 = client.get("/check-basic-auth", headers = headers)
        assert(response2.status == OK)
        assert(response2.body == "success")
    }

    @Test fun `Bearer HTTP Authentication is verified correctly`() = runBlocking {
        val response1 = client.get("/check-bearer-auth")
        assert(response1.status == UNAUTHORIZED)
        assert(response1.body == "Invalid authorization credentials")

        val headers = multiMapOf("Authorization" to "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1aWQiOjI5NDc1LCJleHAiOjE3MDg2MDI1OTEsImlhdCI6MTYwMjA3MTM5MSwidXNlcl90eXBlIjoiMyJ9.oeeIax23lgfEY_rDt_iDXP5cONAXUgfoWZ43A4XCLIw")
        val response2 = client.get("/check-bearer-auth", headers = headers)
        assert(response2.status == OK)
        assert(response2.body == "success")
    }

    @Test fun `HTTP Authentication with unknown scheme throws error`() = runBlocking {
        val response = client.get("/check-unknown-auth")
        assert(response.status == INTERNAL_SERVER_ERROR)
        assert(response.bodyString().contains("Currently the Mock Server only supports Basic and Bearer HTTP Authentication"))
    }

    @Test fun `Query param API Key Authentication is verified correctly`() = runBlocking {
        val response1 = client.get("/check-query-api-auth")
        assert(response1.status == UNAUTHORIZED)
        assert(response1.body == "Invalid authorization credentials")

        val response2 = client.get("/check-query-api-auth?api_key=abcdefg")
        assert(response2.status == OK)
        assert(response2.body == "success")
    }

    @Test fun `Header API Key Authentication is verified correctly`() = runBlocking {
        val response1 = client.get("/check-header-api-auth")
        assert(response1.status == UNAUTHORIZED)
        assert(response1.body == "Invalid authorization credentials")

        val headers = multiMapOf("api_key" to "abcdefg")
        val response2 = client.get("/check-header-api-auth", headers = headers)
        assert(response2.status == OK)
        assert(response2.body == "success")
    }

    @Test fun `Cookie API Key Authentication is verified correctly`() = runBlocking {
        val response1 = client.get("/check-cookie-api-auth")
        assert(response1.status == UNAUTHORIZED)
        assert(response1.body == "Invalid authorization credentials")

        client.cookies += HttpCookie("api_key", "abcdefg")
        val response2 = client.get("/check-cookie-api-auth")
        assert(response2.status == OK)
        assert(response2.body == "success")
        client.cookies = emptyList()
    }

    @Test fun `Unknown location API Key Authentication throws error`() = runBlocking {
        val response = client.get("/check-unknown-api-auth")
        assert(response.status == INTERNAL_SERVER_ERROR)
        assert(response.bodyString().contains("Unknown `in` value found in OpenAPI Spec for security scheme"))
    }

    @Test fun `When there are multiple security mechanisms, any one needs to be satisfied`() = runBlocking {
        val response1 = client.get("/check-multiple-mechanisms")
        assert(response1.status == UNAUTHORIZED)
        assert(response1.body == "Invalid authorization credentials")

        client.cookies += HttpCookie("api_key", "abcdefg")
        val response2 = client.get("/check-multiple-mechanisms")
        assert(response2.status == OK)
        assert(response2.body == "success")
        client.cookies = emptyList()

        val headers = multiMapOf("Authorization" to "Basic dGVzdDEwMDA6aW1vam8xMjM=")
        val response3 = client.get("/check-multiple-mechanisms", headers = headers)
        assert(response3.status == OK)
        assert(response3.body == "success")
    }

    @Test fun `When there are multiple security schemes, all of them need to be satisfied`() = runBlocking {
        val response1 = client.get("/check-multiple-mechanisms")
        assert(response1.status == UNAUTHORIZED)
        assert(response1.body == "Invalid authorization credentials")

        client.cookies += HttpCookie("api_key", "abcdefg")
        val response2 = client.get("/check-multiple-schemes")
        assert(response2.status == UNAUTHORIZED)
        assert(response2.body == "Invalid authorization credentials")
        client.cookies = emptyList()

        val headers = multiMapOf("Authorization" to "Basic dGVzdDEwMDA6aW1vam8xMjM=")
        val response3 = client.get("/check-multiple-schemes", headers = headers)
        assert(response3.status == UNAUTHORIZED)
        assert(response3.body == "Invalid authorization credentials")

        client.cookies += HttpCookie("api_key", "abcdefg")
        val response4 = client.get("/check-multiple-schemes", headers = headers)
        assert(response4.status == OK)
        assert(response4.body == "success")
        client.cookies = emptyList()
    }
}
