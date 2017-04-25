package co.there4.hexagon.web

import co.there4.hexagon.settings.SettingsManager.setting
import co.there4.hexagon.web.HttpMethod.*
import co.there4.hexagon.web.backend.servlet.JettyServletServer
import kotlin.reflect.KClass
import java.net.InetAddress.getByName as address

typealias Handler = Exchange.() -> Unit
typealias ParameterHandler<T> = Exchange.(T) -> Unit
typealias ErrorHandler = ParameterHandler<Exception>

val resourcesFolder = setting<String>("resourcesFolder") ?: "public"

/** Default server. Used by package methods. */
var server: Server = Server(JettyServletServer())
    get () = field
    set (server) {
        if (field.started ())
            error("A default server is already started")

        field = server
    }

fun get (path: String = "/") = Route(Path(path), GET)
fun head (path: String = "/") = Route(Path(path), HEAD)
fun post (path: String = "/") = Route(Path(path), POST)
fun put (path: String = "/") = Route(Path(path), PUT)
fun delete (path: String = "/") = Route(Path(path), DELETE)
fun tracer (path: String = "/") = Route(Path(path), TRACE)
fun options (path: String = "/") = Route(Path(path), OPTIONS)
fun patch (path: String = "/") = Route(Path(path), PATCH)
infix fun HttpMethod.at(path: String) = Route(Path(path), this)

fun Exchange.handler(block: Handler): Unit = this.block()

/** @see Server.run */
fun run() = server.run()
/** @see Server.stop */
fun stop() = server.stop()

/** @see Router.assets */
fun assets (path: String) = server.assets (path)

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
fun error(exception: Class<out Exception>, block: ErrorHandler) = server.error(exception, block)
/** @see Router.error */
fun error(exception: KClass<out Exception>, block: ErrorHandler) = server.error(exception, block)

/** @see Router.reset */
fun reset() = server.reset()
