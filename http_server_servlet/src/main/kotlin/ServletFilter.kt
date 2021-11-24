package com.hexagonkt.http.server.servlet

import com.hexagonkt.core.helpers.CodedException
import com.hexagonkt.http.ALL
import com.hexagonkt.http.Method
import com.hexagonkt.http.Path
import com.hexagonkt.http.Route
import com.hexagonkt.http.server.Call
import com.hexagonkt.http.server.ErrorCodeCallback
import com.hexagonkt.http.server.ExceptionCallback
import com.hexagonkt.http.server.FilterOrder
import com.hexagonkt.http.server.FilterOrder.AFTER
import com.hexagonkt.http.server.FilterOrder.BEFORE
import com.hexagonkt.http.server.Request
import com.hexagonkt.http.server.RequestHandler
import com.hexagonkt.http.server.RequestHandler.CodeHandler
import com.hexagonkt.http.server.RequestHandler.ExceptionHandler
import com.hexagonkt.http.server.RequestHandler.FileHandler
import com.hexagonkt.http.server.RequestHandler.FilterHandler
import com.hexagonkt.http.server.RequestHandler.ResourceHandler
import com.hexagonkt.http.server.RequestHandler.RouteHandler
import com.hexagonkt.http.server.Response
import com.hexagonkt.http.server.RouteCallback
import com.hexagonkt.http.server.ServerFeature
import com.hexagonkt.http.server.ServerFeature.SESSIONS
import com.hexagonkt.http.server.ServerSettings
import com.hexagonkt.http.server.Session
import com.hexagonkt.http.server.UnsupportedSessionAdapter
import com.hexagonkt.core.logging.Logger
import com.hexagonkt.serialization.SerializationManager.contentTypeOf
import java.io.File
import java.io.InputStream
import java.net.URL
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.MultipartConfigElement
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest as HttpRequest
import javax.servlet.http.HttpServletResponse as HttpResponse

class ServletFilter(router: List<RequestHandler>, serverSettings: ServerSettings) : Filter {

    /**
     * Exception used for stopping the execution. It is used only for flow control.
     */
    private class PassException: RuntimeException()

    private val log: Logger = Logger(this::class)

    private val features: Set<ServerFeature> = serverSettings.features
    private val notFoundHandler: ErrorCodeCallback = { send(404, "${request.url} not found") }
    private val baseExceptionHandler: ExceptionCallback =
        { send(500, "${it.javaClass.simpleName} (${it.message ?: "no details"})") }

    private val allHandlers = listOf(
        CodeHandler(Route(Path("/"), ALL), 404, notFoundHandler),
        ExceptionHandler(Route(Path("/"), ALL), Exception::class.java, baseExceptionHandler)
    ) + router

    private val routesByMethod: Map<Method, List<RouteHandler>> = router
        .asSequence()
        .map {
            when (it) {
                is ResourceHandler ->
                    RouteHandler(it.route, createResourceHandler(it.route, it.resource))
                is FileHandler ->
                    RouteHandler(it.route, createResourceHandler(it.route, it.file))
                else ->
                    it
            }
        }
        .filterIsInstance(RouteHandler::class.java)
        .groupBy { it.route.methods.first() }

    private val filtersByOrder: Map<FilterOrder, List<Pair<Route, RouteCallback>>> = router
        .asSequence()
        .filterIsInstance(FilterHandler::class.java)
        .groupBy { it.order }
        .mapValues { entry -> entry.value.map { it.route to it.callback } }

    private val beforeFilters = filtersByOrder[BEFORE] ?: listOf()
    private val afterFilters = filtersByOrder[AFTER] ?: listOf()

    private val codedErrors: Map<Int, ErrorCodeCallback> = allHandlers
        .asSequence()
        .filterIsInstance(CodeHandler::class.java)
        .map { it.code to it.callback }
        .toMap()

    private val exceptionErrors: Map<Class<out Exception>, ExceptionCallback> = allHandlers
        .asSequence()
        .filterIsInstance(ExceptionHandler::class.java)
        .map { it.exception to it.callback }
        .toMap()

    /**
     * TODO Take care of filters that throw exceptions
     */
    private fun filter(
        requestAdapter: RequestAdapter,
        call: Call,
        filters: List<Pair<Route, RouteCallback>>
    ) {
        filters
            .filter { it.first.path.matches(call.request.path) }
            .map {
                requestAdapter.actionPath = it.first.path
                call.(it.second)()
                log.trace { "Filter for path '${it.first.path}' executed" }
            }
    }

