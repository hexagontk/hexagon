package com.hexagonkt.http.server.async

import com.hexagonkt.http.model.HttpProtocol
import com.hexagonkt.http.server.HttpServerFeature

/**
 * Server instance of one kind.
 */
interface HttpServerPort {

    /**
     * Get the runtime port if started, throw an exception otherwise.
     *
     * @return Runtime port if started or an exception otherwise.
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
     *
     * @param server The server instance to be run by this adapter.
     */
    fun startUp(server: HttpServer)

    /**
     * Stop the instance of the engine.
     */
    fun shutDown()

    /**
     * Return the server adapter's supported protocols.
     *
     * @return Set of supported protocols.
     */
    fun supportedProtocols(): Set<HttpProtocol>

    /**
     * Return the server adapter's supported features.
     *
     * @return Set of supported features.
     */
    fun supportedFeatures(): Set<HttpServerFeature>

    /**
     * Return the adapter's allowed configuration options with its values.
     *
     * @return Map of supported options with their values for this instance.
     */
    fun options(): Map<String, *>
}
