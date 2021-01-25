package com.hexagonkt.http.server

import com.hexagonkt.http.Protocol

/**
 * Server instance of one kind.
 */
interface ServerPort {

    /**
     * Get the runtime port if started, throw an exception otherwise.
     */
    fun runtimePort(): Int

    /**
     * Check whether the server has been started or not.
     *
     * @return True if the server has started, else false.
     */
    fun started(): Boolean

    /**
     * Build a server of a certain engine from a server definition and runs it.
     */
    fun startup(server: Server)

    /**
     * Stop the instance of the engine.
     */
    fun shutdown()

    /**
     * Return the server adapter's supported protocols.
     *
     * @return Set of supported protocols.
     */
    fun supportedProtocols(): Set<Protocol>

    /**
     * Return the server adapter's supported features.
     *
     * @return Set of supported features.
     */
    fun supportedFeatures(): Set<ServerFeature>

    /**
     * Return the server adapter's allowed configuration options.
     *
     * @return Set of supported options.
     */
    fun supportedOptions(): Set<String>
}
