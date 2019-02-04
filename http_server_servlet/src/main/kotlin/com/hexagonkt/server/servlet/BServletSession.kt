package com.hexagonkt.http.server.servlet

import com.hexagonkt.http.server.Session
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

internal class BServletSession(private val req: HttpServletRequest) : Session() {

    override fun getAttribute(name: String): Any? = req.session.getAttribute(name)

    override fun setAttribute(name: String, value: Any) { req.session.setAttribute(name, value) }

    override fun removeAttribute(name: String) { req.session.removeAttribute(name) }

    override fun attributeNames(): List<String> = req.session.attributeNames.toList()

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
