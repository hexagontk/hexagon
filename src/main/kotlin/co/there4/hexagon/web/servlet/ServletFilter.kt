package co.there4.hexagon.web.servlet

import co.there4.hexagon.util.CompanionLogger
import co.there4.hexagon.web.*
import co.there4.hexagon.web.FilterOrder.*
import co.there4.hexagon.web.HttpMethod.GET
import co.there4.hexagon.web.Filter as HexagonFilter
import javax.servlet.*
import javax.servlet.Filter
import javax.servlet.http.HttpServletRequest as HttpRequest
import javax.servlet.http.HttpServletResponse as HttpResponse

/**
 * @author jam
 */
class ServletFilter (private val router: Router) : CompanionLogger(ServletFilter::class), Filter {
    companion object : CompanionLogger(ServletFilter::class)

    private val routesByMethod: Map<HttpMethod, List<Pair<Route, Exchange.() -> Unit>>> =
        router.routes.entries.map { it.key to it.value }.groupBy { it.first.method }

    private val filtersByOrder = router.filters.entries
        .groupBy { it.key.order }
        .mapValues { it.value.map { it.key to it.value } }

    private val beforeFilters = filtersByOrder[BEFORE] ?: listOf()
    private val afterFilters = filtersByOrder[AFTER] ?: listOf()

    /**
     * TODO Take care of filters that throw exceptions
     */
    private fun filter(
        request: HttpRequest,
        exchange: Exchange,
        filters: List<Pair<HexagonFilter, Exchange.() -> Unit>>): Boolean =
            filters
                .filter { it.first.path.matches(request.servletPath) }
                .map {
                    (exchange.request as BServletRequest).actionPath = it.first.path
                    exchange.(it.second)()
                    trace("Filter for path '${it.first.path}' executed")
                    true
                }
                .isNotEmpty()

    override fun init(filterConfig: FilterConfig) { /* Not implemented */ }
    override fun destroy() { /* Not implemented */ }

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        if (request !is HttpRequest || response !is HttpResponse)
            error("Invalid request/response parmeters")

        val methodRoutes = routesByMethod[HttpMethod.valueOf (request.method)]?.filter {
            it.first.path.matches(request.servletPath)
        }

        val bRequest = BServletRequest (request)
        val bResponse = BServletResponse (request, response)
        val bSession = BServletSession (request)
        val exchange = Exchange(bRequest, bResponse, bSession)
        var handled = false

        try {
            handled = filter(request, exchange, beforeFilters)
            var resource = false

            if (bRequest.method == GET &&
                !request.servletPath.endsWith("/")) { // Reading a folder as resource gets all files

                resource = returnResource(bResponse, request, response)
                if (resource)
                    handled = true
            }

            if(!resource) {
                if (methodRoutes == null) {
                    bResponse.status = 405
                    bResponse.body = "Invalid method '${request.method}'"
                    trace(bResponse.body as String)
                    handled = true
                }
                else {
                    for (r in methodRoutes) {
                        try {
                            bRequest.actionPath = r.first.path
                            exchange.(r.second)()
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

            handled = filter(request, exchange, afterFilters) || handled // Order matters!!!
        }
        catch (e: EndException) {
            trace("Request processing ended by callback request")
            handled = true
        }
        catch (e: Exception) {
            err ("Error processing request", e)
            router.handleException(e, exchange)
            handled = true
        }
        finally {
            if (!handled)
                exchange.(router.notFoundHandler)()

            response.status = exchange.response.status
            response.outputStream.write(exchange.response.body.toString().toByteArray())
            response.outputStream.flush()

            trace("Status ${response.status} <${if (handled) "" else "NOT "}HANDLED>")
        }
    }

    private fun returnResource(
        bResponse: BServletResponse, request: HttpRequest, response: HttpResponse): Boolean {

        val resourcePath = "/public${request.servletPath}"
        val stream = javaClass.getResourceAsStream(resourcePath)

        if (stream == null)
            return false
        else {
            val contentType = bResponse.getMimeType(request.servletPath)

            // Should be done BEFORE flushing the stream (if not content type is ignored)
            if (response.contentType == null && contentType != null)
                response.contentType = contentType

            trace("Resource for '$resourcePath' (${response.contentType}) found and returned")
            response.outputStream.write(stream.readBytes())
            response.outputStream.flush()
            return true
        }
    }
}
