package com.hexagontk.serverless.http.google

import com.hexagontk.http.handlers.bodyToBytes
import com.google.cloud.functions.HttpFunction
import com.google.cloud.functions.HttpRequest
import com.google.cloud.functions.HttpResponse
import com.hexagontk.core.media.TEXT_PLAIN
import com.hexagontk.core.toText
import com.hexagontk.http.handlers.HttpContext
import com.hexagontk.http.handlers.HttpHandler
import com.hexagontk.http.model.*
import com.hexagontk.http.parseContentType
import java.net.URI

class GoogleHttpFunction(private val handler: HttpHandler): HttpFunction {

    override fun service(request: HttpRequest, response: HttpResponse) {
        val handlerRequest = createRequest(request)
        val handlerContext = handler.process(handlerRequest)
        writeResponse(response, handlerContext)
    }

    private fun createRequest(request: HttpRequest): com.hexagontk.http.model.HttpRequest {
        val uri = URI(request.uri)
        val qp =
            request.queryParameters?.flatMap { (k, v) -> v.map { Parameter(k, it) } } ?: emptyList()
        val h = request.headers?.flatMap { (k, v) -> v.map { Header(k, it) } } ?: emptyList()
        val contentType = request.contentType.map { parseContentType(it) }.orElse(null)
        val accept = request.headers["accept"]?.map { parseContentType(it) } ?: emptyList()
        val authorization = request.headers["authorization"]
            ?.first()
            ?.split(" ", limit = 2)
            ?.let { Authorization(it.first(), it.last()) }

        // TODO Fix parts upload
        val parts = emptyList<HttpPart>()
//            if (request.contentType.orElse("").contains("multipart")) {
//                request.parts.map { (k, v) ->
//                    HttpPart(
//                        name = k,
//                        body = v.inputStream.readAllBytes(),
//                        headers = Headers(),
//                        contentType = v.contentType?.map { parseContentType(it) }?.orElse(null),
//                        size = v.contentLength,
//                        submittedFileName = v.fileName.orElse(null),
//                    )
//                }
//            }
//            else emptyList()

        return HttpRequest(
            method = HttpMethod.valueOf(request.method),
            protocol = HttpProtocol.valueOf(uri.scheme.uppercase()),
            host = uri.host,
            port = uri.port,
            path = request.path,
            queryParameters = Parameters(qp),
            headers = Headers(h),
            body = request.inputStream.readAllBytes() ?: ByteArray(0),
            parts = parts,
            contentType = contentType,
            accept = accept,
            contentLength = request.contentLength,
            authorization = authorization,
        )
    }

    private fun writeResponse(response: HttpResponse, context: HttpContext) {
        val handlerResponse = context.response

        handlerResponse.contentType?.text?.let(response::setContentType)
        handlerResponse.headers.forEach { response.appendHeader(it.name, it.text) }
        response.setStatusCode(handlerResponse.status)

        try {
            response.outputStream.write(bodyToBytes(handlerResponse.body))
        }
        catch (e: Exception) {
            response.setContentType(TEXT_PLAIN.fullType)
            response.setStatusCode(500)
            response.outputStream.write(bodyToBytes(e.toText()))
        }
    }
}
