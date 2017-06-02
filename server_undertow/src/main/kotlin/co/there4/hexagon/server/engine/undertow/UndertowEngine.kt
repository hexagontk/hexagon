package co.there4.hexagon.server.engine.servlet

import co.there4.hexagon.server.Server
import co.there4.hexagon.server.ServerEngine
import java.net.InetAddress.getByName as address

class UndertowEngine : ServerEngine {
    override fun runtimePort(): Int = 0
    override fun started() = false
    override fun startup(server: Server, settings: Map<String, *>) {}
    override fun shutdown() {}
}
