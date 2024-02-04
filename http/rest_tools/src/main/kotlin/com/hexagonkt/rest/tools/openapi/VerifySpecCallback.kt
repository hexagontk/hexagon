package com.hexagonkt.rest.tools.openapi

import com.atlassian.oai.validator.OpenApiInteractionValidator
import com.atlassian.oai.validator.OpenApiInteractionValidator.createForInlineApiSpecification
import com.atlassian.oai.validator.model.Request
import com.atlassian.oai.validator.model.Request.Method
import com.atlassian.oai.validator.model.Response
import com.atlassian.oai.validator.model.SimpleRequest
import com.atlassian.oai.validator.model.SimpleResponse
import com.atlassian.oai.validator.report.ValidationReport
import com.atlassian.oai.validator.report.ValidationReport.Message
import com.hexagonkt.http.handlers.HttpCallback
import com.hexagonkt.http.handlers.HttpContext
import com.hexagonkt.http.model.ContentType
import com.hexagonkt.http.model.HttpMethod
import com.hexagonkt.http.model.HttpMethod.*
import java.net.URL
import kotlin.jvm.optionals.getOrNull

/**
 * Callback that verifies server calls comply with a given OpenAPI spec.
 */
class VerifySpecCallback(spec: URL) : HttpCallback {

    private val messagePrefix: String = "\n- "
    private val specText: String = spec.readText()
    private val validator: OpenApiInteractionValidator =
        createForInlineApiSpecification(specText).build()

    override fun invoke(context: HttpContext): HttpContext {
        val requestReport = validator.validateRequest(request(context))

        val result = context.next()

        val resultMethod = method(result.method)
        val responseReport = validator.validateResponse(result.path, resultMethod, response(result))

        val callReport = responseReport.merge(requestReport)

        return if (callReport.hasErrors()) result.badRequest(message(callReport))
        else result
    }

    private fun message(report: ValidationReport): String {
        val messages = report.messages.map(::messageToText).distinct()
        return messages.joinToString(messagePrefix, "Invalid call:$messagePrefix")
    }

    private fun messageToText(it: Message): String {
        val level = it.level
        val key = it.key
        val context = it.context
            .map { c ->
                val op = c.apiOperation
                    .getOrNull()
                    ?.let { ao ->
                        val method = ao.method
                        val apiPath = ao.apiPath
                        "$method ${apiPath.normalised()}"
                    }
                    ?: ""

                val loc = c.location.getOrNull()?.name ?: ""

                "$op $loc"
            }
            .orElse("")

        val message = it.message
        val additionalInfo = it.additionalInfo
        val nestedMessages = it.nestedMessages

        return "$level: $key [$context] $message $additionalInfo $nestedMessages"
    }

    private fun request(context: HttpContext): Request {
        val request = context.request
        val builder = SimpleRequest.Builder(method(context.method), context.path, true)

        if (request.bodyString().isNotEmpty())
            builder.withBody(request.bodyString())

        request.contentType?.text?.let(builder::withContentType)
        request.headers.httpFields.values.forEach { builder.withHeader(it.name, it.strings()) }
        request.accept.map(ContentType::text).forEach(builder::withAccept)
        request.authorization?.text?.let(builder::withAuthorization)
        request.queryParameters.httpFields.values.forEach {
            builder.withQueryParam(it.name, it.strings())
        }

        return builder.build()
    }

    private fun response(context: HttpContext): Response {
        val response = context.response
        val builder = SimpleResponse.Builder(context.status.code)

        builder.withBody(response.bodyString())

        response.contentType?.text?.let(builder::withContentType)
        response.headers.httpFields.values.forEach { builder.withHeader(it.name, it.strings()) }

        return builder.build()
    }

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
