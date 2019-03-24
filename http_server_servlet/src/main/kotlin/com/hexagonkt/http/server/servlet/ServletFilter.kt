package com.hexagonkt.http.server.servlet

import com.hexagonkt.http.Method
import com.hexagonkt.helpers.CodedException
import com.hexagonkt.helpers.Logger
import com.hexagonkt.http.ALL
import com.hexagonkt.http.server.*
import com.hexagonkt.http.server.FilterOrder.AFTER
import com.hexagonkt.http.server.FilterOrder.BEFORE
import com.hexagonkt.http.server.RequestHandler.*
import com.hexagonkt.http.Path
import com.hexagonkt.http.Route
import com.hexagonkt.serialization.SerializationManager.contentTypeOf

import javax.servlet.*
import javax.servlet.http.HttpServletRequest as HttpRequest
import javax.servlet.http.HttpServletResponse as HttpResponse

/**
 * TODO .
 */
class ServletFilter (router: List<RequestHandler>) : Filter {

    /**
     * Exception used for stopping the execution. It is used only for flow control.
     */
    private class PassException: RuntimeException ()

    private val log: Logger = Logger(this)

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
                is AssetsHandler -> RouteHandler(it.route, createResourceHandler(it.route, it.path))
                else -> it
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
    private fun filter(req: Request, call: Call, filters: List<Pair<Route, RouteCallback>>) {
        filters
            .filter { it.first.path.matches(req.path) }
            .map {
                req.actionPath = it.first.path
                call.(it.second)()
                log.trace { "Filter for path '${it.first.path}' executed" }
            }
    }

    private fun route(call: Call, bRequest: Request): Boolean {
        val routes = routesByMethod[call.request.method]
        val methodRoutes = routes?.filter { it.route.path.matches(call.request.path) } ?: emptyList()

        for ((first, second) in methodRoutes) {
            try {
                bRequest.actionPath = first.path
                call.second()

                log.trace { "Route for path '${bRequest.actionPath}' executed" }
                return true
            }
            catch (e: PassException) {
                log.trace { "Handler for path '${bRequest.actionPath}' passed" }
                continue
            }
        }
        return false
    }

    override fun init(filterConfig: FilterConfig) { /* Not implemented */ }

    override fun destroy() { /* Not implemented */ }

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
//        if (request.isAsyncSupported) {
//            val asyncContext = request.startAsync()
//            val context = request.servletContext // Must be passed and fetched outside executor
//            executor.execute {
//                doFilter(asyncContext.request, asyncContext.response, context)
//                asyncContext.complete()
//            }
//        }
//        else {
//            doFilter(request, response)
//        }

        // TODO Temporary hack only valid for Jetty
        val multipartConfig = MultipartConfigElement("/tmp")
        request.setAttribute("org.eclipse.jetty.multipartConfig", multipartConfig)

        doFilter(request, response)
    }

    private fun doFilter(
        request: ServletRequest,
        response: ServletResponse) {

        if (request !is HttpRequest || response !is HttpResponse)
            error("Invalid request/response parameters")

        val bRequest = Request(request)
        val bResponse = Response(request, response)
        val bSession = Session(request)
        val call = Call(bRequest, bResponse, bSession)
        var handled = false

        try {
            try {
                filter(bRequest, call, beforeFilters)
                handled = route(call, bRequest)
            }
            finally {
                filter(bRequest, call, afterFilters)
            }

            if (!handled)
                throw CodedException(404)
        }
        catch (e: Exception) {
            handleException(e, call)
        }
        finally {
            // TODO Try needed because of a problem with Jetty's response redirect: fix and remove
            try {
                call.response.headers.forEach { header ->
                    header.value.forEach { value ->
                        response.addHeader(header.key, value.toString())
                    }
                }
                response.status = call.response.status
                response.outputStream.write(call.response.body.toString().toByteArray())
                response.outputStream.flush()
            }
            catch (e: Exception) {
                log.warn(e) { "Error handling request: ${bRequest.actionPath}" }
            }

            log.trace { "Status ${response.status} <${if (handled) "" else "NOT "}HANDLED>" }
        }
    }

    private fun handleException(
        exception: Exception, call: Call, type: Class<*> = exception.javaClass) {

        when (exception) {
            is CodedException -> {
                val handler: ErrorCodeCallback =
                    codedErrors[exception.code] ?: { send(it, exception.message ?: "") }
                call.handler(exception.code)
            }
            else -> {
                log.error(exception) { "Error processing request" }

                val handler = exceptionErrors[type]

                if (handler != null)
                    call.handler(exception)
                else
                    type.superclass.also { if (it != null) handleException(exception, call, it) }
            }
        }
    }

    private fun createResourceHandler(route: Route, resourcesFolder: String): RouteCallback = {
        if (request.path.endsWith("/"))
            throw PassException()

        val requestPath = request.path.removePrefix(route.path.path.removeSuffix("/*"))
        val resourcePath = "/$resourcesFolder$requestPath"
        val stream = javaClass.getResourceAsStream(resourcePath)

        if (stream == null) {
            response.status = 404
            throw PassException()
        }
        else {
            val contentType by lazy { contentTypeOf(request.path.substringAfterLast('.')) }

            // Should be done BEFORE flushing the stream (if not content type is ignored)
            if (response.contentType == null && contentType != null)
                response.contentType = contentType

            log.trace { "Resource for '$resourcePath' (${response.contentType}) returned" }
            val bytes = stream.readBytes()
            response.outputStream.write(bytes)
            response.outputStream.flush()
        }
    }
}
