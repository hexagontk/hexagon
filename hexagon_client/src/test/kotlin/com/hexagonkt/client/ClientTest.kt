package com.hexagonkt.client

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options as wmOptions
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer
import com.hexagonkt.serialization.serialize

import org.asynchttpclient.Response
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.io.File

@Test
class ClientTest {
    private val templateTransformer = ResponseTemplateTransformer(true)
    private val wmExtensions = wmOptions().extensions(templateTransformer)
    private val wmOptions: WireMockConfiguration = wmExtensions.dynamicPort()
    private val wmServer = WireMockServer(wmOptions)
    private val client by lazy { Client("http://localhost:${wmServer.port()}", "application/json") }

    @BeforeClass
    fun startup() {
        wmServer.start()
        configureFor(wmServer.port())

        val resp = aResponse()
            .withHeader("content-type", "application/json;charset=utf-8")
            .withBody("{{{request.body}}}")

        stubFor(post(anyUrl()).willReturn(resp))
        stubFor(post(anyUrl()).willReturn(resp))
        stubFor(get(anyUrl()).willReturn(resp))
        stubFor(head(anyUrl()).willReturn(resp))
        stubFor(put(anyUrl()).willReturn(resp))
        stubFor(delete(anyUrl()).willReturn(resp))
        stubFor(trace(anyUrl()).willReturn(resp))
        stubFor(options(anyUrl()).willReturn(resp))
        stubFor(patch(anyUrl()).willReturn(resp))
    }

    @AfterClass
    fun shutdown() {
        wmServer.stop()
    }

    fun `json requests works as expected`() {
        val expectedBody = "{\n  \"foo\" : \"fighters\",\n  \"es\" : \"áéíóúÁÉÍÓÚñÑ\"\n}"
        val requestBody = mapOf("foo" to "fighters", "es" to "áéíóúÁÉÍÓÚñÑ")

        val body = client.post("/", requestBody, "application/json").responseBody
        assert(body.trim() == expectedBody)

        val body2 = client.post("/", body = requestBody).responseBody
        assert(body2.trim() == expectedBody)

        client.get("/")
        client.get("/")
    }

    fun `http methods with objects work ok`() {
        val parameter = mapOf("key" to "value")
        checkResponse(client.get("/"), null)
        checkResponse(client.head("/"), null)
        checkResponse(client.post("/", parameter), parameter)
        checkResponse(client.put("/", parameter), parameter)
        checkResponse(client.delete("/", parameter), parameter)
        checkResponse(client.trace("/", parameter), parameter)
        checkResponse(client.options("/", parameter), parameter)
        checkResponse(client.patch("/", parameter), parameter)
    }

    fun `parameters are set properly` () {
        val endpoint = "http://localhost:${wmServer.port()}"
        val h = mapOf("header1" to listOf("val1", "val2"))
        val c = Client(endpoint, "application/json", false, h, "user", "password", true)

        assert(c.contentType == "application/json")
        assert(!c.useCookies)
        assert(c.headers == h)

        val rn = "request.headers.header1"

        stubFor(get("/auth")
            .willReturn(aResponse()
                .withHeader("auth", "{{request.headers.Authorization}}")
                .withHeader("head1", "{{$rn.[0]}}{{$rn.[1]}}")
            )
        )

        val r = c.get("/auth")
        assert (r.headers.get("auth").startsWith("Basic"))
        assert (r.headers.get("head1").contains("val1"))
        assert (r.headers.get("head1").contains("val2"))
        assert (r.statusCode == 200)
    }

    fun `files are sent in base64` () {
        stubFor(post("/file")
            .willReturn(aResponse()
                .withHeader("file64", "{{request.body}}")
            )
        )

        val r = client.post("/file", File("src/test/resources/logback-test.xml"))
        assert (r.headers.get("file64").isNotEmpty())
        assert (r.statusCode == 200)
    }

    fun `strings are sent properly` () {
        stubFor(post("/string")
            .willReturn(aResponse()
                .withHeader("body", "{{request.body}}")
            )
        )

        val r = client.post("/string", "text")
        assert (r.headers.get("body").isNotEmpty())
        assert (r.statusCode == 200)
    }

    private fun checkResponse(response: Response, parameter: Map<String, String>?) {
        assert(response.statusCode == 200)
        assert(response.responseBody.trim() == parameter?.serialize()?.trim() ?: "")
    }
}
