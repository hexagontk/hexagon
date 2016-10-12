package co.there4.hexagon.web.servlet

import co.there4.hexagon.web.Response
import java.io.OutputStream
import java.net.HttpCookie
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class BServletResponse(val req: HttpServletRequest, val resp: HttpServletResponse) : Response {
    override var body: Any = ""

    override var status: Int
        get() = resp.status
        set(value) { resp.status = value }

    override var contentType: String?
        get() = resp.contentType
        set(value) { resp.contentType = value }

    override val outputStream: OutputStream = resp.outputStream

    override fun getMimeType (file: String): String? = req.servletContext.getMimeType(file)

    override fun addHeader (name: String, value: String) {
        resp.addHeader(name, value)
    }

    override fun addCookie (cookie: HttpCookie) {
        resp.addCookie(Cookie(cookie.name, cookie.value))
    }

    override fun removeCookie (name: String) {
        val cookie = req.cookies.find { it.name == name }
        if (cookie != null) {
            cookie.value = ""
            cookie.path = "/"
            cookie.maxAge = 0
            resp.addCookie(cookie)
        }
    }

    override fun redirect(url: String) {
        resp.sendRedirect(url)
    }
}
