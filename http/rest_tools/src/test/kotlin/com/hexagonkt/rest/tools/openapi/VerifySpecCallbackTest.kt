package com.hexagonkt.rest.tools.openapi

import com.hexagonkt.core.logging.info
import com.hexagonkt.core.media.APPLICATION_JSON
import com.hexagonkt.core.urlOf
import com.hexagonkt.http.handlers.HttpContext
import com.hexagonkt.http.model.*
import com.hexagonkt.serialization.SerializationManager
import com.hexagonkt.serialization.jackson.json.Json
import com.hexagonkt.serialization.jackson.yaml.Yaml
import com.hexagonkt.serialization.serialize
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class VerifySpecCallbackTest {

    private val verifySpecCallback = VerifySpecCallback(urlOf("classpath:petstore_openapi.json"))

    init {
        SerializationManager.formats = setOf(Json, Yaml)
    }

    @Test fun `Requests not complying with spec return an error`() {
        verify(errors = listOf("1"))
    }

    @Test fun `Requests complying with spec return the proper result`() {
        verify(
            HttpRequest(path = "/pet/1"),
            HttpResponse(
                status = OK_200,
                contentType = ContentType(APPLICATION_JSON),
                body = mapOf(
                    "name" to "Keka",
                    "photoUrls" to listOf("http://example.com")
                ),
            ),
        )
    }

    private fun verify(
        request: HttpRequestPort = HttpRequest(),
        response: HttpResponsePort = HttpResponse(status = OK_200),
        errors: List<String> = emptyList(),
    ) {
        val serializedResponse = response.contentType
            ?.let { response.with(body = response.body.serialize(it.mediaType)) }
            ?: response

        val serializedRequest = request.contentType
            ?.let { request.with(body = request.body.serialize(it.mediaType)) }
            ?: request

        val result = verifySpecCallback(HttpContext(serializedRequest, serializedResponse))

        val expectedStatus = if (errors.isEmpty()) OK_200 else BAD_REQUEST_400

        if (errors.isNotEmpty())
            result.response.bodyString().info()

        assertEquals(expectedStatus, result.status)
    }
}
