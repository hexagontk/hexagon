package com.hexagonkt.http.server.jetty

import com.hexagonkt.http.server.Router
import com.hexagonkt.http.server.Server
import com.hexagonkt.http.server.ServerSettings

/**
 * Create a Jetty server and start it. It is a shortcut to avoid passing or injecting the adapter.
 *
 * @param settings Server settings info .
 * @param router [Router] instance.
 *
 * @return The started [Server] instance.
 */
fun serve(settings: ServerSettings = ServerSettings(), router: Router): Server =
    Server(JettyServletAdapter(), router, settings).apply { start() }

/**
 * Create a Jetty server and start it. It is a shortcut to avoid passing or injecting the adapter.
 *
 * @param settings Server settings info.
 * @param block Lambda to be used to create a [Router] instance.
 *
 * @return The started [Server] instance.
 */
fun serve(settings: ServerSettings = ServerSettings(), block: Router.() -> Unit): Server =
    Server(JettyServletAdapter(), Router(block), settings).apply { start() }
