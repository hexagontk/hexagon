package co.there4.hexagon.web

import co.there4.hexagon.settings.SettingsManager.setting
import co.there4.hexagon.util.err
import co.there4.hexagon.util.resource
import co.there4.hexagon.web.HttpMethod.GET
import co.there4.hexagon.web.HttpMethod.*
import co.there4.hexagon.web.servlet.JettyServletServer
import java.net.InetAddress
import kotlin.reflect.KClass

import java.net.InetAddress.getByName as address

typealias Handler = Exchange.() -> Unit
typealias ParameterHandler<T> = Exchange.(T) -> Unit

/** Port from config. */
val bindPort = setting<Int>("bindPort") ?: 2010
/** Address from config. */
val bindAddress: InetAddress = address(setting<String>("bindAddress") ?: "localhost") ?: err

val resourcesFolder = setting<String>("resourcesFolder") ?: "public"

val processResources = resource(resourcesFolder) != null

/** Default server. Used by package methods. */
var server: Server = JettyServletServer(bindPort = bindPort, bindAddress = bindAddress)
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

/** @see Router.notFound */
fun notFound(block: Handler) = server.notFound(block)
/** @see Router.internalError */
fun internalError(block: ParameterHandler<Exception>) = server.internalError(block)

/** @see Router.error */
fun error(exception: Class<out Exception>, block: ParameterHandler<Exception>) =
    server.error (exception, block)

/** @see Router.error */
fun error(exception: KClass<out Exception>, block: ParameterHandler<Exception>) =
    server.error (exception, block)

/** @see Router.reset */
fun reset() = server.reset()

