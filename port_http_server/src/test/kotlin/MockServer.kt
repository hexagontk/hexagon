package com.hexagonkt.http.server

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.parser.OpenAPIV3Parser

class MockServer(pathToSpec: String, port: Int = 0) {

    val server: Server by lazy { createServer() }

    private val serverSettings = ServerSettings(bindPort = port)

    private val openAPIParser = OpenAPIV3Parser()
    private val openAPISpec: OpenAPI = openAPIParser.read(pathToSpec)
        ?: throw IllegalArgumentException("OpenAPI Spec could not be read. Please check the path to the file and verify it is correctly formatted")

    private fun createServer() = Server(settings = serverSettings) {
        openAPISpec.paths.forEach { path: String, pathItem: PathItem ->
            pathItem.get?.let { getOperation ->
                this.get(path = path) {
                    handleRequest(getOperation, call = this)
                }
            }
            pathItem.head?.let { headOperation ->
                this.head(path = path) {
                    handleRequest(headOperation, call = this)
                }
            }
            pathItem.post?.let { postOperation ->
                this.post(path = path) {
                    handleRequest(postOperation, call = this)
                }
            }
            pathItem.put?.let { putOperation ->
                this.put(path = path) {
                    handleRequest(putOperation, call = this)
                }
            }
            pathItem.delete?.let { deleteOperation ->
                this.delete(path = path) {
                    handleRequest(deleteOperation, call = this)
                }
            }
            pathItem.trace?.let { traceOperation ->
                this.trace(path = path) {
                    handleRequest(traceOperation, call = this)
                }
            }
            pathItem.options?.let { optionsOperation ->
                this.options(path = path) {
                    handleRequest(optionsOperation, call = this)
                }
            }
            pathItem.patch?.let { patchOperation ->
                this.patch(path = path) {
                    handleRequest(patchOperation, call = this)
                }
            }
        }
    }

    private fun handleRequest(operation: Operation, call: Call) {
        verifyParams(operation, call)
        verifyBody(operation, call)
        call.ok(content = getResponseContentForStatus(operation, status = 200))
    }

    private fun getResponseContentForStatus(operation: Operation, status: Int): String {
        val responsesForStatus: ApiResponse = operation.responses[status.toString()] ?: throw IllegalArgumentException("The OpenAPI Spec contains no responses for this operation")
        val jsonResponses: MediaType = responsesForStatus.content["application/json"] ?: throw IllegalArgumentException("The OpenAPI Spec contains no JSON responses for this operation")
        val exampleResponse: Any? = getExampleFromSchema(jsonResponses) ?: getExampleFromMediaType(jsonResponses)
        return exampleResponse?.toString() ?: ""
    }

    private fun getExampleFromSchema(mediaType: MediaType) = mediaType.schema?.example

    private fun getExampleFromMediaType(mediaType: MediaType): Any? {
        return if (mediaType.example != null) {
            mediaType.example
        } else {
            mediaType.examples?.toList()?.get(0)?.second?.value
        }
    }

    private fun verifyParams(operation: Operation, call: Call) {
        operation.parameters?.forEach { parameter ->
            when (parameter.`in`) {
                "path" -> {
                    if (!verifyPathParam(parameter, call)) {
                        call.halt(
                            code = 400,
                            content = getResponseContentForStatus(operation, status = 400)
                        )
                    }
                }
                "query" -> {
                    if (!verifyQueryParam(parameter, call)) {
                        call.halt(
                            code = 400,
                            content = getResponseContentForStatus(operation, status = 400)
                        )
                    }
                }
                "header" -> {
                    if (!verifyHeaderParam(parameter, call)) {
                        call.halt(
                            code = 400,
                            content = getResponseContentForStatus(operation, status = 400)
                        )
                    }
                }
                "cookie" -> {
                    if (!verifyCookieParam(parameter, call)) {
                        call.halt(
                            code = 400,
                            content = getResponseContentForStatus(operation, status = 400)
                        )
                    }
                }
            }
        }
    }

    /**
     * Verifies a single path parameter. Returns false if the following conditions are fulfilled:
     * 1. Parameter is required as per the OpenAPI Spec
     * 2. No value is passed for the parameter in the Request
     * 3. The value passed is not in the list of valid values as per the OpenAPI Spec
     * Returns true in all other cases.
     */
    private fun verifyPathParam(parameter: Parameter, call: Call): Boolean {
        if (call.request.pathParameters[parameter.name].isNullOrBlank()) return false
        parameter.schema.enum?.let {
            if (call.request.pathParameters[parameter.name] !in it) return false
        }
        return true
    }

    /**
     * Verifies a single query parameter. Returns false if the following conditions are fulfilled:
     * 1. Parameter is required as per the OpenAPI Spec
     * 2. No value is passed for the parameter in the Request
     * 3. The value passed is not in the list of valid values as per the OpenAPI Spec
     * Returns true in all other cases.
     */
    private fun verifyQueryParam(parameter: Parameter, call: Call): Boolean {
        if (call.request.queryParameters[parameter.name].isNullOrBlank()) {
            return !parameter.required
        }
        parameter.schema.enum?.let {
            if (call.request.queryParameters[parameter.name] !in it) return false
        }
        return true
    }

    /**
     * Verifies a single header parameter. Returns false if the following conditions are fulfilled:
     * 1. Parameter is required as per the OpenAPI Spec
     * 2. No value is passed for the parameter in the Request
     * 3. The value passed is not in the list of valid values as per the OpenAPI Spec
     * Returns true in all other cases.
     */
    private fun verifyHeaderParam(parameter: Parameter, call: Call): Boolean {
        if (call.request.headers[parameter.name].isNullOrBlank()) {
            return !parameter.required
        }
        parameter.schema.enum?.let {
            if (call.request.headers[parameter.name] !in it) return false
        }
        return true
    }

    /**
     * Verifies a single cookie parameter. Returns false if the following conditions are fulfilled:
     * 1. Parameter is required as per the OpenAPI Spec
     * 2. No value is passed for the parameter in the Request
     * 3. The value passed is not in the list of valid values as per the OpenAPI Spec
     * Returns true in all other cases.
     */
    private fun verifyCookieParam(parameter: Parameter, call: Call): Boolean {
        if (call.request.cookies[parameter.name] == null) {
            return !parameter.required
        }
        parameter.schema.enum?.let {
            if (call.request.cookies[parameter.name]?.value !in it) return false
        }
        return true
    }

    private fun verifyBody(operation: Operation, call: Call) {
        operation.requestBody?.let { requestBody ->
            if (requestBody.required && call.request.body.isBlank()) {
                call.halt(
                    code = 400,
                    content = getResponseContentForStatus(operation, 400)
                )
            }
        }
    }
}
