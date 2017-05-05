package co.there4.hexagon.server

import co.there4.hexagon.helpers.CachedLogger
import co.there4.hexagon.helpers.CodedException
import co.there4.hexagon.server.FilterOrder.AFTER
import co.there4.hexagon.server.FilterOrder.BEFORE
import co.there4.hexagon.server.HttpMethod.GET
import co.there4.hexagon.server.RequestHandler.*
import co.there4.hexagon.server.backend.EndException
import co.there4.hexagon.server.backend.PassException
import kotlin.reflect.KClass

/**
 * .
 */
class Router {
    private companion object : CachedLogger(Router::class)

    private val notFoundHandler: ErrorCodeCallback = { error(404, "${request.url} not found") }
    private val baseExceptionHandler: ErrorCallback =
        { error(500, "${it.javaClass.simpleName} (${it.message ?: "no details"})") }

    private val defaultHandlers = listOf(
        ErrorCodeHandler(Route(Path("/"), ALL), 404, notFoundHandler),
        ErrorHandler(Route(Path("/"), ALL), Exception::class.java, baseExceptionHandler)
    )

    var requestHandlers: List<RequestHandler> = defaultHandlers; private set
    var codedErrors: Map<Int, ErrorCodeCallback> = codedErrors(); private set
    var exceptionErrors: Map<Class<out Exception>, ErrorCallback> = exceptionErrors(); private set

    private fun codedErrors(): Map<Int, ErrorCodeCallback> = requestHandlers
        .filterIsInstance(ErrorCodeHandler::class.java)
        .map { it.code to it.handler }
        .toMap()

    private fun exceptionErrors(): Map<Class<out Exception>, ErrorCallback> = requestHandlers
        .filterIsInstance(ErrorHandler::class.java)
        .map { it.exception to it.handler }
        .toMap()

    fun reset() {
        requestHandlers = defaultHandlers
        codedErrors = codedErrors()
        exceptionErrors = exceptionErrors()
    }

    fun Route.handler(order: FilterOrder, block: FilterCallback) {
        requestHandlers += FilterHandler(this, order, block)
        info ("$order ${this.path.path} Filter ADDED")
    }

    fun Route.handler(handler: RouteCallback) {
        requestHandlers += RouteHandler(this, handler)
        info ("$method $path Route ADDED")
    }

    fun Path.mount(handler: Router) {
        requestHandlers += PathHandler(Route(this), handler)
        info ("Router $path Route ADDED")
    }

    infix fun Route.before(handler: FilterCallback) { this.handler(BEFORE, handler) }
    infix fun Route.after(handler: FilterCallback) { this.handler(AFTER, handler) }

    infix fun Route.by(handler: RouteCallback) { this.handler(handler) }

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

    fun assets (resource: String, path: String = "/") {
        requestHandlers += AssetsHandler(Route(Path(path), GET), resource)
    }

    fun error(code: Int, block: ErrorCodeCallback) {
        codedErrors += code to block
        requestHandlers += ErrorCodeHandler(Route(Path("/"), ALL), code, block)
    }

    fun error(exception: KClass<out Exception>, block: ErrorCallback) = error (exception.java, block)
    fun error(exception: Class<out Exception>, block: ErrorCallback) {
        val listOf = listOf(
            CodedException::class.java,
            EndException::class.java,
            PassException::class.java
        )
        require(exception !in listOf) { "${exception.name} is internal and must not be handled" }
        exceptionErrors += exception to block
        requestHandlers += ErrorHandler(Route(Path("/"), ALL), exception, block)
    }

    internal fun handleError(error: Exception, ex: Call, type: Class<*> = error.javaClass) {
        when (error) {
            is EndException -> trace("Request processing ended by callback request")
            is CodedException -> {
                val handler: ErrorCodeCallback =
                    codedErrors[error.code] ?: { error(it, error.message ?: "") }
                ex.handler(error.code)
            }
            else -> {
                error("Error processing request", error)

                val handler = exceptionErrors[type]

                if (handler != null)
                    ex.handler(error)
                else
                    type.superclass.also {
                        if (it != null) handleError(error, ex, it)
                        else ex.baseExceptionHandler(error) // This handler is added before
                    }
            }
        }
    }

    private fun createResourceHandler(resourcesFolder: String): RouteCallback = {
        val path = if (request.path.isEmpty()) request.path else request.path
        val resourcePath = "/$resourcesFolder$path"
        val stream = javaClass.getResourceAsStream(resourcePath)

        if (stream == null)
            pass()
        else {
            val contentType by lazy { response.getMimeType(path) }

            // Should be done BEFORE flushing the stream (if not content type is ignored)
            if (response.contentType == null && contentType != null)
                response.contentType = contentType

            trace("Resource for '$resourcePath' (${response.contentType}) found and returned")
            val bytes = stream.readBytes()
            response.outputStream.write(bytes)
            response.outputStream.flush()
        }
    }
}
