package com.hexagonkt.http.server.test

import com.hexagonkt.http.server.Session

internal class MockSession(private val testSession: TestSession): Session() {
    override fun attributes(): Map<String, Any?> = testSession.attributes
    override fun creationTime(): Long? = testSession.creationTime
    override fun getAttribute(name: String): Any? = testSession.attributes[name]
    override fun id(): String? = testSession.id

    override fun invalidate() {
        testSession.id = null
        testSession.creationTime = null
    }

    override fun isNew(): Boolean = testSession.id == null

    override fun lastAccessedTime(): Long? = lastAccessedTime

    override fun maxInactiveInterval(): Int? = testSession.maxInactiveInterval

    override fun maxInactiveInterval(value: Int?) {
        testSession.maxInactiveInterval = value
    }

    override fun removeAttribute(name: String) {
        testSession.attributes = testSession.attributes - name
    }

    override fun setAttribute(name: String, value: Any) {
        testSession.attributes = testSession.attributes + (name to value)
    }
}
