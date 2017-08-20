package com.hexagonkt.server.undertow

import io.undertow.server.HttpServerExchange
import com.hexagonkt.server.EngineSession
import io.undertow.server.session.Session
import io.undertow.server.session.SessionConfig
import io.undertow.server.session.SessionManager
import io.undertow.server.session.SessionManager.ATTACHMENT_KEY as MANAGER_ATTACHMENT_KEY
import io.undertow.server.session.SessionConfig.ATTACHMENT_KEY as CONFIG_ATTACHMENT_KEY

class UndertowSession(private val exchange: HttpServerExchange) : EngineSession {
    private val sessionManager: SessionManager = exchange.getAttachment(MANAGER_ATTACHMENT_KEY)
    private val sessionConfig: SessionConfig = exchange.getAttachment(CONFIG_ATTACHMENT_KEY)
    private val session: Session? = sessionManager.getSession(exchange, sessionConfig)

    private val createdSession: Session by lazy {
        session ?: sessionManager.createSession(exchange, sessionConfig)
    }

    override val attributeNames: List<String> get() = createdSession.attributeNames.toList()

    override fun getAttribute(name: String): Any? = createdSession.getAttribute(name)

    override fun setAttribute(name: String, value: Any) { createdSession.setAttribute(name, value) }

    override fun removeAttribute(name: String) { createdSession.removeAttribute(name) }

    override val creationTime: Long?
        get() = session?.creationTime

    /** A string containing the unique identifier assigned to this session (Cookie). */
    override var id: String?
        get() = session?.id
        set(value) { throw UnsupportedOperationException() }

    override val lastAccessedTime: Long?
        get() = session?.lastAccessedTime

    override var maxInactiveInterval: Int?
        get() = session?.maxInactiveInterval
        set(value) {
            if (value != null)
                session?.maxInactiveInterval = value
        }

    override fun invalidate () { session?.invalidate(exchange) }

    override fun isNew (): Boolean = session == null
}
