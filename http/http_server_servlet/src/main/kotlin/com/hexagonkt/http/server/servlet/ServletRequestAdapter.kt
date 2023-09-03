package com.hexagonkt.http.server.servlet

import com.hexagonkt.http.model.*
import com.hexagonkt.http.parseContentType
import com.hexagonkt.http.parseQueryString
import java.security.cert.X509Certificate
import jakarta.servlet.http.HttpServletRequest

internal abstract class ServletRequestAdapter(req: HttpServletRequest) : HttpRequestPort {

    private companion object {
        const val CERTIFICATE_ATTRIBUTE = "jakarta.servlet.request.X509Certificate"
    }

    @Suppress("UNCHECKED_CAST")
    override val certificateChain: List<X509Certificate> by lazy {
        (req.getAttribute(CERTIFICATE_ATTRIBUTE) as? Array<X509Certificate>)
            ?.toList()
            ?: emptyList()
    }

    override val accept: List<ContentType> by lazy {
        req.getHeaders("accept")
            ?.toList()
            ?.flatMap { it.split(",") }
            ?.map { parseContentType(it) }
            ?: emptyList()
    }

    override val contentLength: Long by lazy { req.contentLength.toLong() }

    override val queryParameters: QueryParameters by lazy {
        parseQueryString(req.queryString ?: "")
    }

    override val method: HttpMethod by lazy {
        HttpMethod.valueOf(req.method)
    }

    override val protocol: HttpProtocol by lazy { HttpProtocol.valueOf(req.scheme.uppercase()) }
    override val host: String by lazy { req.remoteHost }
    override val port: Int by lazy { req.serverPort }
    override val path: String by lazy { req.servletPath.ifEmpty { req.pathInfo } }
    override val authorization: Authorization? by lazy { authorization() }

    override val cookies: List<Cookie> by lazy {
        req.cookies
            ?.map {
                Cookie(
                    it.name,
                    it.value,
                    it.maxAge.toLong(),
                    it.secure,
                    it.path ?: "/",
                    false, //it.isHttpOnly, // TODO Fix this issue
                    it.domain,
                )
            }
            ?: emptyList()
    }

    override val headers: Headers by lazy {
        Headers(
            req.headerNames
                .toList()
                .map { it.lowercase() }
                .map { Header(it, req.getHeaders(it).toList()) }
        )
    }

    override val contentType: ContentType? by lazy {
        req.contentType?.let { parseContentType(it) }
    }
}
