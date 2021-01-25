package com.hexagonkt.http.server.servlet

import com.hexagonkt.http.server.SessionPort
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

internal class SessionAdapter(private val req: HttpServletRequest) : SessionPort {

    override fun getAttribute(name: String): Any? = req.session.getAttribute(name)

    override fun setAttribute(name: String, value: Any) { req.session.setAttribute(name, value) }

    override fun removeAttribute(name: String) { req.session.removeAttribute(name) }

    override fun attributes(): Map<String, Any?> = req.session.attributeNames
        .toList()
        .map { it to this.getAttribute(it) }
        .toMap()

    override fun creationTime(): Long? = session(req)?.creationTime
    override fun lastAccessedTime(): Long? = session(req)?.lastAccessedTime

    override fun id(): String? = session(req)?.id

    override fun maxInactiveInterval(): Int? = session(req)?.maxInactiveInterval

    override fun maxInactiveInterval(value: Int?) {
        session(req)?.maxInactiveInterval = value ?: 10
    }

    override fun invalidate() = req.session.invalidate()

    override fun isNew() = req.session.isNew

    private fun session (req: HttpServletRequest, create: Boolean = false): HttpSession? =
        req.getSession(create)
}
