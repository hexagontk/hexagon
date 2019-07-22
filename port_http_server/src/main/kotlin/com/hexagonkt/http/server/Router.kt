package com.hexagonkt.http.server

import com.hexagonkt.http.*
import com.hexagonkt.http.server.FilterOrder.AFTER
import com.hexagonkt.http.server.FilterOrder.BEFORE

import com.hexagonkt.helpers.CodedException
import com.hexagonkt.helpers.Resource
import com.hexagonkt.http.Method.GET
import com.hexagonkt.http.server.RequestHandler.*
import com.hexagonkt.http.Path
import com.hexagonkt.http.Route
import java.io.File

import kotlin.reflect.KClass

/**
 * TODO Document.
 * TODO Index routes (ie: GET /foo)
 * TODO Map with routes to all handlers needed
 * Creates and initializes a [Router] based on a code block.
 *
 * @param block Router's setup block.
 * @return A new router initialized by the passed block.
 */
class Router(block: Router.() -> Unit = {}) {

    var requestHandlers: List<RequestHandler> = emptyList(); private set

    init {
        block()
    }

    private infix fun Route.before(block: RouteCallback) {
        requestHandlers = requestHandlers + FilterHandler(this, BEFORE, block)
    }

    private infix fun Route.after(block: RouteCallback) {
        requestHandlers = requestHandlers + FilterHandler(this, AFTER, block)
    }

    private infix fun Route.by(block: RouteCallback) {
        requestHandlers = requestHandlers + RouteHandler(this, block)
    }

    fun before(path: String = "/*", block: RouteCallback) = any(path) before block

    fun after(path: String = "/*", block: RouteCallback) = any(path) after block

    fun get(path: String = "/", block: RouteCallback) = get(path) by block

    fun head(path: String = "/", block: RouteCallback) = head(path) by block

    fun post(path: String = "/", block: RouteCallback) = post(path) by block

    fun put(path: String = "/", block: RouteCallback) = put(path) by block

    fun delete(path: String = "/", block: RouteCallback) = delete(path) by block

    fun trace(path: String = "/", block: RouteCallback) = tracer(path) by block

    fun options(path: String = "/", block: RouteCallback) = options(path) by block

    fun patch(path: String = "/", block: RouteCallback) = patch(path) by block

    fun any(path: String = "/", block: RouteCallback) = any(path) by block

    fun error(code: Int, block: ErrorCodeCallback) {
        requestHandlers = requestHandlers + CodeHandler(Route(Path("/"), ALL), code, block)
    }

    fun error(exception: KClass<out Exception>, block: ExceptionCallback) {
        error(exception.java, block)
    }

    fun error(exception: Class<out Exception>, block: ExceptionCallback) {
        require(exception != CodedException::class.java) {
            "${exception.name} is internal and can't be handled"
        }
        requestHandlers = requestHandlers + ExceptionHandler(Route(Path("/"), ALL), exception, block)
    }

    fun path(path: Path, router: Router) {
        requestHandlers = requestHandlers + PathHandler(Route(path), router)
    }

    fun path(handler: Router) { path("/", handler) }

    fun path(block: Router.() -> Unit) = path(Router(block))

    fun path(path: String, router: Router) = path(Path(path), router)

    fun path(path: String, block: Router.() -> Unit) = path(Path(path), Router(block))

    fun get(resource: Resource) {
        get("/*", resource)
    }

    fun get(path: String, resource: Resource) {
        requestHandlers = requestHandlers + ResourceHandler(Route(Path(path), GET), resource)
    }

    fun get(file: File) {
        get("/*", file)
    }

    fun get(path: String, file: File) {
        requestHandlers = requestHandlers + FileHandler(Route(Path(path), GET), file)
    }

    fun flatRequestHandlers(h: List<RequestHandler> = requestHandlers): List<RequestHandler> = h
        .flatMap { handler ->
            when (handler) {
                is PathHandler -> flatPathHandler(handler)
                is RouteHandler -> handler.route.list().map { handler.copy(route = it) }
                else -> listOf(handler)
            }
        }

    private fun flatPathHandler(handler: PathHandler): List<RequestHandler> {
        return handler.router.requestHandlers.flatMap {
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
                is RouteHandler -> nestedRoute.list().map { r -> it.copy(route = r) }
                is ExceptionHandler -> listOf(it.copy(route = nestedRoute))
                is CodeHandler -> listOf(it.copy(route = nestedRoute))
                is ResourceHandler -> listOf(it.copy(route = nestedRoute))
                is FileHandler -> listOf(it.copy(route = nestedRoute))
                is PathHandler -> flatRequestHandlers(listOf(it.copy(route = nestedRoute)))
            }
        }
    }
}
