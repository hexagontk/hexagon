package com.hexagonkt.http.server

import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.parser.OpenAPIV3Parser

class MockServer(pathToSpec: String) {

    val server: Server by lazy { createServer() }

    private val openAPIParser = OpenAPIV3Parser()
    private val openAPISpec = openAPIParser.read(pathToSpec) ?: throw IllegalArgumentException()

    private fun createServer() = Server {
        openAPISpec.paths.forEach { path: String, pathItem: PathItem ->
            pathItem.get?.let { getOperation ->
                this.get(path = path) {
                    handleRequest(getOperation, call = this)
                }
            }
            pathItem.head?.let {
                this.head(path = path) {
                    handleRequest(it, call = this)
                }
            }
            pathItem.post?.let {
                this.post(path = path) {
                    handleRequest(it, this)
                }
            }
            pathItem.put?.let {
                this.put(path = path) {
                    handleRequest(it, this)
                }
            }
            pathItem.delete?.let {
                this.delete(path = path) {
                    handleRequest(it, this)
                }
            }
            pathItem.trace?.let {
                this.trace(path = path) {
                    handleRequest(it, this)
                }
            }
            pathItem.options?.let {
                this.options(path = path) {
                    handleRequest(it, this)
                }
            }
            pathItem.patch?.let {
                this.patch(path = path) {
                    handleRequest(it, this)
                }
            }
        }
    }

    private fun handleRequest(operation: Operation, call: Call) {
        verifyParams(operation, call)
        call.ok(content = getResponseContentForStatus(operation, 200))
    }

    private fun getResponseContentForStatus(operation: Operation, status: Int): String {
        val responseForStatus = operation.responses[status.toString()] ?: throw Exception()
        val jsonResponses = responseForStatus.content["application/json"] ?: throw Exception()
        return jsonResponses.example.toString()
    }

    private fun verifyParams(operation: Operation, call: Call) {
        operation.parameters.forEach { parameter ->
            when (parameter.`in`) {
                "path" -> {
                    if (!verifyPathParam(parameter, call)) {
                        call.halt(
                            code = 400,
                            content = operation.responses["400"]?.description ?: ""
                        )
                    }
                }
                "query" -> {
                    if (!verifyQueryParam(parameter, call)) {
                        call.halt(
                            code = 400,
                            content = operation.responses["400"]?.description ?: ""
                        )
                    }
                }
                "header" -> {
                    if (!verifyHeaderParam(parameter, call)) {
                        call.halt(
                            code = 400,
                            content = operation.responses["400"]?.description ?: ""
                        )
                    }
                }
                "cookie" -> {
                    if (!verifyCookieParam(parameter, call)) {
                        call.halt(
                            code = 400,
                            content = operation.responses["400"]?.description ?: ""
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
     * Returns true in all other cases.
     */
    private fun verifyPathParam(parameter: Parameter, call: Call): Boolean =
        !(parameter.required && call.request.pathParameters[parameter.name] == null)

    /**
     * Verifies a single query parameter. Returns false if the following conditions are fulfilled:
     * 1. Parameter is required as per the OpenAPI Spec
     * 2. No value is passed for the parameter in the Request
     * Returns true in all other cases.
     */
    private fun verifyQueryParam(parameter: Parameter, call: Call): Boolean =
        !(parameter.required && call.request.queryParameters[parameter.name] == null)

    /**
     * Verifies a single header parameter. Returns false if the following conditions are fulfilled:
     * 1. Parameter is required as per the OpenAPI Spec
     * 2. No value is passed for the parameter in the Request
     * Returns true in all other cases.
     */
    private fun verifyHeaderParam(parameter: Parameter, call: Call): Boolean =
        !(parameter.required && call.request.headers[parameter.name] == null)

    /**
     * Verifies a single cookie parameter. Returns false if the following conditions are fulfilled:
     * 1. Parameter is required as per the OpenAPI Spec
     * 2. No value is passed for the parameter in the Request
     * Returns true in all other cases.
     */
    private fun verifyCookieParam(parameter: Parameter, call: Call): Boolean =
        !(parameter.required && call.request.cookies[parameter.name] == null)

}
