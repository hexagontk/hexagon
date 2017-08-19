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

    private fun checkResponse(response: Response, parameter: Map<String, String>?) {
        assert(response.statusCode == 200)
        assert(response.responseBody.trim() == parameter?.serialize()?.trim() ?: "")
    }
}
