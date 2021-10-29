package com.hexagonkt.http.server

import com.hexagonkt.http.Protocol

internal object VoidAdapter : ServerPort {
    private var started = false

    override fun runtimePort() = 12345
    override fun started() = started
    override fun startup(server: Server) { started = true }
    override fun shutdown() { started = false }
    override fun supportedProtocols(): Set<Protocol> = Protocol.values().toSet()
    override fun supportedFeatures(): Set<ServerFeature> = ServerFeature.values().toSet()
    override fun supportedOptions(): Set<String> = emptySet()
}
