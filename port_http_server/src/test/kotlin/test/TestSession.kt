package com.hexagonkt.http.server.test

import com.hexagonkt.http.server.SessionPort

data class TestSession(
    var attributes: Map<String, Any?> = emptyMap(),
    var creationTime: Long? = null,
    var id: String? = null,
    var maxInactiveInterval: Int? = null,
    var lastAccessedTime: Long? = null
): SessionPort {

    override fun attributes(): Map<String, Any?> = attributes
    override fun creationTime(): Long? = creationTime
    override fun getAttribute(name: String): Any? = attributes[name]
    override fun id(): String? = id

    override fun invalidate() {
        id = null
        creationTime = null
    }

    override fun isNew(): Boolean = id == null

    override fun lastAccessedTime(): Long? = lastAccessedTime

    override fun maxInactiveInterval(): Int? = maxInactiveInterval

    override fun maxInactiveInterval(value: Int?) {
        maxInactiveInterval = value
    }

    override fun removeAttribute(name: String) {
        attributes = attributes - name
    }

    override fun setAttribute(name: String, value: Any) {
        attributes = attributes + (name to value)
    }
}
