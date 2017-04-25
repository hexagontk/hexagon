package co.there4.hexagon.web.backend

import co.there4.hexagon.web.Server

interface IServer {
    /**
     * Gets the runtime port if started, throw an exception otherwise.
     */
    fun runtimePort(): Int

    fun started(): Boolean

    /**
     * Builds a server of a certain backend from a server definition and runs it.
     */
    fun startup(server: Server)

    /**
     * Stops the instance of the backend.
     */
    fun shutdown()
}
