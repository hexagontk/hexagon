package com.hexagonkt.server.jetty

import com.hexagonkt.server.*
import com.hexagonkt.settings.SettingsManager

fun serve(
    settings: Map<String, *> = SettingsManager.settings,
    block: Router.() -> Unit): Server =
        serve(JettyServletAdapter(), settings, block)

fun server(
    settings: Map<String, *> = SettingsManager.settings,
    block: Router.() -> Unit): Server =
        server(JettyServletAdapter(), settings, block)
