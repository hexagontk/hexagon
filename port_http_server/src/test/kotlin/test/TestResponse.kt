package com.hexagonkt.http.server.test

import com.hexagonkt.http.Cookie
import com.hexagonkt.http.server.ResponsePort
import java.io.ByteArrayOutputStream
import java.io.OutputStream

data class TestResponse(
    var cookies: Map<String, Cookie> = emptyMap(),
    var body: Any = "",
    var contentType: String? = null,
    var status: Int = 200,
    var outputStream: OutputStream = ByteArrayOutputStream()
): ResponsePort {

    override fun addCookie(cookie: Cookie) {
        cookies = cookies + (cookie.name to cookie)
    }

    override fun body(): Any = body

    override fun body(value: Any) {
        body = value
    }

    override fun contentType(): String? = contentType

    override fun contentType(value: String?) {
        contentType = value
    }

    override fun outputStream(): OutputStream = outputStream

    override fun redirect(url: String) {
        status = 301
     // TODO Fix this
    }

    private val headers: MutableMap<String, Any?> = LinkedHashMap()

    override fun removeCookie(name: String) {
        cookies = cookies - name
    }

    override fun status(): Int = status

    override fun status(value: Int) {
        status = value
    }
}
