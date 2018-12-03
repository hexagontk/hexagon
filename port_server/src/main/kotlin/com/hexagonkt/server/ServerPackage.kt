package com.hexagonkt.server

import com.hexagonkt.http.HttpMethod
import com.hexagonkt.http.HttpMethod.*
import com.hexagonkt.settings.SettingsManager
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
    engine: ServerPort,
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
    engine: ServerPort,
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

/** Shortcut to create a route from a method and a path. */
infix fun HttpMethod.at(path: String): Route = Route(Path(path), this)
/** Shortcut to create a route from a method and a path. */
infix fun LinkedHashSet<HttpMethod>.at(path: String): Route = Route(Path(path), this)
