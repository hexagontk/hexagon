package co.there4.hexagon.web.backend.servlet

import co.there4.hexagon.util.CachedLogger
import co.there4.hexagon.util.CodedException
import co.there4.hexagon.util.resource
import co.there4.hexagon.web.*
import co.there4.hexagon.web.FilterOrder.AFTER
import co.there4.hexagon.web.FilterOrder.BEFORE
import co.there4.hexagon.web.HttpMethod.GET
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.servlet.*
import javax.servlet.Filter
import co.there4.hexagon.web.Filter as HexagonFilter
import javax.servlet.http.HttpServletRequest as HttpRequest
import javax.servlet.http.HttpServletResponse as HttpResponse

/**
 * @author jam
 */
internal class ServletFilter (private val router: Router) : CachedLogger(ServletFilter::class), Filter {
    companion object : CachedLogger(ServletFilter::class)

    private val processResources = resource(resourcesFolder) != null
    private val routesByMethod: Map<HttpMethod, List<Pair<Route, Handler>>> =
        router.routes.entries.map { it.key to it.value }.groupBy { it.first.method }

    private val filtersByOrder = router.filters.entries
        .groupBy { it.key.order }
        .mapValues { it.value.map { it.key to it.value } }

    private val beforeFilters = filtersByOrder[BEFORE] ?: listOf()
    private val afterFilters = filtersByOrder[AFTER] ?: listOf()

    private val executor: ExecutorService = Executors.newFixedThreadPool(8)

    /**
     * TODO Take care of filters that throw exceptions
     */
    private fun filter(
        request: HttpRequest,
        req: BServletRequest,
        exchange: Exchange,
        filters: List<Pair<HexagonFilter, Handler>>): Boolean =
            filters
                .filter {
                    val servletPath = request.servletPath
                    val requestUrl = if (servletPath.isEmpty()) request.pathInfo else servletPath
                    it.first.path.matches(requestUrl)
                }
                .map {
                    req.actionPath = it.first.path
                    exchange.(it.second)()
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
        val exchange = Exchange(Request(bRequest), Response(bResponse), Session(bSession))
        var handled = false

        try {
            handled = filter(request, bRequest, exchange, beforeFilters)

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

                            // TODO Handle result (warn if body has been set)

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

            handled = filter(request, bRequest, exchange, afterFilters) || handled // Order matters!
            if (!handled)
                throw CodedException(404)
        }
        catch (e: Exception) {
            router.handle(e, exchange)
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
