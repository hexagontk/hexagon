package com.hexagonkt.store

import com.hexagonkt.server.Server
import com.hexagonkt.server.ServerPort

/**
 * TODO Replace with TestEngine
 */
object VoidEngine : ServerPort {
    var started = false

    override fun runtimePort() = 12345
    override fun started() = started
    override fun startup(server: Server, settings: Map<String, *>) { started = true }
    override fun shutdown() { started = false }
}
