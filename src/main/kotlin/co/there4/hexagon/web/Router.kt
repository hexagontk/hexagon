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
    val errors: MutableMap<Class<out Exception>, ParameterHandler<Exception>> = LinkedHashMap ()) {

    companion object : CachedLogger(Router::class)

    /** TODO Make assets work like a get route to an static file serving handler */
    var assets: List<String> = listOf(); private set

    var notFoundHandler: Handler = { error(404, request.url + " not found") }; private set
    private var errorHandler: ErrorHandler = { e -> error(500, e.toText()) }

    fun after(path: String = "/*", block: Handler) = addFilter(path, AFTER, block)
    fun before(path: String = "/*", block: Handler) = addFilter (path, BEFORE, block)

    fun get(path: String = "/", block: Handler) = get(path) by block
    fun head(path: String = "/", block: Handler) = head(path) by block
    fun post(path: String = "/", block: Handler) = post(path) by block
    fun put(path: String = "/", block: Handler) = put(path) by block
    fun delete(path: String = "/", block: Handler) = delete(path) by block
    fun trace(path: String = "/", block: Handler) = tracer(path) by block
    fun options(path: String = "/", block: Handler) = options(path) by block
    fun patch(path: String = "/", block: Handler) = patch(path) by block

    internal fun handle(
        exception: Exception, exchange: Exchange, type: Class<*> = exception.javaClass) {

        val handler = errors[type]

        if (handler != null)
            exchange.(handler)(exception)
        else
            type.superclass.also {
                if (it != null) handle(exception, exchange, it)
                else exchange.errorHandler(exception)
            }
    }

    fun assets (path: String) { assets += path }

    fun error(exception: Class<out Exception>, block: ErrorHandler) = errors.put (exception, block)
    fun error(exception: KClass<out Exception>, block: ErrorHandler) = error (exception.java, block)

    private fun addFilter(path: String, order: FilterOrder, block: Handler) {
        val filter = Filter (Path (path), order)
        require(!filters.containsKey(filter)) { "$order $path Filter is already added" }
        filters.put (filter, block)
        info ("$order $path Filter ADDED")
    }

    fun reset() {
        filters.clear()
        routes.clear()
        errors.clear()
        assets = listOf()
    }

    fun Route.handler(handler: Handler) {
        require (!routes.containsKey(this)) { "$method $path Route is already added" }
        routes.put (this, handler)
        info ("$method $path Route ADDED")
    }

    infix fun Route.by(handler: Handler) {
        this.handler(handler)
    }
}
