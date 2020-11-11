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
     * Checks whether the server has been started.
     *
     * @return True if the server has started, else false.
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
