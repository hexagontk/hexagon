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

    /**
     * Creates and returns a Server object representing the mock server. The provided OpenAPI spec
     * is parsed and each path within it is registered in the router of the mock server.
     *
     * If an explicit port number was provided in the constructor, the
     * mock server listens at the specified port, else a dynamic port number is assigned.
     */
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

    /**
     * Handles a request by verifying the parameters and body against the OpenAPI spec file and
     * returning the appropriate response.
     */
    private fun handleRequest(operation: Operation, call: Call) {
        verifyParams(operation, call)
        verifyBody(operation, call)
        call.ok(content = getResponseContentForStatus(
            operation,
            status = 200,
            exampleName = call.request.headers["X-Mock-Response-Example"]
        ))
    }

    /**
     * Gets the example response for a particular status code (200, 400, etc). The priority order
     * for fetching examples is as follows:
     *
     * 1. If a certain example name is required - as specified in the X-Mock-Response-Example
     * header, then that example is immediately fetched and returned. If the X-Mock-Response header
     * is missing, then the following steps are followed to get the example.
     * 2. First, it tries to fetch an example from the schema key within the mediatype.
     * 3. If no example is found here, it then attempts to fetch the example from the mediatype
     * object.
     * 4. If still no example is found, it simply raises an exception.
     */
    private fun getResponseContentForStatus(operation: Operation, status: Int, exampleName: String? = null): String {
        val responsesForStatus: ApiResponse = operation.responses[status.toString()]
            ?: throw IllegalArgumentException("The OpenAPI Spec contains no responses for this operation")
        val jsonResponses: MediaType = responsesForStatus.content["application/json"]
            ?: throw IllegalArgumentException("The OpenAPI Spec contains no JSON responses for this operation")

        return if (exampleName != null) {
            jsonResponses.examples[exampleName]?.value.toString()
        } else {
            val exampleResponse: Any? =
                getExampleFromSchema(jsonResponses) ?: getExampleFromMediaType(jsonResponses)
            exampleResponse?.toString()
                ?: throw IllegalArgumentException("The OpenAPI Spec contains no response examples for this operation")
        }
    }

    /**
     * Fetches the example response content from the mediatype schema, or null if not present.
     */
    private fun getExampleFromSchema(mediaType: MediaType) = mediaType.schema?.example

    /**
     * Fetches the example response content from the mediatype.
     *
     * 1. If an `example` (singular) key is present, the response content is fetched from there.
     * 2. Else, it attempts to get the examples from the `examples` (plural) key and returns the
     * first one.
     * 3. If this is also not present, it simply returns null.
     */
    private fun getExampleFromMediaType(mediaType: MediaType): Any? {
        return if (mediaType.example != null) {
            mediaType.example
        } else {
            mediaType.examples?.toList()?.get(0)?.second?.value
        }
    }

    /**
     * Verifies all the parameters specified for the operation. If a parameter is optional, it is
     * verified if present in the request, else it is ignored. If a certain parameter is found to
     * fail verification (i.e it may be absent or its value may be incorrect) a 400 response is
     * returned with the body being an example present in the OpenAPI spec file.
     */
    private fun verifyParams(operation: Operation, call: Call) {
        operation.parameters?.forEach { parameter ->
            when (parameter.`in`) {
                "path" -> {
                    if (!verifyPathParam(parameter, call)) {
                        call.halt(
                            code = 400,
                            content = getResponseContentForStatus(
                                operation,
                                status = 400,
                                exampleName = call.request.headers["X-Mock-Response-Example"]
                            )
                        )
                    }
                }
                "query" -> {
                    if (!verifyQueryParam(parameter, call)) {
                        call.halt(
                            code = 400,
                            content = getResponseContentForStatus(
                                operation,
                                status = 400,
                                exampleName = call.request.headers["X-Mock-Response-Example"]
                            )
                        )
                    }
                }
                "header" -> {
                    if (!verifyHeaderParam(parameter, call)) {
                        call.halt(
                            code = 400,
                            content = getResponseContentForStatus(
                                operation,
                                status = 400,
                                exampleName = call.request.headers["X-Mock-Response-Example"]
                            )
                        )
                    }
                }
                "cookie" -> {
                    if (!verifyCookieParam(parameter, call)) {
                        call.halt(
                            code = 400,
                            content = getResponseContentForStatus(
                                operation,
                                status = 400,
                                exampleName = call.request.headers["X-Mock-Response-Example"]
                            )
                        )
                    }
                }
            }
        }
    }

    /**
     * Verifies a single path parameter. According to the OpenAPI specification, path parameters
     * cannot be optional. Returns false if the following conditions are fulfilled:
     * 1. No value is passed for the parameter in the Request
     * 2. The value passed is not in the list of valid values as per the OpenAPI Spec
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
     * 1. Parameter is required as per the OpenAPI Spec but not present in the request
     * 2. The value passed is not in the list of valid values as per the OpenAPI Spec
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
     * 1. Parameter is required as per the OpenAPI Spec but not present in the request
     * 2. The value passed is not in the list of valid values as per the OpenAPI Spec
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
     * 1. Parameter is required as per the OpenAPI Spec but not present in the request
     * 2. The value passed is not in the list of valid values as per the OpenAPI Spec
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

    /**
     * Verifies the request body. At present, the only verification done is whether the body is
     * blank or not. The actual contents of the body are not verified.
     */
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
