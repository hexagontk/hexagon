package com.hexagonkt.client

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options as wmOptions
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer
import com.hexagonkt.serialization.JsonFormat
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
    private val client by lazy {
        Client("http://localhost:${wmServer.port()}", JsonFormat.contentType)
    }

    @BeforeClass
    fun startup() {
        wmServer.start()
        configureFor(wmServer.port())

        val resp = aResponse()
            .withHeader("content-type", "application/json;charset=utf-8")
            .withBody("{{{request.body}}}")

        stubFor(WireMock.post(anyUrl()).willReturn(resp))
        stubFor(WireMock.post(anyUrl()).willReturn(resp))
        stubFor(WireMock.get(anyUrl()).willReturn(resp))
        stubFor(WireMock.head(anyUrl()).willReturn(resp))
        stubFor(WireMock.put(anyUrl()).willReturn(resp))
        stubFor(WireMock.delete(anyUrl()).willReturn(resp))
        stubFor(WireMock.trace(anyUrl()).willReturn(resp))
        stubFor(WireMock.options(anyUrl()).willReturn(resp))
        stubFor(WireMock.patch(anyUrl()).willReturn(resp))
    }

    @AfterClass
    fun shutdown() {
        wmServer.stop()
    }

    fun `json requests works as expected`() {
        val expectedBody = "{\n  \"foo\" : \"fighters\",\n  \"es\" : \"áéíóúÁÉÍÓÚñÑ\"\n}"
        val requestBody = mapOf("foo" to "fighters", "es" to "áéíóúÁÉÍÓÚñÑ")

        val body = client.post("/", requestBody, JsonFormat.contentType).responseBody
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

    fun `http methods with objects work ok with default client`() {
        val parameter = mapOf("key" to "value")
        checkResponse(get("http://localhost:${wmServer.port()}"), null)
        checkResponse(head("http://localhost:${wmServer.port()}"), null)
        checkResponse(post("http://localhost:${wmServer.port()}", parameter), parameter)
        checkResponse(put("http://localhost:${wmServer.port()}", parameter), parameter)
        checkResponse(delete("http://localhost:${wmServer.port()}", parameter), parameter)
        checkResponse(trace("http://localhost:${wmServer.port()}", parameter), parameter)
        checkResponse(options("http://localhost:${wmServer.port()}", parameter), parameter)
        checkResponse(patch("http://localhost:${wmServer.port()}", parameter), parameter)
    }

    fun `parameters are set properly` () {
        val endpoint = "http://localhost:${wmServer.port()}"
        val h = mapOf("header1" to listOf("val1", "val2"))
        val c = Client(endpoint, JsonFormat.contentType, false, h, "user", "password", true)

        assert(c.contentType == JsonFormat.contentType)
        assert(!c.useCookies)
        assert(c.headers == h)

        val rn = "request.headers.header1"

        stubFor(WireMock.get("/auth")
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
        stubFor(WireMock.post("/file")
            .willReturn(aResponse()
                .withHeader("file64", "{{request.body}}")
            )
        )

        val file = File("src/test/resources/logback-test.xml").let {
            if (it.exists()) it
            else File("port_client/src/test/resources/logback-test.xml")
        }
        val r = client.post("/file", file)
        assert (r.headers.get("file64").isNotEmpty())
        assert (r.statusCode == 200)
    }

    fun `strings are sent properly` () {
        stubFor(WireMock.post("/string")
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
