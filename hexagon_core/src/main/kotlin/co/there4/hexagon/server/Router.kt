package co.there4.hexagon.server

import co.there4.hexagon.server.FilterOrder.AFTER
import co.there4.hexagon.server.FilterOrder.BEFORE

import co.there4.hexagon.helpers.CodedException
import co.there4.hexagon.server.HttpMethod.GET
import co.there4.hexagon.server.RequestHandler.*

import kotlin.reflect.KClass

/**
 * TODO Document.
 * TODO Index routes (ie: GET /foo)
 * TODO Map with routes to all handlers needed
 */
class Router {
    var requestHandlers: List<RequestHandler> = emptyList(); private set

    infix fun Route.before(block: FilterCallback) {
        requestHandlers += FilterHandler(this, BEFORE, block)
    }

    infix fun Route.after(block: FilterCallback) {
        requestHandlers += FilterHandler(this, AFTER, block)
    }

    infix fun Route.by(block: RouteCallback) {
        requestHandlers += RouteHandler(this, block)
    }

    fun before(path: String = "/*", block: FilterCallback) = all(path) before block
    fun after(path: String = "/*", block: FilterCallback) = all(path) after block
    fun get(path: String = "/", block: RouteCallback) = get(path) by block
    fun head(path: String = "/", block: RouteCallback) = head(path) by block
    fun post(path: String = "/", block: RouteCallback) = post(path) by block
    fun put(path: String = "/", block: RouteCallback) = put(path) by block
    fun delete(path: String = "/", block: RouteCallback) = delete(path) by block
    fun trace(path: String = "/", block: RouteCallback) = tracer(path) by block
    fun options(path: String = "/", block: RouteCallback) = options(path) by block
    fun patch(path: String = "/", block: RouteCallback) = patch(path) by block

    fun error(code: Int, block: ErrorCodeCallback) {
        requestHandlers += CodeHandler(Route(Path("/"), ALL), code, block)
    }

    fun error(exception: KClass<out Exception>, block: ExceptionCallback) {
        error(exception.java, block)
    }

    fun error(exception: Class<out Exception>, block: ExceptionCallback) {
        require(exception != CodedException::class.java) {
            "${exception.name} is internal and can't be handled"
        }
        requestHandlers += ExceptionHandler(Route(Path("/"), ALL), exception, block)
    }

    infix fun Path.mount(handler: Router) { requestHandlers += PathHandler(Route(this), handler) }

    fun path(handler: Router) { path("/", handler) }
    fun path(block: Router.() -> Unit) = path(router(block))
    fun path(path: Path, router: Router) = path mount router
    fun path(path: String, router: Router) = path(Path(path), router)
    fun path(path: String, block: Router.() -> Unit) = Path(path) mount router(block)

    fun assets (resource: String, path: String = "/*") {
        requestHandlers += AssetsHandler(Route(Path(path), GET), resource)
    }

    fun flatRequestHandlers(h: List<RequestHandler> = requestHandlers): List<RequestHandler> = h
        .flatMap { handler ->
            @Suppress("IfThenToElvis")
            if (handler is PathHandler)
                handler.router.requestHandlers.flatMap {
                    val route = it.route
                    val path = route.path
                    val handlerPath = handler.route.path.path

                    val finalPath =
                        if (handlerPath == "/")
                            if (path.path == "/") "/" else path.path
                        else
                            if (path.path == "/") handlerPath else handlerPath + path.path

                    val nestedPath = path.copy(path = finalPath)
                    val nestedRoute = route.copy(path = nestedPath)

                    when (it) {
                        is FilterHandler -> listOf(it.copy(route = nestedRoute))
                        is RouteHandler -> listOf(it.copy(route = nestedRoute))
                        is ExceptionHandler -> listOf(it.copy(route = nestedRoute))
                        is CodeHandler -> listOf(it.copy(route = nestedRoute))
                        is AssetsHandler -> listOf(it.copy(route = nestedRoute))
                        is PathHandler -> flatRequestHandlers(listOf(it.copy(route = nestedRoute)))
                    }
                }
            else
                listOf(handler)
        }
}
