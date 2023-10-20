package com.hexagonkt.rest.tools.openapi

import com.atlassian.oai.validator.OpenApiInteractionValidator.createForInlineApiSpecification
import com.atlassian.oai.validator.model.Request
import com.atlassian.oai.validator.model.Response
import com.atlassian.oai.validator.model.Request.Method
import com.atlassian.oai.validator.model.SimpleRequest
import com.atlassian.oai.validator.model.SimpleResponse
import com.hexagonkt.http.handlers.HttpContext
import com.hexagonkt.http.model.HttpMethod
import com.hexagonkt.http.model.HttpMethod.*
import java.net.URL

/**
 * Callback that verifies server calls comply with a given OpenAPI spec.
 */
class VerifySpecCallback(spec: URL) : (HttpContext) -> HttpContext {

    private val validator = createForInlineApiSpecification(spec.readText()).build()

    override fun invoke(context: HttpContext): HttpContext {
        validator.validateRequest(request(context))

        val result = context.next()

        validator.validateResponse(result.path, method(result.method), response(result))

        return result
    }

    private fun request(context: HttpContext): Request =
        SimpleRequest.Builder(method(context.method), context.path, true)
            .build()

    private fun response(context: HttpContext): Response =
        SimpleResponse.Builder(context.status.code)
            .build()

    private fun method(method: HttpMethod): Method =
        when (method) {
            GET -> Method.GET
            HEAD -> Method.HEAD
            POST -> Method.POST
            PUT -> Method.PUT
            DELETE -> Method.DELETE
            TRACE -> Method.TRACE
            OPTIONS -> Method.OPTIONS
            PATCH -> Method.PATCH
        }
}
