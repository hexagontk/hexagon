package co.there4.hexagon.web

/**
 * Provides session information.
 */
interface Session {
    val creationTime: Long
    val lastAccessedTime: Long

    /** A string containing the unique identifier assigned to this session (Cookie). */
    var id: String
    var maxInactiveInterval: Int

    fun invalidate ()
    fun isNew (): Boolean

    operator fun get(name: String): Any?
    operator fun set(name: String, value: Any)

    fun remove(name: String)
    val attributes: Map<String, Any?>
}
