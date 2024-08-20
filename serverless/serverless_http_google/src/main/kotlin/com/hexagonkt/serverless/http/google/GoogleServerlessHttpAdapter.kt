package com.hexagonkt.serverless.http.google

import com.google.cloud.functions.HttpFunction
import com.google.cloud.functions.HttpRequest
import com.google.cloud.functions.HttpResponse
import com.hexagonkt.http.handlers.HttpContext
import com.hexagonkt.http.handlers.HttpHandler

class GoogleServerlessHttpAdapter(val handler: HttpHandler): HttpFunction {

    override fun service(request: HttpRequest, response: HttpResponse) {
        val handlerRequest = createRequest(request)
        val handlerContext = handler.process(handlerRequest)
        writeResponse(response, handlerContext)
    }

    private fun createRequest(request: HttpRequest): com.hexagonkt.http.model.HttpRequest {
        TODO("Not yet implemented")
    }

    private fun writeResponse(response: HttpResponse, context: HttpContext) {
        val handlerResponse = context.response

        handlerResponse.contentType?.text?.let(response::setContentType)

        response.headers // TODO
        response.setStatusCode(handlerResponse.status.code)
        response.writer.write(handlerResponse.bodyString())
    }
}
