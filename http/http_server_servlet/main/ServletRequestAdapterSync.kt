package com.hexagontk.http.server.servlet

import com.hexagontk.http.model.*
import com.hexagontk.http.parseContentType
import com.hexagontk.http.patterns.PathPattern
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.Part
import java.security.cert.X509Certificate
import kotlin.UnsupportedOperationException

internal class ServletRequestAdapterSync(req: HttpServletRequest) : ServletRequestAdapter(req) {

    private val parameters: List<Pair<String, String>> by lazy {
        req.parameterMap.flatMap { pm ->
            pm.value.map { pm.key as String to it }
        }
    }

    override val parts: List<HttpPart> by lazy {
        req.parts.map { servletPartAdapter(it) }
    }

    override val formParameters: Parameters by lazy {
        val fields = parameters
            .filter { it.first !in queryParameters.keys }
            .map { (k, v) -> Parameter(k, v) }

        Parameters(fields)
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
        queryParameters: Parameters,
        parts: List<HttpPart>,
        formParameters: Parameters,
        cookies: List<Cookie>,
        accept: List<ContentType>,
        authorization: Authorization?,
        certificateChain: List<X509Certificate>,
        pathPattern: PathPattern?,
        pathParameters: Map<String, Any>,
    ): HttpRequestPort =
        throw UnsupportedOperationException()

    override val body: Any by lazy {
        req.inputStream.readAllBytes()
    }

    override val pathPattern: PathPattern? = null

    override val pathParameters: Map<String, Any> = emptyMap()

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
