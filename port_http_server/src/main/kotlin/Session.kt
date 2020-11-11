package com.hexagonkt.http.server

/**
 * Provides session information.
 */
abstract class Session {

    /**
     * The time when this session was created.
     */
    val creationTime: Long? by lazy { creationTime() }

    /**
     * The last time the client sent a request associated with
     * this session.
     */
    val lastAccessedTime: Long? by lazy { lastAccessedTime() }

    /** A string containing the unique identifier assigned to this session (Cookie). */
    val id: String? by lazy { id() }

    /**
     * The maximum time interval in seconds between client accesses
     * after which the session will be invalidated.
     */
    var maxInactiveInterval: Int?
        get() = maxInactiveInterval()
        set(value) { maxInactiveInterval(value) }

    /**
     * [Map] of attribute objects bound to the session.
     */
    val attributes: Map<String, Any?> by lazy { attributes() }

    /**
     * Returns the attribute object bound to this session by the given name.
     *
     * @param name [String] specifying the name of the object.
     */
    fun get(name: String): Any? = getAttribute(name)

    /**
     * Sets a attribute object to this session with the given name.
     *
     * @param name [String] specifying the name of the object.
     * @param value The object to be bound.
     */
    fun set(name: String, value: Any) { setAttribute(name, value) }

    /**
     * Removes the bound object from the session attribute with the specified name.
     *
     * @param name [String] specifying the name of the object.
     */
    fun remove(name: String) { removeAttribute(name) }

    /**
     * Returns the time when this session was created.
     */
    protected abstract fun creationTime(): Long?

    /**
     * Returns the last time the client sent a request associated with
     * this session.
     */
    protected abstract fun lastAccessedTime(): Long?

    /** A string containing the unique identifier assigned to this session (Cookie). */
    protected abstract fun id(): String?

    /**
     * Returns the maximum time interval in seconds between client accesses
     * after which the session will be invalidated.
     */
    protected abstract fun maxInactiveInterval(): Int?

    /**
     * Sets the time, in seconds, between client requests before the
     * session is invalidated.
     *
     * @param value Maximum inactive time interval in seconds.
     */
    protected abstract fun maxInactiveInterval(value: Int?)

    /**
     * Returns a [Map] of attribute object bound to this session.
     */
    protected abstract fun attributes(): Map<String, Any?>

    /**
     * Returns the attribute object bound to this session by the given name.
     *
     * @param name [String] specifying the name of the object.
     */
    abstract fun getAttribute(name: String): Any?

    /**
     * Sets a attribute object to this session with the given name.
     *
     * @param name [String] specifying the name of the object.
     * @param value The object to be bound.
     */
    abstract fun setAttribute(name: String, value: Any)

    /**
     * Removes the bound object from the session attribute with the specified name.
     *
     * @param name [String] specifying the name of the object.
     */
    abstract fun removeAttribute(name: String)

    /**
     * Invalidates this session then unbinds any objects bound
     * to it.
     */
    abstract fun invalidate()

    /**
     * Returns <code>true</code> if the client does not yet know about the
     * session or if the client chooses not to join the session.
     */
    abstract fun isNew(): Boolean
}
