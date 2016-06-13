package co.there4.hexagon.web

import co.there4.hexagon.web.jetty.JettyServer
import kotlin.reflect.KClass

/** Default server. Used by package methods. */
var blacksheep: Server = JettyServer ()
    get () = field
    set (server) {
        if (field.started ())
            throw IllegalStateException ("A default server is already started")

        field = server
    }

/** @see Server.run */
fun run() = blacksheep.run()
/** @see Server.stop */
fun stop() = blacksheep.stop()

/** @see Router.assets */
fun assets (path: String) = blacksheep.assets (path)

/** @see Router.after */
fun after (path: String = "/*", block: Exchange.() -> Unit) = blacksheep.after (path, block)
/** @see Router.before */
fun before (path: String = "/*", block: Exchange.() -> Unit) = blacksheep.before (path, block)

/** @see Router.get */
fun get (path: String = "/", block: Exchange.() -> Unit) = blacksheep.get (path, block)
/** @see Router.head */
fun head (path: String = "/", block: Exchange.() -> Unit) = blacksheep.head (path, block)
/** @see Router.post */
fun post (path: String = "/", block: Exchange.() -> Unit) = blacksheep.post (path, block)
/** @see Router.put */
fun put (path: String = "/", block: Exchange.() -> Unit) = blacksheep.put (path, block)
/** @see Router.delete */
fun delete (path: String = "/", block: Exchange.() -> Unit) = blacksheep.delete (path, block)
/** @see Router.trace */
fun trace (path: String = "/", block: Exchange.() -> Unit) = blacksheep.trace (path, block)
/** @see Router.options */
fun options (path: String = "/", block: Exchange.() -> Unit) = blacksheep.options (path, block)
/** @see Router.patch */
fun patch (path: String = "/", block: Exchange.() -> Unit) = blacksheep.patch (path, block)

/** @see Router.error */
fun error(exception: Class<out Exception>, block: Exchange.(e: Exception) -> Unit) =
    blacksheep.error (exception, block)

/** @see Router.error */
fun error(exception: KClass<out Exception>, block: Exchange.(e: Exception) -> Unit) =
    blacksheep.error (exception, block)
