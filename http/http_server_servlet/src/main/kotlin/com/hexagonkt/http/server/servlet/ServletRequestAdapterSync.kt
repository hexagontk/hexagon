package com.hexagonkt.http.server.servlet

import com.hexagonkt.http.model.*
import com.hexagonkt.http.parseContentType
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.Part
import java.security.cert.X509Certificate
import kotlin.UnsupportedOperationException

internal class ServletRequestAdapterSync(req: HttpServletRequest) : ServletRequestAdapter(req) {

    private val parameters: Map<String, List<String>> by lazy {
        req.parameterMap.map { it.key as String to it.value.toList() }.toMap()
    }

    override val parts: List<HttpPart> by lazy {
        req.parts.map { servletPartAdapter(it) }
    }

    override val formParameters: FormParameters by lazy {
        val fields = parameters
            .filter { it.key !in queryParameters.httpFields.keys }
            .map { (k, v) -> FormParameter(k, v) }

        FormParameters(fields)
    }

    override fun with(
        body: Any,
        headers: Headers,
        contentType: ContentType?,
        method: HttpMethod,
        protocol: HttpProtocol,
        host: String,
        port: Int,
        path: String,
        queryParameters: QueryParameters,
        parts: List<HttpPart>,
        formParameters: FormParameters,
        cookies: List<Cookie>,
        accept: List<ContentType>,
        authorization: Authorization?,
        certificateChain: List<X509Certificate>,
    ): HttpRequestPort =
        throw UnsupportedOperationException()

    override val body: Any by lazy {
        req.inputStream.readAllBytes()
    }

    private fun servletPartAdapter(part: Part) : HttpPart {
        val headerNames = part.headerNames.filterNotNull()
        return HttpPart(
            name = part.name,
            body = part.inputStream.readAllBytes(),
            headers = Headers(headerNames.map { Header(it, part.getHeaders(it).toList()) }),
            contentType = part.contentType?.let { parseContentType(it) },
            size = part.size,
            submittedFileName = part.submittedFileName,
        )
    }
}
