package co.there4.hexagon.rest.ratpack

import ratpack.server.RatpackServer

fun serverOf(cb: KServerSpec.() -> Unit) = RatpackServer.of { KServerSpec(it).(cb)() }
fun serverStart(cb: KServerSpec.() -> Unit) = RatpackServer.start { KServerSpec(it).(cb)() }
