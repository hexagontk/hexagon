package com.hexagonkt.http.server.servlet

import com.hexagonkt.core.logging.Logger
import com.hexagonkt.core.media.TEXT_PLAIN
import com.hexagonkt.core.toText
import com.hexagonkt.http.bodyToBytes
import com.hexagonkt.http.server.HttpServerSettings
import com.hexagonkt.http.server.handlers.HttpHandler
import com.hexagonkt.http.server.model.HttpServerResponse
import jakarta.servlet.*
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpFilter
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

class ServletFilter(
    pathHandler: HttpHandler,
    private val serverSettings: HttpServerSettings
) : HttpFilter() {

    private val logger: Logger = Logger(ServletFilter::class)

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
              |  * Server settings: $serverSettings
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

        val handlerResponse = handlers[request.method]
            ?.process(ServletRequestAdapterSync(request))
            ?: HttpServerResponse()

        try {
            responseToServlet(handlerResponse, response)
            // TODO Handle different types: deferred values, strings, ints... flows
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
        response: HttpServerResponse, servletResponse: HttpServletResponse) {

        response.headers.values.forEach { (k, v) ->
            v.forEach { servletResponse.addHeader(k, it) }
        }

        response.cookies.forEach {
            val cookie = Cookie(it.name, it.value).apply {
                maxAge = it.maxAge.toInt()
                secure = it.secure
            }
            servletResponse.addCookie(cookie)
        }

        response.contentType?.let { servletResponse.addHeader("content-type", it.text) }
        servletResponse.status = response.status.code
    }
}
