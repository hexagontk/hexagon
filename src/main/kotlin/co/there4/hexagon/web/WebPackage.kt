package co.there4.hexagon.web

import co.there4.hexagon.settings.SettingsManager.setting
import co.there4.hexagon.web.HttpMethod.*
import co.there4.hexagon.web.backend.IServer
import co.there4.hexagon.web.backend.servlet.JettyServletServer
import kotlin.reflect.KClass
import java.net.InetAddress.getByName as address

typealias Handler = Exchange.() -> Unit
typealias ParameterHandler<T> = Exchange.(T) -> Unit
typealias ErrorHandler = ParameterHandler<Exception>

@Deprecated("Replaced by `assets` router method")
val resourcesFolder = setting<String>("resourcesFolder") ?: "public"

/** Default static server. Used by this package's helper methods. */
var server: Server = Server(JettyServletServer())
    get () = field
    set (server) {
        check (!field.started ()) { "A default server is already started" }
        field = server
    }

/** . */
fun server(backend: IServer=JettyServletServer(), block: Server.() -> Unit): Server =
    Server(backend).apply(block)

/** . */
fun serve(backend: IServer=JettyServletServer(), block: Server.() -> Unit): Server =
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
fun Exchange.handler(block: Handler): Unit = this.block()

/** @see Server.run */
fun run() = server.run()
/** @see Server.stop */
fun stop() = server.stop()

/** @see Router.assets */
fun assets (resource: String, path: String = "/") = server.assets (resource, path)

/** @see Router.after */
fun after (path: String = "/*", block: Handler) = server.after (path, block)
/** @see Router.before */
fun before (path: String = "/*", block: Handler) = server.before (path, block)

/** @see Router.get */
fun get (path: String = "/", block: Handler) = server.get (path, block)
/** @see Router.head */
fun head (path: String = "/", block: Handler) = server.head (path, block)
/** @see Router.post */
fun post (path: String = "/", block: Handler) = server.post (path, block)
/** @see Router.put */
fun put (path: String = "/", block: Handler) = server.put (path, block)
/** @see Router.delete */
fun delete (path: String = "/", block: Handler) = server.delete (path, block)
/** @see Router.trace */
fun trace (path: String = "/", block: Handler) = server.trace (path, block)
/** @see Router.options */
fun options (path: String = "/", block: Handler) = server.options (path, block)
/** @see Router.patch */
fun patch (path: String = "/", block: Handler) = server.patch (path, block)

/** @see Router.error */
fun error(code: Int, block: ParameterHandler<Int>) = server.error(code, block)
/** @see Router.error */
fun error(exception: Class<out Exception>, block: ErrorHandler) = server.error(exception, block)
/** @see Router.error */
fun error(exception: KClass<out Exception>, block: ErrorHandler) = server.error(exception, block)

/** @see Router.reset */
fun reset() = server.reset()
