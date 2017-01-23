package co.there4.hexagon.web.servlet

import co.there4.hexagon.web.Session
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

internal class BServletSession(val req: HttpServletRequest) : Session {

    override fun get(name: String): Any? = req.session.getAttribute(name)

    override fun set(name: String, value: Any) { req.session.setAttribute(name, value) }

    override fun remove(name: String) { req.session.removeAttribute(name) }

    override val attributeNames: List<String> get() = req.session.attributeNames.toList()

    override val creationTime: Long? by lazy { session (req)?.creationTime }
    override val lastAccessedTime: Long? by lazy { session (req)?.lastAccessedTime }

    override var id: String?
        get() = session (req)?.id
        set(value) { }

    override var maxInactiveInterval: Int?
        get() = session (req)?.maxInactiveInterval
        set(value) { }

    override fun invalidate() = req.session.invalidate()

    override fun isNew() = req.session.isNew

    private fun session (req: HttpServletRequest, create: Boolean = false): HttpSession? =
        req.getSession(create)
}
