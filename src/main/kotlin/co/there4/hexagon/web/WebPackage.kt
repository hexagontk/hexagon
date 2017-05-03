package co.there4.hexagon.web

import co.there4.hexagon.web.HttpMethod.*
import co.there4.hexagon.web.backend.IServer
import co.there4.hexagon.web.backend.servlet.JettyServletServer
import java.net.InetAddress.getByName as address

typealias FilterCallback = Exchange.() -> Unit
typealias RouteCallback = Exchange.() -> Any
typealias ErrorCallback = Exchange.(Exception) -> Any
typealias ErrorCodeCallback = Exchange.(Int) -> Any

val ALL = HttpMethod.values().toSet()

/** . */
fun server(
    backend: IServer=JettyServletServer(),
    block: Server.() -> Unit): Server =
        Server(backend).apply(block)

/** . */
fun serve(
    backend: IServer=JettyServletServer(),
    block: Server.() -> Unit): Server =
        server(backend, block).apply { run() }

/** Shortcut to create a route for a filter (with all methods). */
fun all (path: String = "/"): Route = Route(Path(path), ALL)
/** Shortcut to create a path (for adding routers). */
fun path (path: String = "/") = Path(path)

/** Shortcut to create a GET route. */
fun get (path: String = "/") = Route(Path(path), GET)
/** Shortcut to create a HEAD route. */
fun head (path: String = "/") = Route(Path(path), HEAD)
/** Shortcut to create a POST route. */
fun post (path: String = "/") = Route(Path(path), POST)
/** Shortcut to create a PUT route. */
fun put (path: String = "/") = Route(Path(path), PUT)
/** Shortcut to create a DELETE route. */
fun delete (path: String = "/") = Route(Path(path), DELETE)
/** Shortcut to create a TRACE route. */
fun tracer (path: String = "/") = Route(Path(path), TRACE)
/** Shortcut to create a OPTIONS route. */
fun options (path: String = "/") = Route(Path(path), OPTIONS)
/** Shortcut to create a PATCH route. */
fun patch (path: String = "/") = Route(Path(path), PATCH)

/** Shortcut to create a route from a method and a path. */
infix fun HttpMethod.at(path: String) = Route(Path(path), this)
/** Shortcut to create a route from a method and a path. */
infix fun Set<HttpMethod>.at(path: String) = Route(Path(path), this)
