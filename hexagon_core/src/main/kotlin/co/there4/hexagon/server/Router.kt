package co.there4.hexagon.server

import co.there4.hexagon.helpers.CodedException
import co.there4.hexagon.server.RequestHandler.*
import co.there4.hexagon.server.engine.PassException

import kotlin.reflect.KClass

import co.there4.hexagon.server.before as packageBefore
import co.there4.hexagon.server.after as packageAfter
import co.there4.hexagon.server.by as packageBy
import co.there4.hexagon.server.error as packageError
import co.there4.hexagon.server.assets as packageAssets

/**
 * TODO Document.
 * TODO Get routes (paths to be handled)
 * TODO Index paths (ie: /foo)
 * TODO Index routes (ie: GET /foo)
 * TODO Map with routes to all handlers needed
 */
class Router {
    var requestHandlers: List<RequestHandler> = emptyList(); private set

    infix fun Route.before(block: FilterCallback) { requestHandlers += this.packageBefore(block) }
    infix fun Route.after(block: FilterCallback) { requestHandlers += this.packageAfter(block) }
    infix fun Route.by(block: RouteCallback) { requestHandlers += this.packageBy(block) }

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

    fun error(code: Int, block: ErrorCodeCallback) { requestHandlers += packageError(code, block) }
    fun error(exception: KClass<out Exception>, block: ExceptionCallback) {
        error(exception.java, block)
    }

    fun error(exception: Class<out Exception>, block: ExceptionCallback) {
        val listOf = listOf(CodedException::class.java, PassException::class.java)
        require(exception !in listOf) { "${exception.name} is internal and must not be handled" }
        requestHandlers += ExceptionHandler(Route(Path("/"), ALL), exception, block)
    }

    infix fun Path.mount(handler: Router) { requestHandlers += PathHandler(Route(this), handler) }
    fun mount(handler: Router) { Path("/") mount handler }

    fun assets (resource: String, path: String = "/*") {
        requestHandlers += packageAssets(resource, path)
    }
}
