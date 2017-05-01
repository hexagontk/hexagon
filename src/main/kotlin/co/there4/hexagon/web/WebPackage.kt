package co.there4.hexagon.web

import co.there4.hexagon.settings.SettingsManager.setting
import co.there4.hexagon.web.HttpMethod.*
import co.there4.hexagon.web.backend.IServer
import co.there4.hexagon.web.backend.servlet.JettyServletServer
import java.net.InetAddress.getByName as address

/**
 * Unit -> 200 <empty>
 * Int -> <status> <empty>
 * String -> 200 body
 * Pair (403 to "Forbidden") -> <code> <body>
 * Map -> serialize with "accept" or default format
 * List -> serialize with "accept header", "response.contentType" or default format
 * Stream -> streaming
 */
typealias FilterHandler = Exchange.() -> Unit
typealias Handler = Exchange.() -> Any
typealias ParameterHandler<T> = Exchange.(T) -> Unit
typealias ErrorHandler = ParameterHandler<Exception>

@Deprecated("Replaced by `assets` router method")
val resourcesFolder = setting<String>("resourcesFolder") ?: "public"

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

/** Syntactic sugar to ease the definition of handler methods. */
fun Exchange.handler(block: Handler): Any = this.block()
