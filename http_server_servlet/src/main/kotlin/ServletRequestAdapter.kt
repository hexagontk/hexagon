package com.hexagonkt.http.server.servlet

import com.hexagonkt.core.MultiMap
import com.hexagonkt.http.model.*
import com.hexagonkt.http.parseContentType
import com.hexagonkt.http.parseQueryString
import com.hexagonkt.http.server.model.HttpServerRequestPort
import jakarta.servlet.MultipartConfigElement
import java.security.cert.X509Certificate
import jakarta.servlet.http.HttpServletRequest

internal class ServletRequestAdapter(req: HttpServletRequest) : HttpServerRequestPort {

    private companion object {
        const val CERTIFICATE_ATTRIBUTE = "jakarta.servlet.request.X509Certificate"
    }

    private val parameters: Map<String, List<String>> by lazy {
        req.parameterMap.map { it.key as String to it.value.toList() }.toMap()
    }

    @Suppress("UNCHECKED_CAST")
    override val certificateChain: List<X509Certificate> by lazy {
        (req.getAttribute(CERTIFICATE_ATTRIBUTE) as? Array<X509Certificate>)
            ?.toList()
            ?: emptyList()
    }

    override val accept: List<ContentType> by lazy {
        req.getHeaders("accept")?.toList()?.map { parseContentType(it) } ?: emptyList()
    }

    override val contentLength: Long by lazy { req.contentLength.toLong() }

    override val queryParameters: MultiMap<String, String> by lazy {
        parseQueryString(req.queryString ?: "")
    }

    override val method: HttpMethod by lazy {
        HttpMethod.valueOf(req.method)
    }

    override val protocol: HttpProtocol by lazy { HttpProtocol.valueOf(req.scheme.uppercase()) }
    override val host: String by lazy { req.remoteHost }
    override val port: Int by lazy { req.serverPort }
    override val path: String by lazy { req.servletPath.ifEmpty { req.pathInfo } }

    override val parts: List<HttpPartPort> by lazy {
        req.setAttribute("org.eclipse.jetty.multipartConfig", multipartConfig)
        req.parts.map { ServletPartAdapter(it) }
    }

    override val formParameters: MultiMap<String, String> by lazy {
        req.setAttribute("org.eclipse.jetty.multipartConfig", multipartConfig)
        MultiMap(parameters.filter { it.key !in queryParameters.keys })
    }

    private val multipartConfig: MultipartConfigElement by lazy { MultipartConfigElement("/tmp") }

    override val cookies: List<HttpCookie> by lazy {
        req.cookies
            ?.map { HttpCookie(it.name, it.value, it.maxAge.toLong(), it.secure) }
            ?: emptyList()
    }

    override val body: Any by lazy {
        req.inputStream.readAllBytes()
    }

    override val headers: MultiMap<String, String> by lazy {
        MultiMap(
            req.headerNames
                .toList()
                .map { it.lowercase() }
                .associateWith { req.getHeaders(it).toList() }
        )
    }

    override val contentType: ContentType? by lazy {
        req.contentType?.let { parseContentType(it) }
    }
}
