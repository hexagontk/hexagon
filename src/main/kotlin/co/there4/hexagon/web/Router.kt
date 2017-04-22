package co.there4.hexagon.web

import java.util.*
import co.there4.hexagon.web.FilterOrder.*
import co.there4.hexagon.util.CachedLogger
import co.there4.hexagon.util.toText
import kotlin.reflect.KClass

/**
 * TODO Compose routers with a map of context to router (children)
 * TODO val children: MutableMap<String, Router> = LinkedHashMap()
 * TODO Add warning in Hexagon when paths DO NOT start with '/' or contains ':' (bad formats)
 * TODO Add get<Request, Response>("/path") {} (for all methods)
 */
open class Router(
    val filters: MutableMap<Filter, Handler> = LinkedHashMap (),
    val routes: MutableMap<Route, Handler> = LinkedHashMap (),
    val assets: MutableList<String> = ArrayList (),
    val errors: MutableMap<Class<out Exception>, ParameterHandler<Exception>> = LinkedHashMap ()) {

    companion object : CachedLogger(Router::class)

    var notFoundHandler: Handler = { error(404, request.url + " not found") }
        private set

    private var errorHandler: ParameterHandler<Exception> = { e -> error(500, e.toText()) }

    fun after(path: String = "/*", block: Handler) = addFilter(path, AFTER, block)
    fun before(path: String = "/*", block: Handler) = addFilter (path, BEFORE, block)

    fun get(path: String = "/", block: Handler) = on(get(path), block)
    fun head(path: String = "/", block: Handler) = on(head(path), block)
    fun post(path: String = "/", block: Handler) = on(post(path), block)
    fun put(path: String = "/", block: Handler) = on(put(path), block)
    fun delete(path: String = "/", block: Handler) = on(delete(path), block)
    fun trace(path: String = "/", block: Handler) = on(tracer(path), block)
    fun options(path: String = "/", block: Handler) = on(options(path), block)
    fun patch(path: String = "/", block: Handler) = on(patch(path), block)

    fun notFound(block: Handler) { notFoundHandler = block }
    fun internalError(block: ParameterHandler<Exception>) { errorHandler = block }

    internal fun handle(exception: Exception, exchange: Exchange) {
        val handler = errors[exception.javaClass]

        if (handler != null)
            exchange.(handler)(exception)
        else
            exchange.errorHandler(exception)
    }

    fun assets (path: String) = assets.add (path)

    fun error(exception: Class<out Exception>, callback: ParameterHandler<Exception>) =
        errors.put (exception, callback)

    fun error(exception: KClass<out Exception>, callback: ParameterHandler<Exception>) =
        error (exception.java, callback)

    private fun addFilter(path: String, order: FilterOrder, block: Handler) {
        val filter = Filter (Path (path), order)

        // TODO Use require
        if (filters.containsKey(filter))
            throw IllegalArgumentException ("$order $path Filter is already added")

        filters.put (filter, block)
        info ("$order $path Filter ADDED")
    }

    fun on(route: Route, block: Handler) {
        // TODO Use require
        if (routes.containsKey(route))
            throw IllegalArgumentException ("${route.method} ${route.path} Route is already added")

        routes.put (route, block)
        info ("${route.method} ${route.path} Route ADDED")
    }

    fun reset() {
        filters.clear()
        routes.clear()
        assets.clear()
        errors.clear()
    }

    fun Route.handler(handler: Handler) {
        on(this, handler)
    }

    infix fun Route.by(handler: Handler) {
        on(this, handler)
    }

    operator fun Route.plus(handler: Handler) {
        on(this, handler)
    }
}