    private fun route(call: Call, requestAdapter: RequestAdapter): Boolean {
        val routes = routesByMethod[call.request.method]
        val methodRoutes =
            routes?.filter { it.route.path.matches(call.request.path) } ?: emptyList()

        try {
            for ((first, second) in methodRoutes) {
                try {
                    requestAdapter.actionPath = first.path
                    call.second()

                    log.trace { "Route for path '${requestAdapter.actionPath}' executed" }
                    return true
                }
                catch (e: PassException) {
                    log.trace { "Handler for path '${requestAdapter.actionPath}' passed" }
                    continue
                }
            }
            return false
        }
        catch(e: CodedException) {
            handleException(e, call)
            return true
        }
    }

    override fun init(filterConfig: FilterConfig) { /* Not implemented */ }

    override fun destroy() { /* Not implemented */ }

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {

        // TODO Temporary hack only valid for Jetty
        val multipartConfig = MultipartConfigElement("/tmp")
        request.setAttribute("org.eclipse.jetty.multipartConfig", multipartConfig)

        doFilter(request, response)
    }

    private fun doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse) {

        if (servletRequest !is HttpRequest || servletResponse !is HttpResponse)
            error("Invalid request/response parameters")

        val requestAdapter = RequestAdapter(servletRequest)
        val request = Request(requestAdapter)
        val response = Response(ResponseAdapter(servletRequest, servletResponse))
        val hasSessions = features.contains(SESSIONS)
        val sessionAdapter =
            if (hasSessions) SessionAdapter(servletRequest)
            else UnsupportedSessionAdapter
        val session = Session(sessionAdapter)
        val call = Call(request, response, session)
        var handled = false

        try {
            try {
                filter(requestAdapter, call, beforeFilters)
                handled = route(call, requestAdapter)
            }
            finally {
                filter(requestAdapter, call, afterFilters)
            }

            if (!handled)
                throw CodedException(404)
        }
        catch (e: Exception) {
            handleException(e, call)
        }
        finally {
            call.response.headersValues.forEach { header ->
                header.value.forEach { value ->
                    servletResponse.addHeader(header.key, value.toString())
                }
            }
            servletResponse.status = call.response.status
            servletResponse.outputStream.write(call.response.body.toString().toByteArray())
            servletResponse.outputStream.flush()

            log.trace { "Status ${servletResponse.status} <${if (handled) "" else "NOT "}HANDLED>" }
        }
    }

    private fun handleException(
        exception: Exception, call: Call, type: Class<*> = exception.javaClass) {

        log.info { "Handling '${exception.javaClass.simpleName}' exception at request processing" }

        when (exception) {
            is CodedException -> {
                val handler = codedErrors[exception.code]

                if (handler != null)
                    call.handler(exception)
                else {
                    val hnd = exceptionErrors[type]

                    if (hnd != null)
                        call.hnd(exception)
                    else {
                        val hnd2: ErrorCodeCallback =
                            { send(exception.code, exception.message ?: "") }
                        call.hnd2(exception)
                    }
                }
            }
            else -> {
                val handler = exceptionErrors[type]

                if (handler != null)
                    call.handler(exception)
                else
                    type.superclass.also { handleException(exception, call, it) }
            }
        }
    }

    private fun createResourceHandler(route: Route, resourcesFolder: URL): RouteCallback = {
        val requestPath = getRequestPath(route)
        val resourcePath = "/${resourcesFolder.path}$requestPath"
        val stream = javaClass.getResourceAsStream(resourcePath)

        if (stream == null)
            response.status = 404
        else
            returnAsset(resourcePath, stream)
    }

    private fun createResourceHandler(route: Route, resourcesFolder: File): RouteCallback = {
        val requestPath = getRequestPath(route).removePrefix("/")
        val file = resourcesFolder.resolve(requestPath).absoluteFile

        if (!file.exists())
            response.status = 404
        else
            returnAsset(file.absolutePath, file.inputStream())
    }

    private fun Call.returnAsset(resourcePath: String, stream: InputStream) {
        val contentType by lazy { contentTypeOf(request.path.substringAfterLast('.')) }

        // Should be done BEFORE flushing the stream (if not content type is ignored)
        if (response.contentType == null && contentType != null)
            response.contentType = contentType

        log.trace { "Resource for '$resourcePath' (${response.contentType}) returned" }
        val bytes = stream.readBytes()
        response.outputStream.write(bytes)
        response.outputStream.flush()
    }

    private fun Call.getRequestPath(route: Route): String {
        if (request.path.endsWith("/"))
            throw PassException()

        return request.path.removePrefix(route.path.pattern.removeSuffix("/*"))
    }
}
