package com.hexagonkt.http.server

/**
 * Provides session information.
 */
abstract class Session {
    val attributes: Map<String, Any?> get() =
        attributeNames.map { it to this.getAttribute(it) }.toMap()

    val creationTime: Long? by lazy { creationTime() }
    val lastAccessedTime: Long? by lazy { lastAccessedTime() }
    val attributeNames: List<String> by lazy { attributeNames() }

    /** A string containing the unique identifier assigned to this session (Cookie). */
    val id: String? by lazy { id() }

    var maxInactiveInterval: Int?
        get() = maxInactiveInterval()
        set(value) { maxInactiveInterval(value) }

    protected abstract fun creationTime(): Long?
    protected abstract fun lastAccessedTime(): Long?
    protected abstract fun attributeNames(): List<String>

    /** A string containing the unique identifier assigned to this session (Cookie). */
    protected abstract fun id(): String?
    protected abstract fun maxInactiveInterval(): Int?

    protected abstract fun maxInactiveInterval(value: Int?)

    abstract fun getAttribute(name: String): Any?
    abstract fun setAttribute(name: String, value: Any)
    abstract fun removeAttribute(name: String)

    abstract fun invalidate ()
    abstract fun isNew (): Boolean
}
