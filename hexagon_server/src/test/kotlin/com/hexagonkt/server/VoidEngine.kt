package com.hexagonkt.server

internal object VoidEngine : ServerEngine {
    private var started = false

    override fun runtimePort() = 12345
    override fun started() = started
    override fun startup(server: Server, settings: Map<String, *>) { started = true }
    override fun shutdown() { started = false }
}
