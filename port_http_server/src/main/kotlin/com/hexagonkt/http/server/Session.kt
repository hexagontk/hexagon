package com.hexagonkt.http.server

/**
 * Provides session information.
 */
abstract class Session {

    val creationTime: Long? by lazy { creationTime() }
    val lastAccessedTime: Long? by lazy { lastAccessedTime() }

    /** A string containing the unique identifier assigned to this session (Cookie). */
    val id: String? by lazy { id() }

    var maxInactiveInterval: Int?
        get() = maxInactiveInterval()
        set(value) { maxInactiveInterval(value) }

    val attributes: Map<String, Any?> by lazy { attributes() }

    fun get(name: String): Any? = getAttribute(name)

    fun set(name: String, value: Any) { setAttribute(name, value) }

    fun remove(name: String) { removeAttribute(name) }

    protected abstract fun creationTime(): Long?
    protected abstract fun lastAccessedTime(): Long?

    /** A string containing the unique identifier assigned to this session (Cookie). */
    protected abstract fun id(): String?

    protected abstract fun maxInactiveInterval(): Int?
    protected abstract fun maxInactiveInterval(value: Int?)

    protected abstract fun attributes(): Map<String, Any?>

    abstract fun getAttribute(name: String): Any?
    abstract fun setAttribute(name: String, value: Any)
    abstract fun removeAttribute(name: String)

    abstract fun invalidate ()
    abstract fun isNew (): Boolean
}
