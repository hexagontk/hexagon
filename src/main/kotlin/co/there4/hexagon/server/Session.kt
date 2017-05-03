package co.there4.hexagon.server

import co.there4.hexagon.server.backend.ISession

/**
 * Provides session information.
 */
class Session (private val session: ISession) {
    val attributes: Map<String, Any?> get() = attributeNames.map { it to this[it] }.toMap()

    val creationTime: Long? by lazy { session.creationTime }
    val lastAccessedTime: Long? by lazy { session.lastAccessedTime }
    val attributeNames: List<String> by lazy { session.attributeNames }

    /** A string containing the unique identifier assigned to this session (Cookie). */
    var id: String?
        get() = session.id
        set(value) { session.id = value }

    var maxInactiveInterval: Int?
        get() = session.maxInactiveInterval
        set(value) { session.maxInactiveInterval = value }

    fun invalidate () = session.invalidate()
    fun isNew (): Boolean = session.isNew()
    fun removeAttribute(name: String) = session.removeAttribute(name)

    operator fun get(name: String): Any? = session.getAttribute(name)
    operator fun set(name: String, value: Any) { session.setAttribute(name, value) }
}
