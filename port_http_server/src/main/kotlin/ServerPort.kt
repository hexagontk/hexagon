package com.hexagonkt.http.server

/**
 * Represents a server instance of one kind.
 */
interface ServerPort {

    /**
     * Gets the runtime port if started, throw an exception otherwise.
     */
    fun runtimePort(): Int

    /**
     * .
     */
    fun started(): Boolean

    /**
     * Builds a server of a certain engine from a server definition and runs it.
     */
    fun startup(server: Server)

    /**
     * Stops the instance of the engine.
     */
    fun shutdown()
}
