package co.there4.hexagon.web

import co.there4.hexagon.util.CachedLogger
import co.there4.hexagon.util.CodedException
import co.there4.hexagon.web.FilterOrder.AFTER
import co.there4.hexagon.web.FilterOrder.BEFORE
import java.util.*
import kotlin.reflect.KClass

/**
 * TODO Compose routers with a map of context to router (children)
 * TODO val children: MutableMap<String, Router> = LinkedHashMap()
 * TODO Add get<Request, Response>("/path") {} (for all methods)
 */
open class Router(
    val filters: MutableMap<Filter, Handler> = LinkedHashMap (),
    val routes: MutableMap<Route, Handler> = LinkedHashMap ()) {

    companion object : CachedLogger(Router::class)

    private val notFoundHandler: ParameterHandler<Int> = { error(404, request.url + " not found") }

    private val baseExceptionHandler: ErrorHandler = {
        error(500, "${it.javaClass.simpleName} (${it.message ?: "no details"})")
    }

    private val codedExceptionHandler: ErrorHandler = {
        if (it is CodedException) {
            val handler: ParameterHandler<Int> = codedErrors[it.code] ?: { code ->
                error(code, it.message ?: "")
            }
            handler(it.code)
        }
        else {
            error(500, "WTF")
        }
    }

    val codedErrors: MutableMap<Int, ParameterHandler<Int>> = linkedMapOf(404 to notFoundHandler)

    val errors: MutableMap<Class<out Exception>, ErrorHandler> = linkedMapOf(
       CodedException::class.java to codedExceptionHandler,
       Exception::class.java to baseExceptionHandler
    )

    /** TODO Make assets work like a get route to an static file serving handler */
    var assets: List<String> = listOf(); private set

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

    internal fun handle(error: Exception, exchange: Exchange, type: Class<*> = error.javaClass) {
        error("Error processing request", error)

        val handler = errors[type]

        if (handler != null)
            exchange.handler(error)
        else
            type.superclass.also {
                if (it != null) handle(error, exchange, it)
                else exchange.baseExceptionHandler(error) // This handler is added before
            }
    }

    fun assets (resource: String, path: String = "/") { assets += resource }

    fun error(code: Int, block: ParameterHandler<Int>) { codedErrors[code] = block }
    fun error(exception: KClass<out Exception>, block: ErrorHandler) = error (exception.java, block)
    fun error(exception: Class<out Exception>, block: ErrorHandler) {
        val listOf = listOf(
            CodedException::class.java,
            EndException::class.java,
            PassException::class.java
        )
        require(exception !in listOf) { "${exception.name} is internal and must not be handled" }
        errors.put (exception, block)
    }

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
