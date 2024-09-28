package com.hexagontk.http.server.servlet

import com.hexagontk.core.info
import com.hexagontk.core.loggerOf
import com.hexagontk.core.media.TEXT_PLAIN
import com.hexagontk.core.toText
import com.hexagontk.http.handlers.bodyToBytes
import com.hexagontk.http.handlers.HttpHandler
import com.hexagontk.http.model.HttpResponse
import com.hexagontk.http.model.HttpResponsePort
import jakarta.servlet.*
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpFilter
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.lang.System.Logger

class ServletFilter(pathHandler: HttpHandler) : HttpFilter() {

    private companion object {
        val logger: Logger = loggerOf(ServletFilter::class)
    }

    private val handlers: Map<String, HttpHandler> =
        pathHandler.byMethod().mapKeys { it.key.toString() }

    override fun init(filterConfig: FilterConfig) {
        val filterName = filterConfig.filterName
        val parameterNames = filterConfig.initParameterNames.toList().joinToString(", ") {
            "$it = ${filterConfig.getInitParameter(it)}"
        }
        logger.info {
            """'$filterName' Servlet filter initialized.
              |  * Context path: ${filterConfig.servletContext.contextPath}
              |  * Parameters: $parameterNames
            """.trimMargin()
        }
    }

    override fun destroy() {
        logger.info { "Servlet filter destroyed" }
    }

    override fun doFilter(
        request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        doFilter(request, response)
    }

    private fun doFilter(request: HttpServletRequest, response: HttpServletResponse) {

        val requestAdapter = ServletRequestAdapterSync(request)
        val handlerResponse = handlers[request.method]
            ?.process(requestAdapter)
            ?.response
            ?: HttpResponse()

        try {
            responseToServlet(requestAdapter.protocol.secure, handlerResponse, response)
            response.outputStream.write(bodyToBytes(handlerResponse.body))
        }
        catch (e: Exception) {
            response.addHeader("content-type", TEXT_PLAIN.fullType)
            response.status = 500
            response.outputStream.write(e.toText().toByteArray())
        }
        finally {
            response.outputStream.flush()
        }
    }

    private fun responseToServlet(
        secureRequest: Boolean,
        response: HttpResponsePort,
        servletResponse: HttpServletResponse
    ) {
        response.headers.values.forEach { (k, v) ->
            v.forEach { servletResponse.addHeader(k, it.toString()) }
        }

        response.cookies
            .filter { if (secureRequest) true else !it.secure }
            .forEach {
                val cookie = Cookie(it.name, it.value).apply {
                    maxAge = it.maxAge.toInt()
                    secure = it.secure
                    path = it.path
                    isHttpOnly = it.httpOnly
                    it.domain?.let { d -> domain = d }
                }
                servletResponse.addCookie(cookie)
            }

        response.contentType?.let { servletResponse.addHeader("content-type", it.text) }
        servletResponse.status = response.status.code
    }
}
