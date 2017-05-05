package co.there4.hexagon.server.backend

import co.there4.hexagon.server.Server

/**
 * Represents a server instance of one kind.
 */
interface ServerEngine {
    /**
     * Gets the runtime port if started, throw an exception otherwise.
     */
    fun runtimePort(): Int

    /**
     * .
     */
    fun started(): Boolean

    /**
     * Builds a server of a certain backend from a server definition and runs it.
     */
    fun startup(server: Server, settings: Map<String, *> = emptyMap<String, Any>())

    /**
     * Stops the instance of the backend.
     */
    fun shutdown()
}
