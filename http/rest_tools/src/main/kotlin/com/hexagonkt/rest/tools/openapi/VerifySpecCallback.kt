package com.hexagonkt.rest.tools.openapi

import com.atlassian.oai.validator.OpenApiInteractionValidator
import com.atlassian.oai.validator.OpenApiInteractionValidator.createForInlineApiSpecification
import com.atlassian.oai.validator.model.Request
import com.atlassian.oai.validator.model.Response
import com.atlassian.oai.validator.model.Request.Method
import com.atlassian.oai.validator.model.SimpleRequest
import com.atlassian.oai.validator.model.SimpleResponse
import com.atlassian.oai.validator.report.ValidationReport
import com.hexagonkt.core.fail
import com.hexagonkt.core.require
import com.hexagonkt.http.handlers.HttpCallback
import com.hexagonkt.http.handlers.HttpContext
import com.hexagonkt.http.model.BAD_REQUEST_400
import com.hexagonkt.http.model.ContentType
import com.hexagonkt.http.model.HttpMethod
import com.hexagonkt.http.model.HttpMethod.*
import com.hexagonkt.http.model.UNAUTHORIZED_401
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.security.SecurityScheme.Type
import io.swagger.v3.parser.OpenAPIV3Parser
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

    private val openAPIParser = OpenAPIV3Parser()
    private val openAPISpec: OpenAPI = openAPIParser.read(spec.path)
        ?: error("OpenAPI Spec could not be read. Please check the file's path and its format")

    override fun invoke(context: HttpContext): HttpContext {
        val requestReport = validator.validateRequest(request(context))

        val result = context.next()

        val resultMethod = method(result.method)
        val responseReport = validator.validateResponse(result.path, resultMethod, response(result))

        responseReport.merge(requestReport)

        return if (responseReport.hasErrors()) result.badRequest(message(responseReport))
        else result
    }

    private fun message(report: ValidationReport): String {
        return report.messages.joinToString(messagePrefix, "Invalid request:$messagePrefix") {
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

            "$level: $key [$context] $message $additionalInfo $nestedMessages"
        }
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

    private fun verifyAuth(operation: Operation, call: HttpContext): HttpContext? {
        if (operation.security == null
            || operation.security.size == 0
            || containsEmptySecurityRequirement(operation))
            return null

        // Any one of the security mechanisms need to be satisfied
        return if (!operation.security.any { securityRequirement -> verifySecurityRequirement(securityRequirement, call) }) {
            call.send(status = UNAUTHORIZED_401)
        }
        else null
    }

    private fun verifySecurityRequirement(
        securityRequirement: SecurityRequirement, call: HttpContext): Boolean =
        securityRequirement.keys.all { verifySecurityScheme(it, call) }

    private fun verifySecurityScheme(schemeName: String, call: HttpContext): Boolean {
        val securityScheme = openAPISpec.components.securitySchemes[schemeName]
            ?: error("The OpenAPI Spec contains no security scheme component for $schemeName")

        return when (securityScheme.type) {
            Type.APIKEY -> validateApiKey(securityScheme, call)
            Type.HTTP -> validateHttpAuth(securityScheme, call)
            else -> error("Mock Server only supports HTTP and API Key authentication")
        }
    }

    private fun validateApiKey(securityScheme: SecurityScheme, call: HttpContext): Boolean =
        when (securityScheme.`in`) {
            SecurityScheme.In.QUERY ->
                call.request.queryParameters[securityScheme.name]?.string()?.isNotBlank() ?: fail
            SecurityScheme.In.HEADER ->
                call.request.headers[securityScheme.name]?.string().isNullOrBlank()
            SecurityScheme.In.COOKIE ->
                call.request.cookiesMap()[securityScheme.name] != null
            else ->
                error("Unknown `in` value found in OpenAPI Spec for security scheme")
        }

    private fun validateHttpAuth(securityScheme: SecurityScheme, call: HttpContext): Boolean =
        when (securityScheme.scheme.lowercase()) {
            "basic" -> {
                call.request.headers["authorization"]?.string()?.let { authString ->
                    authString.isNotBlank() && authString.startsWith("Basic")
                } ?: false
            }
            "bearer" -> {
                call.request.headers["authorization"]?.string()?.let { authString ->
                    authString.isNotBlank() && authString.startsWith("Bearer")
                } ?: false
            }
            else ->
                error("Mock Server only supports Basic and Bearer HTTP Authentication")
        }

    private fun verifyParams(operation: Operation, call: HttpContext): HttpContext? {
        operation.parameters?.forEach { parameter ->
            when (parameter.`in`) {
                "path" -> {
                    if (!verifyPathParam(parameter, call)) {
                        call.send(
                            status = BAD_REQUEST_400,
                        )
                    }
                }
                "query" -> {
                    if (!verifyQueryParam(parameter, call)) {
                        call.send(
                            status = BAD_REQUEST_400,
                        )
                    }
                }
                "header" -> {
                    if (!verifyHeaderParam(parameter, call)) {
                        call.send(
                            status = BAD_REQUEST_400,
                        )
                    }
                }
                "cookie" -> {
                    if (!verifyCookieParam(parameter, call)) {
                        call.send(
                            status = BAD_REQUEST_400,
                        )
                    }
                }
            }
        }

        return null
    }

    private fun verifyPathParam(parameter: Parameter, call: HttpContext): Boolean {
        if (call.pathParameters[parameter.name].isNullOrBlank()) return false
        parameter.schema.enum?.let {
            if (call.pathParameters[parameter.name] !in it) return false
        }
        return true
    }

    private fun verifyQueryParam(parameter: Parameter, call: HttpContext): Boolean {
        if (call.request.queryParameters.require(parameter.name).string().isNullOrBlank()) {
            return !parameter.required
        }
        parameter.schema.enum?.let {
            if (call.request.queryParameters[parameter.name] !in it) return false
        }
        return true
    }

    private fun verifyHeaderParam(parameter: Parameter, call: HttpContext): Boolean {
        if (call.request.headers[parameter.name]?.string().isNullOrBlank()) {
            return !parameter.required
        }
        parameter.schema.enum?.let {
            if (call.request.headers[parameter.name] !in it) return false
        }
        return true
    }

    private fun verifyCookieParam(parameter: Parameter, call: HttpContext): Boolean {
        if (call.request.cookiesMap()[parameter.name] == null) {
            return !parameter.required
        }
        parameter.schema.enum?.let {
            if (call.request.cookiesMap()[parameter.name]?.value !in it) return false
        }
        return true
    }

    private fun verifyBody(operation: Operation, call: HttpContext): HttpContext? {
        operation.requestBody?.let { requestBody ->
            if (requestBody.required && call.request.bodyString().isBlank()) {
                call.send(
                    status = BAD_REQUEST_400,
                )
            }
        }
        return null
    }

    private fun containsEmptySecurityRequirement(operation: Operation): Boolean =
        operation.security.any { it.size == 0 }
}
