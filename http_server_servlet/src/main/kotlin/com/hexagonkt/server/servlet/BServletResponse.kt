package com.hexagonkt.http.server.servlet

import com.hexagonkt.http.server.Response
import java.io.OutputStream
import java.net.HttpCookie
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

internal class BServletResponse(
    private val req: HttpServletRequest,
    private val resp: HttpServletResponse) : Response() {

    private var bodyValue: Any = ""

    override fun outputStream(): OutputStream = resp.outputStream

    override fun body(): Any = bodyValue

    override fun body(value: Any) {
        bodyValue = value
    }

    override fun status(): Int = resp.status

    override fun status(value: Int) { resp.status = value }

    override fun contentType(): String? = resp.contentType

    override fun contentType(value: String?) { resp.contentType = value }

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
