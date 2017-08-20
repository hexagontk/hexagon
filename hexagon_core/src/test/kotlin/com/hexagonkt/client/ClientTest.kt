package com.hexagonkt.client

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options as wmoptions
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
    val templateTransformer = ResponseTemplateTransformer(true)
    val options: WireMockConfiguration = wmoptions().extensions(templateTransformer).dynamicPort()
    val server = WireMockServer(options)
    val client by lazy { Client("http://localhost:${server.port()}", "application/json") }

    @BeforeClass
    fun startup() {
        server.start()
        configureFor(server.port())

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
        server.stop()
    }

    fun json_requests_works_as_expected() {
        val expectedBody = "{\n  \"foo\" : \"fighters\",\n  \"es\" : \"áéíóúÁÉÍÓÚñÑ\"\n}"
        val requestBody = mapOf("foo" to "fighters", "es" to "áéíóúÁÉÍÓÚñÑ")

        val body = client.post("/", requestBody, "application/json").responseBody
        assert(body.trim() == expectedBody)

        val body2 = client.post("/", body = requestBody).responseBody
        assert(body2.trim() == expectedBody)

        client.get("/")
        client.get("/")
    }

    fun http_methods_with_objects_work_ok() {
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

    fun parameters_are_set_properly () {
        val endpoint = "http://localhost:${server.port()}"
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

    fun files_are_sent_in_base64 () {
        stubFor(post("/file")
            .willReturn(aResponse()
                .withHeader("file64", "{{request.body}}")
            )
        )

        val r = client.post("/file", File("src/test/resources/data/tag.yaml"))
        assert (r.headers.get("file64").isNotEmpty())
        assert (r.statusCode == 200)
    }

    private fun checkResponse(response: Response, parameter: Map<String, String>?) {
        assert(response.statusCode == 200)
        assert(response.responseBody.trim() == parameter?.serialize()?.trim() ?: "")
    }
}
