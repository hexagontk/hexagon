package co.there4.hexagon.web.servlet

import co.there4.hexagon.util.CompanionLogger
import co.there4.hexagon.web.*
import co.there4.hexagon.web.FilterOrder.*
import co.there4.hexagon.web.Filter as BlacksheepFilter
import javax.servlet.*
import javax.servlet.Filter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author jam
 */
class ServletFilter (
    val server: Server,
    val filters: MutableMap<BlacksheepFilter, Exchange.() -> Unit> = server.filters,
    val routes: MutableMap<Route, Exchange.() -> Unit> = server.routes
) : Filter {

    companion object : CompanionLogger(ServletFilter::class)

    private val routesByMethod: Map<HttpMethod, List<Pair<Route, Exchange.() -> Unit>>> =
        routes.entries.map { it.key to it.value }.groupBy { it.first.method }

    override fun init(filterConfig: FilterConfig) { /* Not implemented */ }
    override fun destroy() { /* Not implemented */ }

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val req = request as HttpServletRequest
        val res = response as HttpServletResponse
        val fs = filters.filter { it.key.path.matches(req.servletPath) }
        val methodRoutes = routesByMethod[HttpMethod.valueOf (req.method)]?.filter {
            it.first.path.matches(req.servletPath)
        }

        val bRequest = BServletRequest (req)
        val bResponse = BServletResponse (req, res)
        val bSession = BServletSession (req)

        val exchange = Exchange ( bRequest, bResponse, bSession )
        var handled = false

        try {
            fs.filter { it.key.order == BEFORE }.forEach {
                bRequest.actionPath = it.key.path
                exchange.(it.value)()
                handled = true
            }

            if (methodRoutes == null) {
                bResponse.status = 405
                bResponse.body = "Invalid method '${req.method}'"
                handled = true
            }
            else if (methodRoutes.isEmpty()) {
                val stream = javaClass.getResourceAsStream("/public" + req.servletPath)
                if (stream != null) {
                    response.outputStream.write(stream.readBytes())
                    response.outputStream.flush()
                    handled = true
                }
            }
            else {
                methodRoutes.forEach {
                    bRequest.actionPath = it.first.path
                    exchange.(it.second)()
                    handled = true
                }
            }

            fs.filter { it.key.order == AFTER }.forEach {
                bRequest.actionPath = it.key.path
                exchange.(it.value)()
                handled = true
            }
        }
        catch (e: EndException) {
            handled = true
        }
        catch (e: Exception) {
            server.handleException(e, exchange)
        }
        finally {
            if (handled)
                response.status = exchange.response.status
            else
                response.status = 404

            try {
                response.writer?.write(exchange.response.body.toString())
                response.writer?.flush()
            }
            catch (e: Exception) {
                // TODO Handle
            }
        }
    }
}
