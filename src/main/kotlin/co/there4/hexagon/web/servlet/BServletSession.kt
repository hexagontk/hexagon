package co.there4.hexagon.web.servlet

import co.there4.hexagon.web.Session
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

internal class BServletSession(val req: HttpServletRequest) : Session {

    override fun get(name: String): Any? = req.session.getAttribute(name)

    override fun set(name: String, value: Any) { req.session.setAttribute(name, value) }

    override fun remove(name: String) { req.session.removeAttribute(name) }

    override val attributes: Map<String, Any?>
        get() = req.session.attributeNames.toList().map { it to this[it] }.toMap()

    override val creationTime: Long by lazy { session (req)?.creationTime ?: 0L }
    override val lastAccessedTime: Long by lazy { session (req)?.lastAccessedTime ?: 0L }
    override var id: String
        get() = session (req)?.id ?: ""
        set(value) { }
    override var maxInactiveInterval: Int
        get() = session (req)?.maxInactiveInterval ?: 0
        set(value) { }

    override fun invalidate() = req.session.invalidate()

    override fun isNew() = req.session.isNew

    private fun session (req: HttpServletRequest): HttpSession? = req.getSession(false)
}
