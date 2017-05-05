package co.there4.hexagon.server

import co.there4.hexagon.server.HttpMethod.*
import co.there4.hexagon.server.backend.ServerEngine
import co.there4.hexagon.server.backend.servlet.JettyServletEngine
import java.net.InetAddress.getByName as address

typealias FilterCallback = Call.() -> Unit
typealias RouteCallback = Call.() -> Any
typealias ErrorCallback = Call.(Exception) -> Any
typealias ErrorCodeCallback = Call.(Int) -> Any

val ALL = HttpMethod.values().toSet()

/** . */
fun server(
    backend: ServerEngine = JettyServletEngine(),
    block: Router.() -> Unit): Server =
        Server(backend).apply { router.block() }

/** . */
fun serve(
    backend: ServerEngine = JettyServletEngine(),
    block: Router.() -> Unit): Server =
        server(backend, block).apply { run() }

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
infix fun Set<HttpMethod>.at(path: String) = Route(Path(path), this)
