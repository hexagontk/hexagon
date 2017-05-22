package co.there4.hexagon.server

import co.there4.hexagon.server.FilterOrder.AFTER
import co.there4.hexagon.server.FilterOrder.BEFORE
import co.there4.hexagon.server.HttpMethod.*
import co.there4.hexagon.server.RequestHandler.*
import co.there4.hexagon.server.engine.ServerEngine
import co.there4.hexagon.server.engine.servlet.JettyServletEngine
import co.there4.hexagon.settings.SettingsManager
import java.util.*
import kotlin.reflect.KClass

/** Alias for filters' callbacks. Functions executed before/after routes. */
typealias FilterCallback = Call.() -> Unit
/** Alias for routes' callbacks. Functions executed when a route is matched. */
typealias RouteCallback = Call.() -> Any
/** Alias for exceptions' callbacks. Functions executed when an exception is thrown. */
typealias ExceptionCallback = Call.(Exception) -> Any
/** Alias for errors' callbacks. Functions executed to handle a HTTP error code. */
typealias ErrorCodeCallback = Call.(Int) -> Any

/** Set containing all HTTP methods. */
val ALL: LinkedHashSet<HttpMethod> = linkedSetOf(*HttpMethod.values())

/**
 * Creates a server with a router. It is a combination of [Server] and [router].
 *
 * @param engine The server engine.
 * @param settings Server settings. Port and address will be searched in this map.
 * @param block Router's setup block.
 * @return A new server with the built router.
 */
fun server(
    engine: ServerEngine = JettyServletEngine(),
    settings: Map<String, *> = SettingsManager.settings,
    block: Router.() -> Unit): Server =
        Server(engine, settings, router(block))

/**
 * Creates and starts a server with a router. It is a combination of [Server] and [router].
 *
 * @param engine The server engine.
 * @param settings Server settings. Port and address will be searched in this map.
 * @param block Router's setup block.
 * @return The running server with the built router.
 */
fun serve(
    engine: ServerEngine = JettyServletEngine(),
    settings: Map<String, *> = SettingsManager.settings,
    block: Router.() -> Unit): Server =
        server(engine, settings, block).apply { run() }

/**
 * Creates and initializes a [Router] based on a code block.
 *
 * @param block Router's setup block.
 * @return A new router initialized by the passed block.
 */
fun router(block: Router.() -> Unit): Router = Router().apply { block() }

/** Shortcut to create a path (for adding routers). */
fun path(path: String = "/") = Path(path)

/** Shortcut to create a route for a filter (with all methods). */
fun all(path: String = "/"): Route = Route(Path(path), ALL)
/** Shortcut to create a GET route. */
fun get(path: String = "/") = Route(Path(path), GET)
/** Shortcut to create a HEAD route. */
fun head(path: String = "/") = Route(Path(path), HEAD)
/** Shortcut to create a POST route. */
fun post(path: String = "/") = Route(Path(path), POST)
/** Shortcut to create a PUT route. */
fun put(path: String = "/") = Route(Path(path), PUT)
/** Shortcut to create a DELETE route. */
fun delete(path: String = "/") = Route(Path(path), DELETE)
/** Shortcut to create a TRACE route. */
fun tracer(path: String = "/") = Route(Path(path), TRACE)
/** Shortcut to create a OPTIONS route. */
fun options(path: String = "/") = Route(Path(path), OPTIONS)
/** Shortcut to create a PATCH route. */
fun patch(path: String = "/") = Route(Path(path), PATCH)

/** Shortcut to create a route from a method and a path. */
infix fun HttpMethod.at(path: String) = Route(Path(path), this)
/** Shortcut to create a route from a method and a path. */
infix fun LinkedHashSet<HttpMethod>.at(path: String) = Route(Path(path), this)

infix fun Route.before(block: FilterCallback) = FilterHandler(this, BEFORE, block)
infix fun Route.after(block: FilterCallback) = FilterHandler(this, AFTER, block)
infix fun Route.by(block: RouteCallback) = RouteHandler(this, block)

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

fun error(code: Int, block: ErrorCodeCallback) = CodeHandler(Route(Path("/"), ALL), code, block)
fun error(exception: KClass<out Exception>, block: ExceptionCallback) = error(exception.java, block)
fun error(exception: Class<out Exception>, block: ExceptionCallback) =
    ExceptionHandler(all(), exception, block)

infix fun Path.handler(router: Router) = PathHandler(Route(this), router)
fun mount(path: Path, router: Router) = path handler router
fun mount(path: String, router: Router) = Path(path) handler router

fun assets(resource: String, path: String = "/*") = AssetsHandler(Route(Path(path), GET), resource)
