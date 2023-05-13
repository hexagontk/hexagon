package com.hexagonkt.http.server.async

import com.hexagonkt.http.model.HttpProtocol
import com.hexagonkt.http.model.HttpProtocol.H2C
import com.hexagonkt.http.server.HttpServerFeature
import com.hexagonkt.http.server.HttpServerFeature.SSE

internal object VoidAdapter : HttpServerPort {
    private var started = false

    override fun runtimePort() = 12345
    override fun started() = started
    override fun startUp(server: HttpServer) { started = true }
    override fun shutDown() { started = false }
    override fun supportedProtocols(): Set<HttpProtocol> = HttpProtocol.values().toSet() - H2C
    override fun supportedFeatures(): Set<HttpServerFeature> = setOf(SSE)
    override fun options(): Map<String, *> = mapOf("option1" to 1, "option2" to 2)
}
