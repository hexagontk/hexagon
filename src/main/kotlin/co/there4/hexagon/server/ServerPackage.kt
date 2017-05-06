package co.there4.hexagon.server

import co.there4.hexagon.server.HttpMethod.*
import co.there4.hexagon.server.engine.ServerEngine
import co.there4.hexagon.server.engine.servlet.JettyServletEngine

/** Shortcut for filters' callbacks. Functions executed before/after routes. */
typealias FilterCallback = Call.() -> Unit
/** Shortcut for routes' callbacks. Functions executed when a route is matched. */
typealias RouteCallback = Call.() -> Any
/** Shortcut for exceptions' callbacks. Functions executed when an exception is thrown. */
typealias ErrorCallback = Call.(Exception) -> Any
/** Shortcut for errors' callbacks. Functions executed to handle a HTTP error code. */
typealias ErrorCodeCallback = Call.(Int) -> Any

/** Set containing all HTTP methods. */
val ALL: Set<HttpMethod> = HttpMethod.values().toSet()

/** TODO . */
fun server(
    backend: ServerEngine = JettyServletEngine(),
    block: Router.() -> Unit): Server =
        Server(backend, router = router(block))

/** TODO . */
fun serve(
    backend: ServerEngine = JettyServletEngine(),
    block: Router.() -> Unit): Server =
        server(backend, block).apply { run() }

/** . */
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
infix fun Set<HttpMethod>.at(path: String) = Route(Path(path), this)
