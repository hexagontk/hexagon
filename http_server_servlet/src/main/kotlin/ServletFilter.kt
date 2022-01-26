package com.hexagonkt.http.server.servlet

import com.hexagonkt.core.logging.Logger
import com.hexagonkt.core.media.TextMedia
import com.hexagonkt.core.toText
import com.hexagonkt.http.bodyToBytes
import com.hexagonkt.http.server.HttpServerFeature.ASYNC
import com.hexagonkt.http.server.HttpServerSettings
import com.hexagonkt.http.server.handlers.PathHandler
import com.hexagonkt.http.server.model.HttpServerResponse
import jakarta.servlet.FilterChain
import jakarta.servlet.FilterConfig
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpFilter
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

            try {
                responseToServlet(handlerResponse, response)
            }
            catch (e: Exception) {
                response.addHeader("content-type", TextMedia.PLAIN.fullType)
                response.status = 500
                withContext(Dispatchers.IO) {
                    response.outputStream.write(e.toText().toByteArray())
                }
            }
            finally {
                asyncContext.complete()
            }
        }
    }

    // TODO Only works on requests without payloads
//    private fun doFilterAsync2(request: HttpServletRequest, response: HttpServletResponse) {
//        val asyncContext = request.startAsync()
//
//        request.inputStream.setReadListener(object : ReadListener {
//            override fun onDataAvailable() {
//                // TODO Reads the data, but is not loaded on the request
//                request.inputStream.readAllBytes()
//            }
//
//            override fun onAllDataRead() {
//                val handlerResponse = handlers[request.method]
//                    ?.process(ServletRequestAdapter(request))
//                    ?: HttpServerResponse()
//
//                response.outputStream.setWriteListener(object : WriteListener {
//                    override fun onWritePossible() {
//                        responseToServlet(handlerResponse, response)
//                        asyncContext.complete()
//                    }
//
//                    override fun onError(t: Throwable?) {
//                        logger.error(t)
//                        response.addHeader("content-type", TextMedia.PLAIN.fullType)
//                        response.status = 500
//                        if (t != null)
//                            response.outputStream.write(t.toText().toByteArray())
//                        asyncContext.complete()
//                    }
//                })
//            }
//
//            override fun onError(t: Throwable?) {
//                logger.error(t)
//                asyncContext.complete()
//            }
//        })
//    }

    private fun doFilter(request: HttpServletRequest, response: HttpServletResponse) {

        val handlerResponse = handlers[request.method]
            ?.process(ServletRequestAdapter(request))
            ?: HttpServerResponse()

        try {
            responseToServlet(handlerResponse, response)
        }
        catch (e: Exception) {
            response.addHeader("content-type", TextMedia.PLAIN.fullType)
            response.status = 500
            response.outputStream.write(e.toText().toByteArray())
        }
        finally {
            response.outputStream.flush()
        }
    }

    private fun responseToServlet(
        response: HttpServerResponse, servletResponse: HttpServletResponse) {

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
}
