package com.hexagontk.serverless.http.google

import com.google.cloud.functions.HttpFunction
import com.google.cloud.functions.HttpRequest
import com.google.cloud.functions.HttpResponse
import com.hexagontk.http.handlers.HttpContext
import com.hexagontk.http.handlers.HttpHandler
import com.hexagontk.http.model.*
import com.hexagontk.http.parseContentType
import java.net.URI

class GoogleServerlessHttpAdapter(private val handler: HttpHandler): HttpFunction {

    override fun service(request: HttpRequest, response: HttpResponse) {
        val handlerRequest = createRequest(request)
        val handlerContext = handler.process(handlerRequest)
        writeResponse(response, handlerContext)
    }

    private fun createRequest(request: HttpRequest): com.hexagontk.http.model.HttpRequest {
        val uri = URI(request.uri)
        val qp = request.queryParameters?.map { (k, v) -> QueryParameter(k, v) } ?: emptyList()
        val h = request.headers?.map { (k, v) -> Header(k, v) } ?: emptyList()

        if (request.contentType.orElse("").contains("multipart")) {
            request.parts.map { (k, v) ->
                HttpPart(
                    name = k,
                    body = v.inputStream.readAllBytes(),
                    headers = Headers(),
                    contentType = v.contentType?.map { parseContentType(it) }?.orElse(null),
                    size = v.contentLength,
                    submittedFileName = v.fileName.orElse(null),

                    /*
                    name: String,
                    body: Any,
                    headers: Headers = Headers(),
                    contentType: ContentType? = null,
                    size: Long = -1L,
                    submittedFileName: String? = null
                     */
                )
            }
        }

        return HttpRequest(
            method = HttpMethod.valueOf(request.method),
            protocol = HttpProtocol.valueOf(uri.scheme.uppercase()),
            host = uri.host,
            port = uri.port,
            path = request.path,
            queryParameters = QueryParameters(qp),
            headers = Headers(h),
            body = request.inputStream.readAllBytes() ?: ByteArray(0),
//            parts = pa,
//            formParameters = fp,
            contentType = request.contentType.map { parseContentType(it) }.orElse(null),
//            accept = ac,
            contentLength = request.contentLength,
//            authorization = au,
        )
    }

    private fun writeResponse(response: HttpResponse, context: HttpContext) {
        val handlerResponse = context.response

        handlerResponse.contentType?.text?.let(response::setContentType)
        handlerResponse.headers.forEach { (k, v) -> response.headers[k] = v.strings() }
        response.setStatusCode(handlerResponse.status.code)
        response.writer.write(handlerResponse.bodyString())
    }
}
