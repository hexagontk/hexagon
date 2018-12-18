package com.hexagonkt.http.server

import com.hexagonkt.http.Method
import com.hexagonkt.http.Method.*
import com.hexagonkt.http.Path
import com.hexagonkt.http.Route
import java.util.*

/** Alias for filters' callbacks. Functions executed before/after routes. */
typealias FilterCallback = Call.() -> Unit
/** Alias for routes' callbacks. Functions executed when a route is matched. */
typealias RouteCallback = Call.() -> Any
/** Alias for exceptions' callbacks. Functions executed when an exception is thrown. */
typealias ExceptionCallback = Call.(Exception) -> Any
/** Alias for errors' callbacks. Functions executed to handle a HTTP error code. */
typealias ErrorCodeCallback = Call.(Int) -> Any

/** Set containing all HTTP methods. */
val ALL: LinkedHashSet<Method> = linkedSetOf(*Method.values())

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
fun get(path: String = "/"): Route = Route(Path(path), GET)
/** Shortcut to create a HEAD route. */
fun head(path: String = "/"): Route = Route(Path(path), HEAD)
/** Shortcut to create a POST route. */
fun post(path: String = "/"): Route = Route(Path(path), POST)
/** Shortcut to create a PUT route. */
fun put(path: String = "/"): Route = Route(Path(path), PUT)
/** Shortcut to create a DELETE route. */
fun delete(path: String = "/"): Route = Route(Path(path), DELETE)
/** Shortcut to create a TRACE route. */
fun tracer(path: String = "/"): Route = Route(Path(path), TRACE)
/** Shortcut to create a OPTIONS route. */
fun options(path: String = "/"): Route = Route(Path(path), OPTIONS)
/** Shortcut to create a PATCH route. */
fun patch(path: String = "/"): Route = Route(Path(path), PATCH)
