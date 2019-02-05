package com.hexagonkt.http.server

/**
 * Provides session information.
 */
abstract class Session {
    val attributes: Map<String, Any?> get() = attributeNames.map { it to this[it] }.toMap()

    val creationTime: Long? by lazy { creationTime() }
    val lastAccessedTime: Long? by lazy { lastAccessedTime() }
    val attributeNames: List<String> by lazy { attributeNames() }

    /** A string containing the unique identifier assigned to this session (Cookie). */
    val id: String? by lazy { id() }

    var maxInactiveInterval: Int?
        get() = maxInactiveInterval()
        set(value) { maxInactiveInterval(value) }

    operator fun get(name: String): Any? = getAttribute(name)
    operator fun set(name: String, value: Any) { setAttribute(name, value) }

    protected abstract fun creationTime(): Long?
    protected abstract fun lastAccessedTime(): Long?
    protected abstract fun attributeNames(): List<String>

    /** A string containing the unique identifier assigned to this session (Cookie). */
    protected abstract fun id(): String?
    protected abstract fun maxInactiveInterval(): Int?

    protected abstract fun maxInactiveInterval(value: Int?)

    protected abstract fun getAttribute(name: String): Any?
    protected abstract fun setAttribute(name: String, value: Any)

    abstract fun invalidate ()
    abstract fun isNew (): Boolean
    abstract fun removeAttribute(name: String)
}
