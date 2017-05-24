package co.there4.hexagon.server

import co.there4.hexagon.server.engine.ServerEngine

object VoidEngine : ServerEngine {
    var started = false

    override fun runtimePort() = 12345
    override fun started() = started
    override fun startup(server: Server, settings: Map<String, *>) { started = true }
    override fun shutdown() { started = false }
}
