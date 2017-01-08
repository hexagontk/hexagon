package co.there4.hexagon.web

import co.there4.hexagon.settings.SettingsManager.setting
import co.there4.hexagon.util.err
import co.there4.hexagon.web.servlet.JettyServletServer
import java.net.InetAddress
import kotlin.reflect.KClass

import java.net.InetAddress.getByName as address

/*
 * TODO Add base class for Application (setup locales, etc.) for web applications
 * TODO Add base class for Services (the setup for applications is not needed here)
 * TODO Add initialization for applications and services
 * TODO Setup metrics
 */

/** Port from config. */
val bindPort = setting<Int>("bindPort") ?: 2010
/** Address from config. */
val bindAddress: InetAddress = address(setting<String>("bindAddress") ?: "localhost") ?: err

/** Default server. Used by package methods. */
var server: Server = JettyServletServer(bindPort = bindPort, bindAddress = bindAddress)
    get () = field
    set (server) {
        if (field.started ())
            error("A default server is already started")

        field = server
    }

/** @see Server.run */
fun run() = server.run()
/** @see Server.stop */
fun stop() = server.stop()

/** @see Router.assets */
fun assets (path: String) = server.assets (path)

/** @see Router.after */
fun after (path: String = "/*", block: Exchange.() -> Unit) = server.after (path, block)
/** @see Router.before */
fun before (path: String = "/*", block: Exchange.() -> Unit) = server.before (path, block)

/** @see Router.get */
fun get (path: String = "/", block: Exchange.() -> Unit) = server.get (path, block)
/** @see Router.head */
fun head (path: String = "/", block: Exchange.() -> Unit) = server.head (path, block)
/** @see Router.post */
fun post (path: String = "/", block: Exchange.() -> Unit) = server.post (path, block)
/** @see Router.put */
fun put (path: String = "/", block: Exchange.() -> Unit) = server.put (path, block)
/** @see Router.delete */
fun delete (path: String = "/", block: Exchange.() -> Unit) = server.delete (path, block)
/** @see Router.trace */
fun trace (path: String = "/", block: Exchange.() -> Unit) = server.trace (path, block)
/** @see Router.options */
fun options (path: String = "/", block: Exchange.() -> Unit) = server.options (path, block)
/** @see Router.patch */
fun patch (path: String = "/", block: Exchange.() -> Unit) = server.patch (path, block)

/** @see Router.notFound */
fun notFound(block: Exchange.() -> Unit) = server.notFound(block)
/** @see Router.internalError */
fun internalError(block: Exchange.(e: Exception) -> Unit) = server.internalError(block)

/** @see Router.err */
fun error(exception: Class<out Exception>, block: Exchange.(e: Exception) -> Unit) =
    server.error (exception, block)

/** @see Router.err */
fun error(exception: KClass<out Exception>, block: Exchange.(e: Exception) -> Unit) =
    server.error (exception, block)

/** @see Router.reset */
fun reset() = server.reset()
