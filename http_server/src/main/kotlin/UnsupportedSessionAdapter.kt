package com.hexagonkt.http.server

object UnsupportedSessionAdapter : SessionPort {

    override fun creationTime(): Long? {
        throw UnsupportedOperationException()
    }

    override fun lastAccessedTime(): Long? {
        throw UnsupportedOperationException()
    }

    override fun id(): String? {
        throw UnsupportedOperationException()
    }

    override fun maxInactiveInterval(): Int? {
        throw UnsupportedOperationException()
    }

    override fun maxInactiveInterval(value: Int?) {
        throw UnsupportedOperationException()
    }

    override fun attributes(): Map<String, Any?> {
        throw UnsupportedOperationException()
    }

    override fun getAttribute(name: String): Any? {
        throw UnsupportedOperationException()
    }

    override fun setAttribute(name: String, value: Any) {
        throw UnsupportedOperationException()
    }

    override fun removeAttribute(name: String) {
        throw UnsupportedOperationException()
    }

    override fun invalidate() {
        throw UnsupportedOperationException()
    }

    override fun isNew(): Boolean {
        throw UnsupportedOperationException()
    }
}
