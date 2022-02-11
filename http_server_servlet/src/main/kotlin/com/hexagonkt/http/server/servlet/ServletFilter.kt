package com.hexagonkt.http.server.servlet

import com.hexagonkt.core.logging.Logger
import com.hexagonkt.core.media.TextMedia
import com.hexagonkt.core.toText
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
import java.nio.ByteBuffer

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
        val inputStream = request.inputStream

        inputStream.setReadListener(object : ReadListener {
            val buffer = ByteArray(4_096)
            var bytes = ByteArray(0)

            override fun onDataAvailable() {
                do {
                    val length = inputStream.read(buffer)
                    if (length < 0) break
                    val size = bytes.size
                    bytes = bytes.copyOf(size + length)
                    buffer.copyInto(bytes, size, 0, length)
                }
                while(inputStream.isReady)
            }

            override fun onAllDataRead() {
                val handlerResponse = handlers[request.method]
                    ?.process(ServletRequestAdapterAsync(request, bytes))
                    ?: HttpServerResponse()

                responseToServlet(handlerResponse, response)

                val outputStream: ServletOutputStream = response.outputStream

                outputStream.setWriteListener(object : WriteListener {

                    val outBuffer: ByteBuffer =
                        try {
                            // TODO Handle different types: deferred values, strings, ints... flows
                            ByteBuffer.wrap(bodyToBytes(handlerResponse.body))
                        }
                        catch (e: Exception) {
                            onError(e)
                            throw e
                        }

                    override fun onWritePossible() {
                        while (outputStream.isReady) {
                            if (!outBuffer.hasRemaining()) {
                                asyncContext.complete()
                                return
                            }
                            val buffer = ByteArray(1) { outBuffer.get() }
                            outputStream.write(buffer)
                        }
                    }

                    override fun onError(t: Throwable) {
                        logger.error(t)
                        response.addHeader("content-type", TextMedia.PLAIN.fullType)
                        response.status = 500
                        outputStream.write(t.toText().toByteArray())
                        asyncContext.complete()
                    }
                })
            }

            override fun onError(t: Throwable?) {
                logger.error(t)
                asyncContext.complete()
            }
        })
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
    }
}
