package com.hexagonkt.http.server

import com.hexagonkt.http.*
import com.hexagonkt.http.server.FilterOrder.AFTER
import com.hexagonkt.http.server.FilterOrder.BEFORE

import com.hexagonkt.http.Method.GET
import com.hexagonkt.http.server.RequestHandler.*
import com.hexagonkt.http.Path
import com.hexagonkt.http.Route
import java.io.File
import java.net.URL

import kotlin.reflect.KClass

/**
 * Creates and initializes a [Router] based on a code block.
 *
 * @param block Router's setup block.
 * @return A new router initialized by the passed block.
 */
class Router(block: Router.() -> Unit = {}) {

    /**
     * [List] of [RequestHandler] instances.
     */
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

    /**
     * Registers [FilterHandler] with [BEFORE] [FilterOrder] for given route [path].
     *
     * @param path Route path for which the handler is to be registered.
     * @param block [RouteCallback] instance to be executed by the handler.
     */
    fun before(path: String = "*", block: RouteCallback) = any(path) before block

    /**
     * Registers [FilterHandler] with [AFTER] [FilterOrder] for given route [path].
     *
     * @param path Route path for which the handler is to be registered.
     * @param block [RouteCallback] instance to be execited by the handler.
     */
    fun after(path: String = "*", block: RouteCallback) = any(path) after block

    /**
     * Creates GET route with given [RouteCallback] to be handled.
     *
     * @param path Route path of the request.
     * @param block A [RouteCallback] instance to be handled.
     */
    fun get(path: String = "/", block: RouteCallback) = get(path) by block

    /**
     * Creates HEAD route with given [RouteCallback] to be handled.
     *
     * @param path Route path of the request.
     * @param block A [RouteCallback] instance to be handled.
     */
    fun head(path: String = "/", block: RouteCallback) = head(path) by block

    /**
     * Creates POST route with given [RouteCallback] to be handled.
     *
     * @param path Route path of the request.
     * @param block A [RouteCallback] instance to be handled.
     */
    fun post(path: String = "/", block: RouteCallback) = post(path) by block

    /**
     * Creates PUT route with given [RouteCallback] to be handled.
     *
     * @param path Route path of the request.
     * @param block A [RouteCallback] instance to be handled.
     */
    fun put(path: String = "/", block: RouteCallback) = put(path) by block

    /**
     * Creates DELETE route with given [RouteCallback] to be handled.
     *
     * @param path Route path of the request.
     * @param block A [RouteCallback] instance to be handled.
     */
    fun delete(path: String = "/", block: RouteCallback) = delete(path) by block

    /**
     * Creates TRACE route with given [RouteCallback] to be handled.
     *
     * @param path Route path of the request.
     * @param block A [RouteCallback] instance to be handled.
     */
    fun trace(path: String = "/", block: RouteCallback) = trace(path) by block

    /**
     * Creates OPTIONS route with given [RouteCallback] to be handled.
     *
     * @param path Route path of the request.
     * @param block A [RouteCallback] instance to be handled.
     */
    fun options(path: String = "/", block: RouteCallback) = options(path) by block

    /**
     * Creates PATH route with given [RouteCallback] to be handled.
     *
     * @param path Route path of the request.
     * @param block A [RouteCallback] instance to be handled.
     */
    fun patch(path: String = "/", block: RouteCallback) = patch(path) by block

    /**
     * Creates a route for a filter (with all methods) with given [RouteCallback] to be handled.
     *
     * @param path Route path of the request.
     * @param block A [RouteCallback] instance to be executed.
     */
    fun any(path: String = "/", block: RouteCallback) = any(path) by block

    /**
     * Registers handler for routes halted with specified [code].
     *
     * @param code Status code.
     * @param block Callback to be executed by the handler.
     */
    fun error(code: Int, block: ErrorCodeCallback) {
        requestHandlers = requestHandlers + CodeHandler(Route(Path("/"), ALL), code, block)
    }

    /**
     * Registers handler for routes halted due to specified [exception].
     *
     * @param exception [Exception] [KClass] to register callback for.
     * @param block Callback to be executed by the handler.
     */
    fun error(exception: KClass<out Exception>, block: ExceptionCallback) {
        error(exception.java, block)
    }

    /**
     * Registers handler for routes halted due to specified [exception].
     *
     * @param exception [Exception] [Class] to register callback for.
     * @param block Callback to be executed by the handler.
     */
    fun error(exception: Class<out Exception>, block: ExceptionCallback) {
        val rootPath = Route(Path("/"), ALL)
        requestHandlers = requestHandlers + ExceptionHandler(rootPath, exception, block)
    }

    /**
     * Registers [PathHandler] for given route [path].
     *
     * @param path Route path for which the callback is to be registered.
     * @param router Nested [Router] handler.
     */
    fun path(path: Path, router: Router) {
        requestHandlers = requestHandlers + PathHandler(Route(path), router)
    }

    /**
     * Registers [PathHandler] for "/" route with given [handler].
     *
     * @param handler Nested [Router] handler.
     */
    fun path(handler: Router) { path("/", handler) }

    /**
     * Registers [PathHandler] with specified [block].
     *
     * @param block [Router] handler block.
     */
    fun path(block: Router.() -> Unit) = path(Router(block))

    /**
     * Registers [PathHandler] for given route [path].
     *
     * @param path Route path for which the callback is to be registered.
     * @param router Nested [Router] handler.
     */
    fun path(path: String, router: Router) = path(Path(path), router)

    /**
     * Registers [PathHandler] for given route [path].
     *
     * @param path Route path for which the callback is to be registered.
     * @param block [Router] handler block.
     */
    fun path(path: String, block: Router.() -> Unit) = path(Path(path), Router(block))

    /**
     * Registers [ResourceHandler] for given [resource] URL for any route.
     *
     * @param resource The [URL] for which handler is to be registered.
     */
    fun get(resource: URL) {
        get("/*", resource)
    }

    /**
     * Registers [ResourceHandler] for given [resource] URL at given route [path].
     *
     * @param path Route path for which the handler is to be registered.
     * @param resource The [URL] for which the handler is to be registered.
     */
    fun get(path: String, resource: URL) {
        requestHandlers = requestHandlers + ResourceHandler(Route(Path(path), GET), resource)
    }

    /**
     * Registers [FileHandler] for given [file] URL for any route.
     *
     * @param file The [File] for which handler is to be registered.
     */
    fun get(file: File) {
        get("/*", file)
    }

    /**
     * Registers [FileHandler] for given [file] at given route [path].
     *
     * @param path Route path for which the handler is to be registered.
     * @param file The [File] for which the handler is to be registered.
     */
    fun get(path: String, file: File) {
        requestHandlers = requestHandlers + FileHandler(Route(Path(path), GET), file)
    }

    /**
     * Register CORS for the router.
     *
     * @param settings Instance of [CorsSettings].
     */
    fun cors(settings: CorsSettings) {
        before { simpleRequest(settings) }
        options("/") { preFlightRequest(settings) }
        options("/*") { preFlightRequest(settings) }
    }

    /**
     * Flattens the given [List] of [RequestHandler].
     * @param h [List] of [RequestHandler].
     * @return Flattened [List] of [RequestHandler].
     */
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
            val handlerPath = handler.route.path.pattern

            val finalPath =
                if (handlerPath == "/")
                    if (path.pattern == "/") "/" else path.pattern
                else
                    if (path.pattern == "/") handlerPath else handlerPath + path.pattern

            val nestedPath = path.copy(pattern = finalPath)
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
