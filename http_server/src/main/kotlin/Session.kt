package com.hexagonkt.http.server

/**
 * Provides session information.
 */
class Session(val adapter: SessionPort) {

    /**
     * The time when this session was created.
     */
    val creationTime: Long? by lazy { adapter.creationTime() }

    /**
     * The last time the client sent a request associated with
     * this session.
     */
    val lastAccessedTime: Long? by lazy { adapter.lastAccessedTime() }

    /** A string containing the unique identifier assigned to this session (Cookie). */
    val id: String? by lazy { adapter.id() }

    /**
     * The maximum time interval in seconds between client accesses
     * after which the session will be invalidated.
     */
    var maxInactiveInterval: Int?
        get() = adapter.maxInactiveInterval()
        set(value) { adapter.maxInactiveInterval(value) }

    /**
     * [Map] of attribute objects bound to the session.
     */
    val attributes: Map<String, Any?> by lazy { adapter.attributes() }

    /**
     * Returns the attribute object bound to this session by the given name.
     *
     * @param name [String] specifying the name of the object.
     */
    fun get(name: String): Any? = adapter.getAttribute(name)

    /**
     * Sets a attribute object to this session with the given name.
     *
     * @param name [String] specifying the name of the object.
     * @param value The object to be bound.
     */
    fun set(name: String, value: Any) { adapter.setAttribute(name, value) }

    /**
     * Removes the bound object from the session attribute with the specified name.
     *
     * @param name [String] specifying the name of the object.
     */
    fun remove(name: String) {
        adapter.removeAttribute(name)
    }

    /**
     * Removes the bound object from the session attribute with the specified name.
     *
     * @param name [String] specifying the name of the object.
     */
    fun removeAttribute(name: String) {
        adapter.removeAttribute(name)
    }

    /**
     * Returns <code>true</code> if the client does not yet know about the
     * session or if the client chooses not to join the session.
     */
    fun isNew(): Boolean =
        adapter.isNew()

    /**
     * Invalidates this session then unbinds any objects bound
     * to it.
     */
    fun invalidate() {
        adapter.invalidate()
    }
}
