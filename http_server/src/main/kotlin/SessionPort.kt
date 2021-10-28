package com.hexagonkt.http.server

interface SessionPort {

    /**
     * Returns the time when this session was created.
     */
    fun creationTime(): Long?

    /**
     * Returns the last time the client sent a request associated with
     * this session.
     */
    fun lastAccessedTime(): Long?

    /** A string containing the unique identifier assigned to this session (Cookie). */
    fun id(): String?

    /**
     * Returns the maximum time interval in seconds between client accesses
     * after which the session will be invalidated.
     */
    fun maxInactiveInterval(): Int?

    /**
     * Sets the time, in seconds, between client requests before the
     * session is invalidated.
     *
     * @param value Maximum inactive time interval in seconds.
     */
    fun maxInactiveInterval(value: Int?)

    /**
     * Returns a [Map] of attribute object bound to this session.
     */
    fun attributes(): Map<String, Any?>

    /**
     * Returns the attribute object bound to this session by the given name.
     *
     * @param name [String] specifying the name of the object.
     */
    fun getAttribute(name: String): Any?

    /**
     * Sets a attribute object to this session with the given name.
     *
     * @param name [String] specifying the name of the object.
     * @param value The object to be bound.
     */
    fun setAttribute(name: String, value: Any)

    /**
     * Removes the bound object from the session attribute with the specified name.
     *
     * @param name [String] specifying the name of the object.
     */
    fun removeAttribute(name: String)

    /**
     * Invalidates this session then unbinds any objects bound
     * to it.
     */
    fun invalidate()

    /**
     * Returns <code>true</code> if the client does not yet know about the
     * session or if the client chooses not to join the session.
     */
    fun isNew(): Boolean
}
