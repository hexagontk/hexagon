package com.hexagonkt.rest.tools.openapi

import com.hexagonkt.core.logging.info
import com.hexagonkt.core.media.APPLICATION_JSON
import com.hexagonkt.core.urlOf
import com.hexagonkt.http.handlers.HttpContext
import com.hexagonkt.http.model.*
import com.hexagonkt.http.model.HttpMethod.*
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

    // TODO Check commented code (it should throw validation errors)
    @Test fun `Requests not complying with spec return an error`() {
        verify(errors = listOf("ERROR: validation.request.path.missing [ ] No API path found that matches request ''. [] []"))
        // 'status' query parameter with invalid value
//        verify(
//            HttpRequest(
//                path = "/pet/findByStatus",
//                queryParameters = QueryParameters(QueryParameter("status", "invalid"))
//            ),
//            HttpResponse(
//                status = OK_200,
//                contentType = ContentType(APPLICATION_JSON),
//                body = listOf(
//                    mapOf(
//                        "name" to "Keka",
//                        "photoUrls" to listOf("https://example.com")
//                    )
//                ),
//            ),
//            listOf("1")
//        )
        verify(
            HttpRequest(
                method = HEAD,
                path = "/pet/1",
                accept = listOf(ContentType(APPLICATION_JSON)),
            ),
            HttpResponse(
                status = OK_200,
                contentType = ContentType(APPLICATION_JSON),
                body = mapOf(
                    "name" to "Keka",
                    "photoUrls" to listOf("https://example.com")
                ),
            ),
            listOf("ERROR: validation.request.operation.notAllowed [ ] HEAD operation not allowed on path '/pet/1'. [] []")
        )
        verify(
            HttpRequest(
                method = POST,
                path = "/pet",
                contentType = ContentType(APPLICATION_JSON),
                body = mapOf(
                    "name" to "Keka",
                    "photoUrls" to listOf("https://example.com")
                )
            ),
            HttpResponse(
                status = OK_200,
                contentType = ContentType(APPLICATION_JSON),
                body = listOf(
                    mapOf(
                        "name" to "Keka",
                        "photoUrls" to listOf("https://example.com")
                    )
                ),
            ),
            listOf("ERROR: validation.response.body.schema.type [POST /pet RESPONSE] Instance type (array) does not match any allowed primitive type (allowed: [\"object\"]) [] []")
        )
        verify(
            HttpRequest(method = DELETE, path = "/pet/1"),
            HttpResponse(status = OK_200),
            listOf("ERROR: validation.response.status.unknown [DELETE /pet/{petId} RESPONSE] Response status 200 not defined for path '/pet/{petId}'. [] []")
        )
    }

    @Test fun `Requests complying with spec return the proper result`() {
        verify(
            HttpRequest(
                path = "/pet/1",
                accept = listOf(ContentType(APPLICATION_JSON)),
            ),
            HttpResponse(
                status = OK_200,
                contentType = ContentType(APPLICATION_JSON),
                body = mapOf(
                    "name" to "Keka",
                    "photoUrls" to listOf("https://example.com")
                ),
            ),
        )
        verify(
            HttpRequest(method = HEAD, path = "/pet/findByTags"),
            HttpResponse(status = OK_200),
        )
        verify(
            HttpRequest(method = TRACE, path = "/pet/findByTags"),
            HttpResponse(status = OK_200),
        )
        verify(
            HttpRequest(method = OPTIONS, path = "/pet/findByTags"),
            HttpResponse(status = OK_200),
        )
        verify(
            HttpRequest(method = PATCH, path = "/pet/findByTags"),
            HttpResponse(status = OK_200),
        )
        verify(
            HttpRequest(
                path = "/pet/findByStatus",
                queryParameters = QueryParameters(QueryParameter("status", "sold"))
            ),
            HttpResponse(
                status = OK_200,
                contentType = ContentType(APPLICATION_JSON),
                body = listOf(
                    mapOf(
                        "name" to "Keka",
                        "photoUrls" to listOf("https://example.com")
                    )
                ),
            ),
        )
        verify(
            HttpRequest(
                method = POST,
                path = "/pet",
                contentType = ContentType(APPLICATION_JSON),
                body = mapOf(
                    "name" to "Keka",
                    "photoUrls" to listOf("https://example.com")
                )
            ),
            HttpResponse(
                status = OK_200,
                contentType = ContentType(APPLICATION_JSON),
                body = mapOf(
                    "name" to "Keka",
                    "photoUrls" to listOf("https://example.com")
                ),
            ),
        )
        verify(
            HttpRequest(
                method = PUT,
                path = "/pet",
                contentType = ContentType(APPLICATION_JSON),
                body = mapOf(
                    "name" to "Keka",
                    "photoUrls" to listOf("https://example.com")
                )
            ),
            HttpResponse(
                status = OK_200,
                contentType = ContentType(APPLICATION_JSON),
                body = mapOf(
                    "name" to "Keka",
                    "photoUrls" to listOf("https://example.com")
                ),
            ),
        )
        verify(
            HttpRequest(
                method = PUT,
                path = "/pet",
                contentType = ContentType(APPLICATION_JSON),
                body = mapOf(
                    "name" to "Keka",
                    "photoUrls" to listOf("https://example.com")
                )
            ),
            HttpResponse(status = NOT_FOUND_404),
        )
        verify(
            HttpRequest(method = DELETE, path = "/pet/1"),
            HttpResponse(status = BAD_REQUEST_400),
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

        val bodyString = result.response.bodyString()
        val actualErrors =
            if (errors.isEmpty()) emptyList()
            else bodyString.info().lines().drop(1).map { it.removePrefix("- ") }

        val expectedStatus = if (errors.isEmpty()) response.status else BAD_REQUEST_400

        assertEquals(expectedStatus, result.status)
        assertEquals(errors, actualErrors)
    }
}
