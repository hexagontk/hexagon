package com.hexagonkt.rest.tools.openapi

import com.hexagonkt.core.fail
import com.hexagonkt.core.require
import com.hexagonkt.http.model.BAD_REQUEST_400
import com.hexagonkt.http.model.HttpMethod
import com.hexagonkt.http.model.HttpMethod.*
import com.hexagonkt.http.model.UNAUTHORIZED_401
import com.hexagonkt.http.handlers.*

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.security.SecurityScheme.Type
import io.swagger.v3.parser.OpenAPIV3Parser

// TODO Validate bodies with vertx-json-schema
// TODO Check https://github.com/swagger-api/swagger-parser for route verification
internal class OpenApiHandler(pathToSpec: String) {

    private val openAPIParser = OpenAPIV3Parser()
    private val openAPISpec: OpenAPI = openAPIParser.read(pathToSpec)
        ?: error("OpenAPI Spec could not be read. Please check the file's path and its format")

    fun createServer(): HttpHandler =
        PathHandler(
            "",
            openAPISpec.paths.map { (path: String, pathItem: PathItem) ->
                when {
                    pathItem.get != null -> createHandler(GET, path, pathItem.get)
                    pathItem.head != null -> createHandler(HEAD, path, pathItem.head)
                    pathItem.post != null -> createHandler(POST, path, pathItem.post)
                    pathItem.put != null -> createHandler(PUT, path, pathItem.put)
                    pathItem.delete != null -> createHandler(DELETE, path, pathItem.delete)
                    pathItem.trace != null -> createHandler(TRACE, path, pathItem.trace)
                    pathItem.options != null -> createHandler(OPTIONS, path, pathItem.options)
                    pathItem.patch != null -> createHandler(PATCH, path, pathItem.patch)
                    else -> error("Unsupported method")
                }
            }
        )

    private fun createHandler(method: HttpMethod, path: String, operation: Operation): HttpHandler =
        OnHandler(method, path, handleRequest(operation))

    private fun handleRequest(operation: Operation): HttpCallback =
        {
            verifyAuth(operation, this)
                ?: verifyParams(operation, this)
                ?: verifyBody(operation, this)
                ?: ok(
                    getResponseContentForStatus(
                        operation,
                        status = 200,
                        exampleName = request.headers["x-mock-response-example"]?.string()
                    )
                )
        }

    private fun getResponseContentForStatus(
        operation: Operation, status: Int, exampleName: String? = null
    ): String {

        val responsesForStatus: ApiResponse = operation.responses[status.toString()]
            ?: error("The OpenAPI Spec contains no responses for this operation")
        val jsonResponses: MediaType = responsesForStatus.content["application/json"]
            ?: error("The OpenAPI Spec contains no JSON responses for this operation")

        return if (exampleName != null)
            jsonResponses.examples[exampleName]?.value.toString()
        else
            (getExampleFromSchema(jsonResponses) ?: getExampleFromMediaType(jsonResponses))
                ?.toString()
                ?: error("The OpenAPI Spec contains no response examples for this operation")
    }

    private fun getExampleFromSchema(mediaType: MediaType) =
        mediaType.schema?.example

    private fun getExampleFromMediaType(mediaType: MediaType): Any? =
        if (mediaType.example != null) mediaType.example
        else mediaType.examples?.toList()?.get(0)?.second?.value

    private fun verifyAuth(operation: Operation, call: HttpContext): HttpContext? {
        if (operation.security == null || operation.security.size == 0
            || containsEmptySecurityRequirement(operation)) return null

        // Any one of the security mechanisms need to be satisfied
        return if (!operation.security.any { securityRequirement ->
            verifySecurityRequirement(securityRequirement, call)
        }) {
            call.send(status = UNAUTHORIZED_401, body = getResponseContentForStatus(operation, 401))
        }
        else null
    }

    private fun containsEmptySecurityRequirement(operation: Operation): Boolean =
        operation.security.any { it.size == 0 }

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
                            body = getResponseContentForStatus(
                                operation,
                                status = 400,
                                exampleName = call.request.headers["x-mock-response-example"]?.string()
                            )
                        )
                    }
                }
                "query" -> {
                    if (!verifyQueryParam(parameter, call)) {
                        call.send(
                            status = BAD_REQUEST_400,
                            body = getResponseContentForStatus(
                                operation,
                                status = 400,
                                exampleName = call.request.headers["x-mock-response-example"]?.string()
                            )
                        )
                    }
                }
                "header" -> {
                    if (!verifyHeaderParam(parameter, call)) {
                        call.send(
                            status = BAD_REQUEST_400,
                            body = getResponseContentForStatus(
                                operation,
                                status = 400,
                                exampleName = call.request.headers["x-mock-response-example"]?.string()
                            )
                        )
                    }
                }
                "cookie" -> {
                    if (!verifyCookieParam(parameter, call)) {
                        call.send(
                            status = BAD_REQUEST_400,
                            body = getResponseContentForStatus(
                                operation,
                                status = 400,
                                exampleName = call.request.headers["x-mock-response-example"]?.string()
                            )
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
                    body = getResponseContentForStatus(operation, 400)
                )
            }
        }
        return null
    }
}
