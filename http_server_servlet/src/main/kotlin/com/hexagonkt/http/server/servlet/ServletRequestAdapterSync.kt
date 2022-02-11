package com.hexagonkt.http.server.servlet

import com.hexagonkt.core.MultiMap
import com.hexagonkt.http.model.*
import jakarta.servlet.MultipartConfigElement
import jakarta.servlet.http.HttpServletRequest

internal class ServletRequestAdapterSync(req: HttpServletRequest) : ServletRequestAdapter(req) {

    private val parameters: Map<String, List<String>> by lazy {
        req.parameterMap.map { it.key as String to it.value.toList() }.toMap()
    }

    override val parts: List<HttpPartPort> by lazy {
        req.setAttribute("org.eclipse.jetty.multipartConfig", multipartConfig)
        req.parts.map { ServletPartAdapter(it) }
    }

    override val formParameters: MultiMap<String, String> by lazy {
        req.setAttribute("org.eclipse.jetty.multipartConfig", multipartConfig)
        MultiMap(parameters.filter { it.key !in queryParameters.keys })
    }

    private val multipartConfig: MultipartConfigElement by lazy { MultipartConfigElement("/tmp") }

    override val body: Any by lazy {
        req.inputStream.readAllBytes()
    }
}
