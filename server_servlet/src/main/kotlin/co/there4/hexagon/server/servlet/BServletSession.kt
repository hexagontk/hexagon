package co.there4.hexagon.server.servlet

import co.there4.hexagon.server.EngineSession
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

internal class BServletSession(val req: HttpServletRequest) : EngineSession {

    override fun getAttribute(name: String): Any? = req.session.getAttribute(name)

    override fun setAttribute(name: String, value: Any) { req.session.setAttribute(name, value) }

    override fun removeAttribute(name: String) { req.session.removeAttribute(name) }

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
