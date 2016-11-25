package co.there4.hexagon.web.undertow

import io.undertow.server.HttpServerExchange
import co.there4.hexagon.web.Session

class UndertowSession(exchange: HttpServerExchange) : Session {
    override fun get(name: String): Any? {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun set(name: String, value: Any) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun remove(name: String) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    val e: HttpServerExchange = exchange

    override val attributes: MutableMap<String, Any>
        get() = mutableMapOf()
//        get() = throw UnsupportedOperationException()

    override val creationTime: Long
        get() = throw UnsupportedOperationException()

    /** A string containing the unique identifier assigned to this session (Cookie). */
    override var id: String
        get() = throw UnsupportedOperationException()
        set(value) { throw UnsupportedOperationException() }

    override val lastAccessedTime: Long
        get() = throw UnsupportedOperationException()

    override var maxInactiveInterval: Int
        get() = throw UnsupportedOperationException()
        set(value) { throw UnsupportedOperationException() }

    override fun invalidate () = throw UnsupportedOperationException ()

    override fun isNew (): Boolean = throw UnsupportedOperationException ()
}
