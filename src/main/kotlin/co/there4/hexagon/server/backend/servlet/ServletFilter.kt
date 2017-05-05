package co.there4.hexagon.server.backend.servlet

import co.there4.hexagon.settings.SettingsManager
import co.there4.hexagon.util.CachedLogger
import co.there4.hexagon.util.CodedException
import co.there4.hexagon.util.resource
import co.there4.hexagon.server.*
import co.there4.hexagon.server.FilterOrder.AFTER
import co.there4.hexagon.server.FilterOrder.BEFORE
import co.there4.hexagon.server.HttpMethod.GET
import co.there4.hexagon.server.RequestHandler.FilterHandler
import co.there4.hexagon.server.RequestHandler.RouteHandler
import co.there4.hexagon.server.backend.PassException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.servlet.*
import javax.servlet.Filter
import javax.servlet.http.HttpServletRequest as HttpRequest
import javax.servlet.http.HttpServletResponse as HttpResponse

/**
 * @author jam
 */
internal class ServletFilter (private val router: Router) : CachedLogger(ServletFilter::class), Filter {
    companion object : CachedLogger(ServletFilter::class)

    @Deprecated("Replaced by `assets` router method")
    val resourcesFolder = SettingsManager.setting<String>("resourcesFolder") ?: "public"

    private val processResources = resource(resourcesFolder) != null

//    private val routesByMethod: Map<HttpMethod, List<Pair<Route, RouteCallback>>> =
//        router.routes.entries.map { it.key to it.value }.groupBy { it.first.method.first() }
//
//    private val filtersByOrder = router.filters.entries
//        .groupBy { it.key.order }
//        .mapValues { it.value.map { it.key to it.value } }

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

            var resource = false

            val servletPath = request.servletPath
            val filledPath = if (servletPath.isEmpty()) request.pathInfo else servletPath
            if (processResources &&
                bRequest.method == GET &&
                !filledPath.endsWith("/")) { // Reading a folder as resource gets all files

                resource = returnResource(bResponse, request, response)
                if (resource)
                    handled = true
            }

            if (!resource) {
                val methodRoutes = routesByMethod[HttpMethod.valueOf(request.method)]
                    ?.filter { it.first.path.matches(filledPath) }

                if (methodRoutes == null) {
                    bResponse.status = 405
                    bResponse.body = "Invalid method '${request.method}'"
                    trace(bResponse.body as String)
                    handled = true
                }
                else {
                    for ((first, second) in methodRoutes) {
                        try {
                            bRequest.actionPath = first.path
                            val result = exchange.second()
                            /*
                             * TODO Handle result (warn if body has been set)
                             * Unit -> 200 <empty>
                             * Int -> <status> <empty>
                             * String -> 200 body
                             * Pair (403 to "Forbidden") -> <code> <body>
                             * Map -> serialize with "accept" or default format
                             * List -> serialize with "accept header", "response.contentType" or default format
                             * Stream -> streaming
                             */

                            when (result) {
                                is Unit -> {}
                                is Nothing -> {}
                                is Int -> exchange.response.status = result
                                is String -> exchange.response.body = result
                                is Pair<*, *> -> ""
                                is Map<*, *> -> ""
                                is List<*> -> ""
                            }

                            trace("Route for path '${bRequest.actionPath}' executed")
                            handled = true
                            break
                        }
                        catch (e: PassException) {
                            trace("Handler for path '${bRequest.actionPath}' passed")
                            continue
                        }
                    }
                }
            }

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

    private fun returnResource(
        bResponse: BServletResponse, request: HttpRequest, response: HttpResponse): Boolean {

        val servletPath = request.servletPath
        val path = if (servletPath.isEmpty()) request.pathInfo else servletPath
        val resourcePath = "/$resourcesFolder$path"
        val stream = javaClass.getResourceAsStream(resourcePath)

        if (stream == null)
            return false
        else {
            val contentType by lazy { bResponse.getMimeType(path) }

            // Should be done BEFORE flushing the stream (if not content type is ignored)
            if (response.contentType == null && contentType != null)
                response.contentType = contentType

            trace("Resource for '$resourcePath' (${response.contentType}) found and returned")
            val bytes = stream.readBytes()
            response.outputStream.write(bytes)
            response.outputStream.flush()
            return true
        }
    }
}
