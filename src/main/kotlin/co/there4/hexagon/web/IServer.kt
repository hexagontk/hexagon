package co.there4.hexagon.web

interface IServer {
    fun started (): Boolean

    /**
     * Builds a server of a certain backend from a server definition and runs it.
     */
    fun startup(server: Server)

    /**
     * Stops the instance of the backend.
     */
    fun shutdown()
}
