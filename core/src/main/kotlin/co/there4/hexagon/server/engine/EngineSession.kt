package co.there4.hexagon.server.engine

/**
 * Provides session information.
 */
interface EngineSession {
    val creationTime: Long?
    val lastAccessedTime: Long?
    val attributeNames: List<String>

    /** A string containing the unique identifier assigned to this session (Cookie). */
    var id: String?
    var maxInactiveInterval: Int?

    fun invalidate ()
    fun isNew (): Boolean
    fun getAttribute(name: String): Any?
    fun setAttribute(name: String, value: Any)
    fun removeAttribute(name: String)
}
