package com.hexagonkt.http.server.servlet

import com.hexagonkt.http.Cookie
import com.hexagonkt.http.server.ResponsePort
import java.io.OutputStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.Cookie as ServletCookie

internal class ResponseAdapter(
    private val req: HttpServletRequest,
    private val resp: HttpServletResponse) : ResponsePort {

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

    override fun addCookie (cookie: Cookie) {
        resp.addCookie(ServletCookie(cookie.name, cookie.value))
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
