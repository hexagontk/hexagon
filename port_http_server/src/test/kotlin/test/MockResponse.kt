package com.hexagonkt.http.server.test

import com.hexagonkt.http.Cookie
import com.hexagonkt.http.server.ResponsePort
import java.io.OutputStream

internal class MockResponse(private val testResponse: TestResponse): ResponsePort {
    override fun addCookie(cookie: Cookie) {
        testResponse.cookies = testResponse.cookies + (cookie.name to cookie)
    }

    override fun body(): Any = testResponse.body

    override fun body(value: Any) {
        testResponse.body = value
    }

    override fun contentType(): String? = testResponse.contentType

    override fun contentType(value: String?) {
        testResponse.contentType = value
    }

    override fun outputStream(): OutputStream = testResponse.outputStream

    override fun redirect(url: String) {
        testResponse.status = 301
//        setHeader("location", url) // TODO Fix this
    }

    private val headers: MutableMap<String, Any?> = LinkedHashMap()

    override fun removeCookie(name: String) {
        testResponse.cookies = testResponse.cookies - name
    }

    override fun status(): Int = testResponse.status

    override fun status(value: Int) {
        testResponse.status = value
    }
}
