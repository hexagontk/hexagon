package com.hexagonkt.serverless.http.google

import com.google.cloud.functions.HttpFunction
import com.google.cloud.functions.HttpRequest
import com.google.cloud.functions.HttpResponse
import com.hexagonkt.http.handlers.HttpHandler

class GoogleServerlessHttpAdapter(val handler: HttpHandler): HttpFunction {

    override fun service(request: HttpRequest, response: HttpResponse) {
        // Transform request
        // Call handler
        // Transform response
        response.writer.write("Hello World!")
    }
}
