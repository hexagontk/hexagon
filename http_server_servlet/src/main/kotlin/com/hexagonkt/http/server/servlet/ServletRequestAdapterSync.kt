package com.hexagonkt.http.server.servlet

import com.hexagonkt.http.model.*
import com.hexagonkt.http.parseContentType
import jakarta.servlet.MultipartConfigElement
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.Part

internal class ServletRequestAdapterSync(req: HttpServletRequest) : ServletRequestAdapter(req) {

    private val parameters: Map<String, List<String>> by lazy {
        req.parameterMap.map { it.key as String to it.value.toList() }.toMap()
    }

    override val parts: List<HttpPart> by lazy {
        req.setAttribute("org.eclipse.jetty.multipartConfig", multipartConfig)
        req.parts.map { servletPartAdapter(it) }
    }

    override val formParameters: HttpFields<FormParameter> by lazy {
        req.setAttribute("org.eclipse.jetty.multipartConfig", multipartConfig)
        val fields = parameters
            .filter { it.key !in queryParameters.httpFields.keys }
            .map { (k, v) -> FormParameter(k, v) }

        HttpFields(fields)
    }

    private val multipartConfig: MultipartConfigElement by lazy { MultipartConfigElement("/tmp") }

    override val body: Any by lazy {
        req.inputStream.readAllBytes()
    }

    private fun servletPartAdapter(part: Part) : HttpPart {
        val headerNames = part.headerNames.filterNotNull()
        return HttpPart(
            name = part.name,
            body = part.inputStream.readAllBytes(),
            headers = HttpFields(headerNames.map { Header(it, part.getHeaders(it).toList()) }),
            contentType = part.contentType?.let { parseContentType(it) },
            size = part.size,
            submittedFileName = part.submittedFileName,
        )
    }
}
