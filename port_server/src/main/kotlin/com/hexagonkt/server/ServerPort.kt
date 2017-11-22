package com.hexagonkt.server

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
    fun startup(server: Server, settings: Map<String, *> = emptyMap<String, Any>())

    /**
     * Stops the instance of the engine.
     */
    fun shutdown()
}
