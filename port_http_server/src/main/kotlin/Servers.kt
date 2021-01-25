package com.hexagonkt.http.server

import com.hexagonkt.helpers.CodedException
import com.hexagonkt.injection.InjectionManager

/** Alias for routes' and filters' callbacks. Functions executed when a route is matched. */
typealias RouteCallback = Call.() -> Unit

/** Alias for exceptions' callbacks. Functions executed when an exception is thrown. */
typealias ExceptionCallback = Call.(Exception) -> Unit

/** Alias for errors' callbacks. Functions executed to handle a HTTP error code. */
typealias ErrorCodeCallback = Call.(CodedException) -> Unit

/**
 * Create a server and start it.
 *
 * @param settings Server settings info .
 * @param router [Router] instance.
 * @param adapter Adapter instance which implements [ServerPort].
 *
 * @return The started [Server] instance.
 */
fun serve(
    settings: ServerSettings = ServerSettings(),
    router: Router,
    adapter: ServerPort = InjectionManager.inject()): Server =
        Server(adapter, router, settings).apply { start() }

/**
 * Create a server and start it.
 *
 * @param settings Server settings info.
 * @param adapter Adapter instance which implements [ServerPort].
 * @param block Lambda to be used to create a [Router] instance.
 *
 * @return The started [Server] instance.
 */
fun serve(
    settings: ServerSettings = ServerSettings(),
    adapter: ServerPort = InjectionManager.inject(),
    block: Router.() -> Unit): Server =
        Server(adapter, Router(block), settings).apply { start() }
