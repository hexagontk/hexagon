package co.there4.hexagon.server.engine.undertow

import io.undertow.server.HttpServerExchange
import co.there4.hexagon.server.EngineSession

class UndertowSession(exchange: HttpServerExchange) : EngineSession {
    override val attributeNames: List<String>
        get() = TODO("not implemented")

    override fun getAttribute(name: String): Any? {
        TODO("not implemented")
    }

    override fun setAttribute(name: String, value: Any) {
        TODO("not implemented")
    }

    override fun removeAttribute(name: String) {
        TODO("not implemented")
    }

    val e: HttpServerExchange = exchange

    override val creationTime: Long
        get() = throw UnsupportedOperationException()

    /** A string containing the unique identifier assigned to this session (Cookie). */
    override var id: String?
        get() = throw UnsupportedOperationException()
        set(value) { throw UnsupportedOperationException() }

    override val lastAccessedTime: Long
        get() = throw UnsupportedOperationException()

    override var maxInactiveInterval: Int?
        get() = throw UnsupportedOperationException()
        set(value) { throw UnsupportedOperationException() }

    override fun invalidate () = throw UnsupportedOperationException ()

    override fun isNew (): Boolean = throw UnsupportedOperationException ()
}
