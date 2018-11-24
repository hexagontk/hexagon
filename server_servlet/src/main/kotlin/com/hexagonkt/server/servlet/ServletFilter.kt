package com.hexagonkt.server.servlet

import com.hexagonkt.HttpMethod
import com.hexagonkt.helpers.CodedException
import com.hexagonkt.helpers.Logger
import com.hexagonkt.serialization.serialize
import com.hexagonkt.server.*
import com.hexagonkt.server.FilterOrder.AFTER
import com.hexagonkt.server.FilterOrder.BEFORE
import com.hexagonkt.server.RequestHandler.*

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

    private val notFoundHandler: ErrorCodeCallback = { error(404, "${request.url} not found") }
    private val baseExceptionHandler: ExceptionCallback =
        { error(500, "${it.javaClass.simpleName} (${it.message ?: "no details"})") }

    private val allHandlers = listOf(
        CodeHandler(Route(Path("/"), ALL), 404, notFoundHandler),
        ExceptionHandler(Route(Path("/"), ALL), Exception::class.java, baseExceptionHandler)
    ) + router

    private val routesByMethod: Map<HttpMethod, List<RouteHandler>> = router
        .asSequence()
        .map {
            when (it) {
                is PathHandler -> it // TODO
                is AssetsHandler -> RouteHandler(it.route, createResourceHandler(it.path))
                else -> it
            }
        }
        .filterIsInstance(RouteHandler::class.java)
        .groupBy { it.route.methods.first() }

    private val filtersByOrder = router
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
        req: BServletRequest,
        call: Call,
        filters: List<Pair<Route, FilterCallback>>): Boolean =
            filters
                .filter { it.first.path.matches(req.path) }
                .map {
                    req.actionPath = it.first.path
                    call.(it.second)()
                    log.trace { "Filter for path '${it.first.path}' executed" }
                    true
                }
                .isNotEmpty()

    private fun route(call: Call, bRequest: BServletRequest): Boolean {
        val routes = routesByMethod[call.request.method]
        val methodRoutes = routes?.filter { it.route.path.matches(call.request.path) } ?: emptyList()

        if (methodRoutes.isEmpty())
            throw CodedException(405, "Invalid method '${call.request.method}'")

        for ((first, second) in methodRoutes) {
            try {
                bRequest.actionPath = first.path
                call.handleResult(call.second())

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
            doFilter(request, response)
//        }
    }

    private fun doFilter(
        request: ServletRequest,
        response: ServletResponse,
        context: ServletContext = request.servletContext) {

        if (request !is HttpRequest || response !is HttpResponse)
            error("Invalid request/response parameters")

        val bRequest = BServletRequest(request)
        val bResponse = BServletResponse(request, response, context)
        val bSession = BServletSession(request)
        val exchange = Call(Request(bRequest), Response(bResponse), Session(bSession))
        var handled = false

        try {
            handled = filter(bRequest, exchange, beforeFilters)
            handled = route(exchange, bRequest) || handled // Order matters!!!
            handled = filter(bRequest, exchange, afterFilters) || handled // Order matters!!!

            if (!handled)
                throw CodedException(404)
        }
        catch (e: Exception) {
            handleException(e, exchange)
        }
        finally {
            // TODO Try needed because of a problem with Jetty's response redirect: fix and remove
            try {
                response.status = exchange.response.status
                response.outputStream.write(exchange.response.body.toString().toByteArray())
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
                    codedErrors[exception.code] ?: { error(it, exception.message ?: "") }
                call.handleResult(call.handler(exception.code))
            }
            else -> {
                log.error(exception) { "Error processing request" }

                val handler = exceptionErrors[type]

                if (handler != null)
                    call.handleResult(call.handler(exception))
                else
                    type.superclass.also { if (it != null) handleException(exception, call, it) }
            }
        }
    }

    private fun createResourceHandler(resourcesFolder: String): RouteCallback = {
        if (request.path.endsWith("/"))
            throw PassException()

        val resourcePath = "/$resourcesFolder${request.path}"
        val stream = javaClass.getResourceAsStream(resourcePath)

        if (stream == null) {
            response.status = 404
            response.statusChanged = false // TODO Handle this in a better way
            throw PassException()
        }
        else {
            val contentType by lazy { response.getMimeType(request.path) }

            // Should be done BEFORE flushing the stream (if not content type is ignored)
            if (response.contentType == null && contentType != null)
                response.contentType = contentType

            log.trace { "Resource for '$resourcePath' (${response.contentType}) found and returned" }
            val bytes = stream.readBytes()
            response.outputStream.write(bytes)
            response.outputStream.flush()
        }
    }

    private fun Call.handleResult(result: Any) {
        log.trace { "Result of type: ${result.javaClass.name}" }
        when (result) {
            Unit -> {
                if (!response.statusChanged && response.status != 302)
                    response.status = 200
            }
            is Nothing -> {
                if (!response.statusChanged && response.status != 302)
                    response.status = 200
            }
            is Int -> response.status = result
            is String -> ok(result)
            is Pair<*, *> -> ok(
                code = result.first as? Int ?: 200,
                content = result.second.let {
                    it as? String
                        ?: it?.serialize(serializationFormat())
                        ?: ""
                }
            )
            else -> ok(result.serialize(serializationFormat()))
        }
    }
}
