package co.there4.hexagon.server.engine.servlet

import co.there4.hexagon.helpers.CachedLogger
import co.there4.hexagon.helpers.CodedException
import co.there4.hexagon.serialization.serialize
import co.there4.hexagon.server.*
import co.there4.hexagon.server.FilterOrder.AFTER
import co.there4.hexagon.server.FilterOrder.BEFORE
import co.there4.hexagon.server.RequestHandler.FilterHandler
import co.there4.hexagon.server.RequestHandler.RouteHandler
import co.there4.hexagon.server.engine.PassException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.servlet.*
import javax.servlet.http.HttpServletRequest as HttpRequest
import javax.servlet.http.HttpServletResponse as HttpResponse

/**
 * @author jam
 */
internal class ServletFilter (private val router: Router) : CachedLogger(ServletFilter::class), Filter {
    companion object : CachedLogger(ServletFilter::class)

    private val routesByMethod: Map<HttpMethod, List<Pair<Route, RouteCallback>>> =
        router.requestHandlers
            .filterIsInstance(RouteHandler::class.java)
            .map { it.route to it.handler }
            .groupBy { it.first.method.first() }

    private val filtersByOrder = router.requestHandlers
        .filterIsInstance(FilterHandler::class.java)
        .groupBy { it.order }
        .mapValues { it.value.map { it.route to it.handler } }

    private val beforeFilters = filtersByOrder[BEFORE] ?: listOf()
    private val afterFilters = filtersByOrder[AFTER] ?: listOf()

    private val executor: ExecutorService = Executors.newFixedThreadPool(8)

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
                    trace("Filter for path '${it.first.path}' executed")
                    true
                }
                .isNotEmpty()

    private fun route(exchange: Call, bRequest: BServletRequest): Boolean {
        val methodRoutes = routesByMethod[exchange.request.method]
            ?.filter { it.first.path.matches(exchange.request.path) }
            ?: throw CodedException(405, "Invalid method '${exchange.request.method}'")

        for ((first, second) in methodRoutes) {
            try {
                bRequest.actionPath = first.path
                val result = exchange.second()

                /*
                 * TODO Handle result (warn if body has been set)
                 * Unit -> 200 <empty>
                 * Pair (403 to "Forbidden") -> <code> <body>
                 * List -> serialize with "accept header", "response.contentType" or default format
                 * Stream -> streaming
                 */
                when (result) {
                    is Unit -> {}
                    is Nothing -> {}
                    is Int -> exchange.response.status = result
                    is String -> exchange.response.body = result
                    is Pair<*, *> -> {
                        if (result.first is Int)
                            exchange.response.status = result.first as Int

                    }
                    else -> result.serialize(exchange.response.contentType
                        ?: exchange.request.contentType
                        ?: co.there4.hexagon.helpers.err
                    )
                }

                trace("Route for path '${bRequest.actionPath}' executed")
                return true
            }
            catch (e: PassException) {
                trace("Handler for path '${bRequest.actionPath}' passed")
                continue
            }
        }
        return false
    }

    override fun init(filterConfig: FilterConfig) { /* Not implemented */ }
    override fun destroy() { /* Not implemented */ }

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        if (request.isAsyncSupported) {
            val asyncContext = request.startAsync()
            val context = request.servletContext // Must be passed and fetched outside executor
            executor.execute {
                doFilter(asyncContext.request, asyncContext.response, context)
                asyncContext.complete()
            }
        }
        else {
            doFilter(request, response)
        }
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
            handled = route(exchange, bRequest) || handled
            handled = filter(bRequest, exchange, afterFilters) || handled // Order matters!!!

            if (!handled)
                throw CodedException(404)
        }
        catch (e: Exception) {
            router.handleError(e, exchange)
        }
        finally {
            response.status = exchange.response.status
            response.outputStream.write(exchange.response.body.toString().toByteArray())
            response.outputStream.flush()

            trace("Status ${response.status} <${if (handled) "" else "NOT "}HANDLED>")
        }
    }
}
