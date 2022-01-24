package com.hexagonkt.http.server.servlet

import com.hexagonkt.core.toText
import com.hexagonkt.core.logging.Logger
import com.hexagonkt.core.media.TextMedia
import com.hexagonkt.http.bodyToBytes
import com.hexagonkt.http.server.HttpServerFeature.ASYNC
import com.hexagonkt.http.server.HttpServerSettings
import com.hexagonkt.http.server.handlers.PathHandler
import com.hexagonkt.http.server.model.HttpServerResponse
import jakarta.servlet.*
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpFilter
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.*

class ServletFilter(
    pathHandler: PathHandler,
    private val serverSettings: HttpServerSettings
) : HttpFilter() {

    private val logger: Logger = Logger(ServletFilter::class)
    private val async: Boolean = serverSettings.features.contains(ASYNC)

    private val handlers: Map<String, PathHandler> =
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

        if (async) doFilterAsync(request, response)
        else doFilter(request, response)
    }

    private fun doFilterAsync(request: HttpServletRequest, response: HttpServletResponse) {
        val asyncContext = request.startAsync()

        CoroutineScope(Dispatchers.Default).launch {
            val handlerResponse = handlers[request.method]
                ?.process(ServletRequestAdapter(request))
                ?: HttpServerResponse()

            responseToServlet(handlerResponse, response)
            asyncContext.complete()
        }
    }

    // TODO Don't use 'runBlocking' (in servlet async mode)
    private fun doFilter(request: HttpServletRequest, response: HttpServletResponse) = runBlocking {

        val handlerResponse = handlers[request.method]
            ?.process(ServletRequestAdapter(request))
            ?: HttpServerResponse()

        responseToServlet(handlerResponse, response)
    }

    private fun responseToServlet(
        response: HttpServerResponse, servletResponse: HttpServletResponse) {

        try {
            response.headers.allValues.forEach { (k, v) ->
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
            // TODO Handle different types: deferred values, strings, ints... flows
            servletResponse.outputStream.write(bodyToBytes(response.body))
        }
        catch (e: Exception) {
            servletResponse.addHeader("content-type", TextMedia.PLAIN.fullType)
            servletResponse.status = 500
            servletResponse.outputStream.write(e.toText().toByteArray())
        }
        finally {
            servletResponse.outputStream.flush()
        }
    }
}
